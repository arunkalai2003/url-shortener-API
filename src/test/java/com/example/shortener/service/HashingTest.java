package com.example.shortener.service;

import com.example.shortener.util.HashingUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashingTest {
    @Test void sha256IsStableAndFixedWidth(){
        String h= HashingUtil.sha256("https://example.com/");
        assertEquals(64,h.length());
        assertEquals(h, HashingUtil.sha256("https://example.com/"));
    }
}
