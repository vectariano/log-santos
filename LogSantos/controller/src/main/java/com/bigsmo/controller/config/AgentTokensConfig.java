package com.bigsmo.controller.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "log-santos")
public class AgentTokensConfig {
    private List<String> validTokens = new ArrayList<>();
}