package com.nurun.dto;

import com.nurun.enumlist.SelectionMode;
import com.nurun.model.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiRequest {
    private String modelName;
    private List<Message> history;
    private String newMessage;
    private Long conversationId;
    private SelectionMode selectionMode;
}
