package com.example.shortener.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.example.shortener.common.Constants.CORRELATION_ID_HEADER_NAME;

/**
 * Servlet filter that ensures every request is associated with a correlation ID,
 * useful for tracing a request across logs and downstream services.
 * <p>
 * If the incoming request already carries a correlation ID (via the
 * {@code CORRELATION_ID_HEADER_NAME} header), that value is reused; otherwise a new
 * random ID is generated. The resolved ID is exposed to the rest of the request
 * pipeline as a request attribute ({@code correlationId}) and echoed back to the
 * client in the response header, so callers can correlate their request with
 * server-side logs even if they didn't supply an ID themselves.
 * <p>
 * As a {@link OncePerRequestFilter}, this filter is guaranteed to execute at most
 * once per request, regardless of forwarding/dispatch.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    /**
     * Resolves the correlation ID for this request, storing it as a request attribute
     * and echoing it in the response, before continuing the filter chain.
     *
     * @param req   the incoming HTTP request
     * @param res   the outgoing HTTP response
     * @param chain the filter chain to continue after processing
     * @throws ServletException if an error occurs during downstream filter/servlet processing
     * @throws IOException      if an I/O error occurs during downstream filter/servlet processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String id = req.getHeader(CORRELATION_ID_HEADER_NAME);
        if (!StringUtils.hasText(id)) {
            id = UUID.randomUUID().toString();
        }
        req.setAttribute("correlationId", id);
        res.setHeader(CORRELATION_ID_HEADER_NAME, id);
        chain.doFilter(req, res);
    }
}
