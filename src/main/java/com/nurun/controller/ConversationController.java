package com.nurun.controller;

import com.nurun.dto.ConversationSummaryDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.dto.UpdateConversationTitleRequestDto;
import com.nurun.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageResponseDto>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.getConversationById(id));
    }


    @GetMapping
    public ResponseEntity<List<ConversationSummaryDto>> getAllConversation() {
        return ResponseEntity.status(HttpStatus.OK).body(conversationService.getAllConversations());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ConversationSummaryDto> renameConversation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConversationTitleRequestDto requestDto) {
        return ResponseEntity.ok(conversationService.updateConversationTitle(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

}
