package com.nurun.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationSummaryDto {
    private Long id;
    private String title;
    private Instant createdAt;
}
