package com.nurun.controller;

import com.nurun.dto.MessageRequestDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> startNewChat(@RequestBody MessageRequestDto request) {
        return ResponseEntity.ok(
                messageService.createMessage(request, null)
        );
    }

    @PostMapping("/{conversationId}")
    public ResponseEntity<MessageResponseDto> replayChat(@RequestBody MessageRequestDto request, @PathVariable Long conversationId) {
        return ResponseEntity.ok(
                messageService.createMessage(request, conversationId)
        );
    }




}