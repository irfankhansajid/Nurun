package com.nurun.controller;

import com.nurun.dto.MessageRequestDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/message")
    public ResponseEntity<MessageResponseDto> sendMessage(@RequestBody MessageRequestDto requestDto) {
        return ResponseEntity.ok(messageService.sendMessageWithAi(requestDto.getContent(), requestDto.getConversationId()));
    }

    @GetMapping("/conversation/{id}/messages")
    public ResponseEntity<List<MessageResponseDto>> getAllMessages(@PathVariable("id") Long conversationId) {
        return ResponseEntity.ok(messageService.getMessage(conversationId));
    }
}
