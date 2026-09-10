package com.example.shortener.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashingTest {
    @Test void sha256IsStableAndFixedWidth(){
        String h=Hashing.sha256("https://example.com/");
        assertEquals(64,h.length());
        assertEquals(h,Hashing.sha256("https://example.com/"));
    }
}
