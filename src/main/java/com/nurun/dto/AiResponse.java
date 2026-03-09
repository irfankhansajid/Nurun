package com.nurun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AiResponse {

    private String content;
    private String modelName;
    private String providerName;
    private boolean fallBackUsed;

}
