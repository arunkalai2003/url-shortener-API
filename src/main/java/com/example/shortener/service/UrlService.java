package com.example.shortener.service;

import com.example.shortener.cache.*;
import com.example.shortener.config.ShortenerProperties;
import com.example.shortener.domain.*;
import com.example.shortener.dto.*;
import com.example.shortener.exception.*;
import com.example.shortener.repository.*;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class UrlService {
    private static final String ALIAS_REGEX="^[A-Za-z0-9_-]{3,32}$";
    private final UrlNormalizer normalizer; private final ShortCodeGenerator generator; private final ExpirationPolicy expiration;
    private final ShortUrlRepository repo; private final IdempotencyRepository idempotencyRepo; private final ShortenerProperties p;
    private final Clock clock; private final UrlCacheService redisCache; private final Cache<String,Object> l1;

    public UrlService(UrlNormalizer normalizer,ShortCodeGenerator generator,ExpirationPolicy expiration,ShortUrlRepository repo,
                      IdempotencyRepository idempotencyRepo,ShortenerProperties p,Clock clock,UrlCacheService redisCache,Cache<String,Object> l1){
        this.normalizer=normalizer;this.generator=generator;this.expiration=expiration;this.repo=repo;this.idempotencyRepo=idempotencyRepo;
        this.p=p;this.clock=clock;this.redisCache=redisCache;this.l1=l1;
    }

    public CreateShortUrlResponse create(CreateShortUrlRequest request,String idempotencyKey){
        String normalized=normalizer.normalize(request.url());
        Instant expiresAt=expiration.validate(request.expiresAt());
        String requestHash=Hashing.sha256(normalized+"|"+String.valueOf(request.customAlias())+"|"+String.valueOf(expiresAt)+"|"+String.valueOf(request.campaign()));

        if(idempotencyKey!=null&&!idempotencyKey.isBlank()){
            var prior=idempotencyRepo.findById(idempotencyKey);
            if(prior.isPresent()){
                if(!prior.get().getRequestHash().equals(requestHash)) throw new ConflictException("Idempotency-Key was already used with a different request");
                return repo.findByShortCode(prior.get().getShortCode()).map(this::response).orElseThrow(() -> new ConflictException("Idempotency record is inconsistent"));
            }
        }

        String fingerprint=Hashing.sha256(normalized); // campaign-independent canonical identity
        Optional<ShortUrlEntity> existing=repo.findByUrlFingerprint(fingerprint);
        if(existing.isPresent()){
            saveIdempotencyIfPresent(idempotencyKey,requestHash,existing.get().getShortCode());
            return response(existing.get());
        }

        if(request.customAlias()!=null&&!request.customAlias().isBlank()) validateAlias(request.customAlias());

        int attempts=request.customAlias()==null||request.customAlias().isBlank()?p.maxCollisionRetries():1;
        for(int i=0;i<attempts;i++){
            String code=(request.customAlias()==null||request.customAlias().isBlank())?generator.generate():request.customAlias();
            Instant createdAt=clock.instant();
            int inserted=repo.insertIfAbsent(UUID.randomUUID(), code, normalized, normalized, fingerprint, createdAt, expiresAt);
            if(inserted==1){
                ShortUrlEntity entity=repo.findByShortCode(code).orElseThrow();
                saveIdempotencyIfPresent(idempotencyKey,requestHash,code);
                cache(entity);
                return response(entity);
            }

            // No exception-based concurrency: ON CONFLICT DO NOTHING keeps the transaction usable.
            Optional<ShortUrlEntity> winner=repo.findByUrlFingerprint(fingerprint);
            if(winner.isPresent()){
                saveIdempotencyIfPresent(idempotencyKey,requestHash,winner.get().getShortCode());
                return response(winner.get());
            }
            if(request.customAlias()!=null&&!request.customAlias().isBlank()) throw new ConflictException("Custom alias already exists");
            // Otherwise the generated short code collided; generate another candidate.
        }
        throw new ServiceUnavailableException("Unable to allocate a unique short code after bounded retries",null);
    }

    private void saveIdempotencyIfPresent(String key,String hash,String code){
        if(key==null||key.isBlank())return;
        idempotencyRepo.insertIfAbsent(key,hash,code,clock.instant());
        IdempotencyRecord existing=idempotencyRepo.findById(key).orElseThrow();
        if(!existing.getRequestHash().equals(hash)) throw new ConflictException("Idempotency-Key was already used with a different request");
    }

    private void validateAlias(String a){
        if(!a.matches(ALIAS_REGEX))throw new BadRequestException("customAlias must match "+ALIAS_REGEX);
        if(p.reservedAliases().stream().anyMatch(x->x.equalsIgnoreCase(a)))throw new BadRequestException("customAlias is reserved");
    }

    public ResolvedUrl resolve(String code){
        Object local=l1.getIfPresent(code);
        if(local instanceof CachedUrl c) return validateCached(code,c);
        if(local instanceof String s && s.equals("NOT_FOUND")) throw new NotFoundException("Short URL not found");

        // Caffeine get() performs per-key computation atomically within a pod, reducing cache stampede.
        Object loaded=l1.get(code,k->load(k));
        if(loaded instanceof String s && s.equals("NOT_FOUND")) throw new NotFoundException("Short URL not found");
        return validateCached(code,(CachedUrl)loaded);
    }

    private Object load(String code){
        Optional<CachedUrl> cached=redisCache.get(code);
        if(cached.isPresent()){
            if("NOT_FOUND".equals(cached.get().status()))return "NOT_FOUND";
            return cached.get();
        }
        Optional<ShortUrlEntity> db=repo.findByShortCode(code);
        if(db.isEmpty()) { redisCache.putNegative(code); return "NOT_FOUND"; }
        CachedUrl c=toCached(db.get()); redisCache.put(code,c); return c;
    }

    private ResolvedUrl validateCached(String code,CachedUrl c){
        if(!"ACTIVE".equals(c.status())) throw new GoneException("Short URL is not active");
        if(c.expiresAt()!=null&&!c.expiresAt().isAfter(clock.instant())) throw new GoneException("Short URL has expired");
        return new ResolvedUrl(code,c.originalUrl(),c.expiresAt());
    }

    @Transactional
    public void disable(String code){
        ShortUrlEntity e=repo.findByShortCode(code).orElseThrow(()->new NotFoundException("Short URL not found"));
        e.setStatus(UrlStatus.DELETED); repo.save(e); l1.invalidate(code); redisCache.evict(code);
    }

    private void cache(ShortUrlEntity e){ CachedUrl c=toCached(e); l1.put(e.getShortCode(),c); redisCache.put(e.getShortCode(),c); }
    private CachedUrl toCached(ShortUrlEntity e){return new CachedUrl(e.getNormalizedUrl(),e.getExpiresAt(),e.getStatus().name());}
    private CreateShortUrlResponse response(ShortUrlEntity e){return new CreateShortUrlResponse(e.getShortCode(),p.baseUrl()+"/"+e.getShortCode(),e.getNormalizedUrl(),e.getCreatedAt(),e.getExpiresAt());}
    public record ResolvedUrl(String shortCode,String destination,Instant expiresAt){}
}
