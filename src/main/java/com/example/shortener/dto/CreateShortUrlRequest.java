package com.example.shortener.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateShortUrlRequest(

        @NotBlank
        @Size(max = 4096)
        String url,

        @Size(max = 32)
        String customAlias,

        Instant expiresAt,

        @Size(max = 100)
        String campaign
) {}
