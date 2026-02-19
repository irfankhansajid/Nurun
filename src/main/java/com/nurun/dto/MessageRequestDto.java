package com.nurun.dto;

import com.nurun.model.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDto {

    private String content;

    private Long conversationId;

    private MessageRole messageRole;

}
