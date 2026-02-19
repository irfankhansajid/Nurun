package com.nurun.dto;

import com.nurun.model.MessageRole;
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
    private Long conversationId;
    private MessageRole messageRole;
    private Instant sentAt;

}
