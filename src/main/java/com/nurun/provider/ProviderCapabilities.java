package com.nurun.provider;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProviderCapabilities {

    private final String providerName;
    private final int maxTokensPerRequest;
    private final int maxTokensPerMinute;
    private final int averageLatencyMs;

}
