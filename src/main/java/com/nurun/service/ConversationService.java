package com.nurun.service;

import com.nurun.dto.ConversationSummaryDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.repository.ConversationRepository;
import com.nurun.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return principal.getId();
    }



    public List<ConversationSummaryDto> getAllConversations() {
        Long userId = getCurrentUserId();
        List<Conversation> conversations = conversationRepository.findByUserId(userId);

        return conversations.stream().map(conversation -> ConversationSummaryDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .createdAt(conversation.getCreatedAt())
                .build())
                .toList();
    }

    @Transactional
    public List<MessageResponseDto> getConversationById(Long conversationId) {
        Long userId = getCurrentUserId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation with id not found " + conversationId));

        if (!userId.equals(conversation.getUser().getId())) {
            throw new RuntimeException("forbidden access");
        }

        return conversation.getMessageList().stream()
                .map(msg -> MessageResponseDto.builder()
                        .id(msg.getId())
                        .content(msg.getContent())
                        .messageRole(msg.getMessageRole())
                        .userId(msg.getConversation().getUser().getId())
                        .sentAt(msg.getSentAt())
                        .modelUsed(msg.getModelUsed())
                        .providerUsed(msg.getProviderUsed()).conversationId(conversationId)
                        .build())
                .toList();
    }

}
