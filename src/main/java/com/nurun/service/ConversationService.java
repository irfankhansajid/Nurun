package com.nurun.service;

import com.nurun.dto.ConversationSummaryDto;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.repository.ConversationRepository;
import com.nurun.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

}
