package com.example.shortener.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.shortener.common.Constants.X_FORWARDED_FOR_HEADER_NAME;

/**
 * Servlet filter that applies rate limiting to short URL creation requests, based
 * on client IP address.
 * <p>
 * Only requests matching {@code POST /api/v1/urls} are subject to rate limiting;
 * all other requests pass through unaffected. Rate limiting itself is delegated to
 * a {@link DistributedRateLimiter}, keyed by the resolved client IP.
 * <p>
 * As a {@link OncePerRequestFilter}, this filter is guaranteed to execute at most
 * once per request, regardless of forwarding/dispatch.
 *
 * @see DistributedRateLimiter
 */
@Component
public class CreateRateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMITED_PATH = "/api/v1/urls";
    private static final String RATE_LIMITED_METHOD = "POST";

    private final DistributedRateLimiter limiter;

    public CreateRateLimitFilter(DistributedRateLimiter limiter) {
        this.limiter = limiter;
    }

    /**
     * Applies rate limiting to short URL creation requests, based on client IP address.
     * <p>
     * Only requests matching {@code POST /api/v1/urls} are rate limited; all other
     * requests pass through unaffected. The client IP is derived from the first entry
     * of the {@code X-Forwarded-For} header if present, falling back to the request's
     * remote address otherwise.
     * <p>
     * <strong>Note:</strong> {@code X-Forwarded-For} is client-suppliable and should only
     * be trusted when this application sits behind a proxy that sets/overwrites it reliably;
     * otherwise this check can be bypassed or abused.
     * <p>
     * If the resolved IP has exceeded its allowed rate, responds immediately with
     * HTTP {@code 429 Too Many Requests} and a JSON error body, short-circuiting the
     * filter chain. Otherwise, the request proceeds normally.
     *
     * @param httpServletRequest   the incoming HTTP request
     * @param httpServletResponse   the outgoing HTTP response
     * @param filterChain the filter chain to continue if the request is not rate limited
     * @throws ServletException if an error occurs during downstream filter/servlet processing
     * @throws IOException      if an I/O error occurs writing the rate-limit response, or during
     *                          downstream filter/servlet processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if (isRateLimitedEndpoint(httpServletRequest) && !limiter.allow(resolveClientIp(httpServletRequest))) {
            writeRateLimitedResponse(httpServletResponse);
            return;
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    private boolean isRateLimitedEndpoint(HttpServletRequest httpServletRequest) {
        return RATE_LIMITED_METHOD.equals(httpServletRequest.getMethod()) && RATE_LIMITED_PATH.equals(httpServletRequest.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest httpServletRequest) {
        String forwardedFor = httpServletRequest.getHeader(X_FORWARDED_FOR_HEADER_NAME);
        if (!StringUtils.hasText(forwardedFor)) {
            return httpServletRequest.getRemoteAddr();
        }
        return forwardedFor.split(",")[0].trim();
    }

    private void writeRateLimitedResponse(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // res.getWriter().write(mapper.writeValueAsString(new RateLimitErrorResponse("RATE_LIMITED", "Too many create requests")));
        httpServletResponse.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Too many create requests\"}");
    }
}
