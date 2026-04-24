package com.bigsmo.controller.service.impl;

import com.bigsmo.controller.config.AgentTokensConfig;
import com.bigsmo.controller.service.TokenValidator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenValidatorImpl implements TokenValidator {
    private final AgentTokensConfig config;
    private final Set<String> tokenHashes = new HashSet<>();

    @Override
    @PostConstruct
    public void init() {
        if (config.getValidTokens() == null) return;
        for (String token : config.getValidTokens()) {
            if (token != null && !token.isBlank()) {
                tokenHashes.add(sha256(token));
            }
        }
        log.info("Agent tokens initialized. Loaded {} hashes into memory.", tokenHashes.size());
    }

    @Override
    public boolean isValid(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String raw = authHeader.substring(7).trim();
        if (raw.isEmpty()) {
            return false;
        }
        return tokenHashes.contains(sha256(raw));
    }

    private String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}