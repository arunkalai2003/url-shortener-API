package com.example.shortener.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.example.shortener.common.Constants.UNKNOWN;

/**
 * Utility methods for extracting and normalizing values from HTTP request headers,
 * primarily in support of coarse-grained analytics collection (e.g. browser
 * classification, referer host extraction, and safe header value retrieval).
 * <p>
 * All methods in this class are {@code static} and null-tolerant, favoring
 * well-defined fallback values ({@link #DIRECT}, {Constants#UNKNOWN}) over
 * exceptions or {@code null} returns, so callers can use the results directly
 * without additional null-checking.
 * <p>
 * This class is not intended to be instantiated.
 */
public class HeaderUtil {

    private static final int MAX_HEADER_VALUE_LENGTH = 64;
    private static final String DIRECT = "DIRECT";

    private static final List<Map.Entry<String, String>> BROWSER_SIGNATURES = List.of(
            Map.entry("chrome", "Chrome"),   // must precede "safari" — Chrome UAs also contain "safari"
            Map.entry("safari", "Safari"),
            Map.entry("firefox", "Firefox")
    );

    /**
     * Retrieves the value of a request header, truncated to a maximum length, falling
     * back to a default value when the header is absent or blank.
     *
     * @param httpServletRequest the incoming HTTP servlet request
     * @param name               the name of the header to retrieve
     * @param fallbackValue      the value to return if the header is missing or blank
     * @return the header value truncated to at most {@value #MAX_HEADER_VALUE_LENGTH}
     *         characters, or {@code fallbackValue} if the header has no text
     */
    public static String getHeaderValue(HttpServletRequest httpServletRequest, String name, String fallbackValue) {
        String value = httpServletRequest.getHeader(name);
        if (!StringUtils.hasText(value)) {
            return fallbackValue;
        }
        return value.substring(0, Math.min(value.length(), MAX_HEADER_VALUE_LENGTH));
    }

    /**
     * Derives a coarse, human-readable browser name from a raw User-Agent header value.
     * <p>
     * Performs a simple case-insensitive substring match against a small, ordered set of
     * known browser signatures (checked in order, since some User-Agent strings contain
     * multiple recognizable tokens — e.g. Chrome's UA also contains "safari"). Any
     * unrecognized browser is categorized as {@code "Other"}, and a blank or missing
     * User-Agent yields {Constants#UNKNOWN}.
     *
     * @param userAgent the raw User-Agent header value; may be {@code null} or blank
     * @return one of {@code "Chrome"}, {@code "Safari"}, {@code "Firefox"}, {@code "Other"},
     *         or {Constants#UNKNOWN} if {@code userAgent} has no text
     */
    public static String deriveUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return UNKNOWN;
        }
        String lowerCased = userAgent.toLowerCase();
        return BROWSER_SIGNATURES.stream()
                .filter(entry -> lowerCased.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("Other");
    }

    /**
     * Extracts the host component from a Referer header value, discarding path,
     * query, and scheme information.
     * <p>
     * Returns {@link #DIRECT} when no referer was supplied (i.e. {@code ref} is
     * {@code null} or blank), and {Constants#UNKNOWN} when the value cannot be parsed
     * as a URI or has no identifiable host component (e.g. a relative reference).
     *
     * @param ref the raw Referer header value; may be {@code null} or blank
     * @return the host portion of {@code ref}, {@link #DIRECT} if {@code ref} has
     *         no text, or {Constants#UNKNOWN} if the host cannot be determined
     */
    public static String deriveHost(String ref) {
        if (!StringUtils.hasText(ref)) {
            return DIRECT;
        }
        try {
            String host = URI.create(ref).getHost();
            return !StringUtils.hasText(host) ? UNKNOWN : host;
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
