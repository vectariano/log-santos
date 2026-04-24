package com.bigsmo.controller.service;

public interface TokenValidator {
    void init();

    boolean isValid(String authHeader);
}
