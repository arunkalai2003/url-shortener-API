package com.example.shortener.controller;

import com.example.shortener.analytics.*;
import com.example.shortener.dto.*;
import com.example.shortener.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Clock;
import java.util.UUID;

@RestController
public class UrlController {
    private final UrlService urls; private final AnalyticsPublisher analytics; private final AnalyticsService analyticsService; private final Clock clock;
    public UrlController(UrlService urls,AnalyticsPublisher analytics,AnalyticsService analyticsService,Clock clock){this.urls=urls;this.analytics=analytics;this.analyticsService=analyticsService;this.clock=clock;}

    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request,
                                                          @RequestHeader(value="Idempotency-Key",required=false) String idempotencyKey){
        return ResponseEntity.status(HttpStatus.CREATED).body(urls.create(request,idempotencyKey));
    }

    @GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable String code,HttpServletRequest request){
        UrlService.ResolvedUrl r=urls.resolve(code);
        String country=header(request,"X-Country","UNKNOWN");
        String region=header(request,"X-Region","UNKNOWN");
        String ref=hostOnly(request.getHeader("Referer"));
        String ua=coarseUa(request.getHeader("User-Agent"));
        analytics.publish(new AnalyticsEvent(UUID.randomUUID(),code,clock.instant(),country,region,ref,ua));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(r.destination())).build();
    }

    @GetMapping("/api/v1/urls/{code}/analytics")
    public AnalyticsResponse analytics(@PathVariable String code){return analyticsService.get(code);}

    @DeleteMapping("/api/v1/urls/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable String code){urls.disable(code);}

    private String header(HttpServletRequest r,String name,String fallback){String v=r.getHeader(name);return v==null||v.isBlank()?fallback:v.substring(0,Math.min(v.length(),64));}
    private String hostOnly(String ref){try{return ref==null?"DIRECT":URI.create(ref).getHost();}catch(Exception e){return "UNKNOWN";}}
    private String coarseUa(String ua){if(ua==null)return "UNKNOWN";String x=ua.toLowerCase();if(x.contains("chrome"))return "Chrome";if(x.contains("safari"))return "Safari";if(x.contains("firefox"))return "Firefox";return "Other";}
}
