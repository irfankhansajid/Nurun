package com.nurun.service;

import com.nurun.dto.MessageResponseDto;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.repository.ConversationRepository;
import com.nurun.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageQueryService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public MessageQueryService(MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public Page<MessageResponseDto> getMessages(Long conversationId, Pageable pageable) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(()-> new ResourceNotFoundException("Conversation not found"));

        Long userId = conversation.getUser().getId();

        Page<Message> page = messageRepository.findByConversationIdOrderBySentAtDesc(conversationId, pageable);

        return page.map(message -> mapToResponseDto( message,  userId));
    }

    private MessageResponseDto mapToResponseDto(Message message, Long userId) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageRole(message.getMessageRole())
                .userId(userId)
                .sentAt(message.getSentAt())
                .build();
    }
}
