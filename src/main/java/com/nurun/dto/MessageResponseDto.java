package com.nurun.dto;

import com.nurun.enumlist.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponseDto {

    private Long id;
    private String content;
    private MessageRole messageRole;
    private Long userId;
    private Instant sentAt;
    private String modelUsed;
    private String providerUsed;

}
