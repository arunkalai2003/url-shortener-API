package com.example.shortener.service;

import com.example.shortener.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.net.*;

@Component
public class UrlNormalizer {
    public String normalize(String input) {
        try {
            URI uri = new URI(input.trim());
            String scheme = uri.getScheme() == null ? null : uri.getScheme().toLowerCase();
            if (!"http".equals(scheme) && !"https".equals(scheme))
                throw new BadRequestException("Only http and https URLs are supported");
            String host = uri.getHost();
            if (host == null || host.isBlank()) throw new BadRequestException("URL must contain a valid host");
            host = IDN.toASCII(host.toLowerCase());
            int port = uri.getPort();
            if (("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443)) port = -1;
            String path = uri.getRawPath();
            if (path == null || path.isBlank()) path = "/";
            // Business decision: ALL query parameters and fragments are removed from canonical identity and redirect target.
            return new URI(scheme, null, host, port, path, null, null).normalize().toASCIIString();
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new BadRequestException("Malformed URL");
        }
    }
}
