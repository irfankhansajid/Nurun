package com.nurun.service;

import com.nurun.dto.MessageRequestDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.enumlist.MessageRole;
import com.nurun.model.User;
import com.nurun.repository.ConversationRepository;
import com.nurun.repository.UserRepository;
import com.nurun.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MessageService {


    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    public MessageService(UserRepository userRepository, ConversationRepository conversationRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return principal.getId();
    }


    @Transactional
    public MessageResponseDto createMessage(MessageRequestDto messageRequestDto) {

        Long userId = getCurrentUserId();
        // lazy identity mapping
        User user = userRepository.getReferenceById(userId);

        Conversation conversation;
        if (messageRequestDto.getConversationId() == null) {

            conversation = new Conversation();
            conversation.setUser(user);
            conversation.setTitle("Chat " + Instant.now());


        } else {
            conversation = conversationRepository.findById(messageRequestDto.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

            if (!conversation.getUser().getId().equals(userId)) {
                throw new RuntimeException("forbidden access");
            }
        }

        Message message = new Message();
        message.setContent(messageRequestDto.getContent());
        message.setMessageRole(MessageRole.USER);

        conversation.addMessage(message);

        conversationRepository.save(conversation);

        return mapToResponseDto(message);

    }

    private MessageResponseDto mapToResponseDto(Message message) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageRole(message.getMessageRole())
                .userId(message.getConversation().getUser().getId())
                .sentAt(message.getSentAt())
                .build();
    }
}
