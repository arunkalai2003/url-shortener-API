package com.example.shortener.service;

import com.example.shortener.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UrlNormalizerTest {
    private final UrlNormalizer n=new UrlNormalizer();
    @Test void removesDefaultPortQueryAndFragment(){
        assertEquals("https://example.com/product",n.normalize("HTTPS://Example.COM:443/product?id=100&utm_source=x#frag"));
    }
    @Test void rootGetsSlash(){ assertEquals("http://example.com/",n.normalize("http://EXAMPLE.com:80")); }
    @Test void rejectsUnsafeSchemes(){ assertThrows(BadRequestException.class,()->n.normalize("javascript:alert(1)")); }
}
