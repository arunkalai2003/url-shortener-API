package com.example.shortener.service;

import com.example.shortener.config.ShortenerProperties;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {
    // Human-readable Base58 alphabet: excludes 0/O/I/l to reduce transcription errors.
    static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private final SecureRandom random = new SecureRandom();
    private final ShortenerProperties properties;

    public ShortCodeGenerator(ShortenerProperties properties) {
        this.properties = properties;
    }

    public String generate() {
        char[] out = new char[properties.codeLength()];
        for (int i = 0; i < out.length; i++) out[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        return new String(out);
    }
}
