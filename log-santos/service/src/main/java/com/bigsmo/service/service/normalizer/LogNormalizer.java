package com.bigsmo.service.normalizer;

import com.bigsmo.common.dto.NormalizedLogEvent;

public interface LogNormalizer {
    NormalizedLogEvent normalize(String rawMessage);
}