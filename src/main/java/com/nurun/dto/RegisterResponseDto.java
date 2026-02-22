package com.nurun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponseDto {


    private String email;
    private String token;
    private String displayName;
    private String avatarUrl;

    private Instant createdAt;

}
