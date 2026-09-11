package com.example.shortener.controller;

import com.example.shortener.analytics.*;
import com.example.shortener.dto.*;
import com.example.shortener.service.*;
import com.example.shortener.util.HeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Clock;
import java.util.UUID;

import static com.example.shortener.common.Constants.*;

@RestController
public class UrlController {

    private final UrlService urls;
    private final AnalyticsPublisher analytics;
    private final AnalyticsService analyticsService;
    private final Clock clock;

    public UrlController(UrlService urls, AnalyticsPublisher analytics, AnalyticsService analyticsService, Clock clock) {
        this.urls = urls;
        this.analytics = analytics;
        this.analyticsService = analyticsService;
        this.clock = clock;
    }

    /**
     * Creates a new shortened URL.
     * <p>
     * Accepts a {@link CreateShortUrlRequest} payload describing the URL to be shortened,
     * validates it, and delegates creation to the {@code urls} service. An optional
     * {@code Idempotency-Key} header can be supplied by the client to ensure that
     * retried requests do not result in duplicate URL creation.
     *
     * @param request        the request body containing the details of the URL to shorten;
     *                       must be valid according to the constraints defined on
     *                       {@link CreateShortUrlRequest}
     * @param idempotencyKey optional idempotency key, provided via the
     *                       {@code Idempotency-Key} request header, used to safely
     *                       retry the request without creating duplicate resources;
     *                       may be {@code null} if not supplied
     * @return a {@link ResponseEntity} with HTTP status {@code 201 Created} containing
     * a {@link CreateShortUrlResponse} with details of the newly created short URL
     */
    @PostMapping("/api/v1/urls")
    public ResponseEntity<CreateShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request,
                                                         @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urls.create(request, idempotencyKey));
    }

    /**
     * Resolves a short URL code to its destination and redirects the client accordingly.
     * <p>
     * Looks up the target URL associated with the given {@code code}, records an analytics
     * event capturing coarse-grained request metadata (country, region, referer host, and
     * user-agent category), and responds with an HTTP {@code 302 Found} redirect to the
     * resolved destination.
     * <p>
     * The {@code code} path variable is constrained to alphanumeric characters, underscores,
     * and hyphens, between 3 and 32 characters long.
     *
     * @param code    the short URL code to resolve; must match {@code [A-Za-z0-9_-]{3,32}}
     * @param request the incoming HTTP servlet request, used to extract headers such as
     *                {@code X-Country}, {@code X-Region}, {@code Referer}, and {@code User-Agent}
     *                for analytics purposes
     * @return a {@link ResponseEntity} with HTTP status {@code 302 Found} and a
     * {@code Location} header set to the resolved destination URL; the response
     * body is empty
     */
    @GetMapping("/{code:[A-Za-z0-9_-]{3,32}}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        UrlService.ResolvedUrl resolvedUrl = urls.resolve(code);
        String country = HeaderUtil.getHeaderValue(request, X_COUNTRY_HEADER_NAME, UNKNOWN);
        String region = HeaderUtil.getHeaderValue(request, X_REGION_HEADER_NAME, UNKNOWN);
        String host = HeaderUtil.deriveHost(request.getHeader(REFERER_HEADER_NAME));
        String userAgent = HeaderUtil.deriveUserAgent(request.getHeader(USER_AGENT_HEADER_NAME));
        analytics.publish(new AnalyticsEvent(UUID.randomUUID(), code, clock.instant(), country, region, host, userAgent));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(resolvedUrl.destination())).build();
    }

    /**
     * Retrieves analytics data for a specific short URL code.
     *
     * @param code the short URL code whose analytics are to be retrieved
     * @return an {@link AnalyticsResponse} containing the aggregated analytics data
     * associated with the given short URL code
     */
    @GetMapping("/api/v1/urls/{code}/analytics")
    public AnalyticsResponse analytics(@PathVariable String code) {
        return analyticsService.get(code);
    }

    /**
     * Disables a short URL, preventing it from resolving to its destination.
     * <p>
     * The short URL identified by {@code code} is deactivated rather than deleted outright;
     * subsequent redirect attempts for this code should no longer succeed. Responds with
     * HTTP {@code 204 No Content} on success.
     *
     * @param code the short URL code to disable
     */
    @DeleteMapping("/api/v1/urls/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable String code) {
        urls.disable(code);
    }
}
