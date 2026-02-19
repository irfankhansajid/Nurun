package com.nurun.service;

import com.nurun.dto.MessageResponseDto;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.model.MessageRole;
import com.nurun.repository.ConversationRepository;
import com.nurun.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    public MessageResponseDto createMessage(String content, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            Message createMessage = new Message();

            createMessage.setContent(content);

            createMessage.setMessageRole(MessageRole.USER);
            createMessage.setConversation(conversation);

            Message savedMessage = messageRepository.save(createMessage);
            return MessageResponseDto.builder()
                    .id(savedMessage.getId())
                    .content(savedMessage.getContent())
                    .conversationId(savedMessage.getConversation().getId())
                    .messageRole(savedMessage.getMessageRole())
                    .sentAt(savedMessage.getSentAt())
                    .build();
    }

    public List<MessageResponseDto> getMessage(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        List<Message> messageList = messageRepository.findAllByConversationIdOrderBySentAtAsc(conversationId);

        return messageList.stream().map(msg -> MessageResponseDto.builder()
                        .id(msg.getId())
                        .content(msg.getContent())
                        .conversationId(conversation.getId())
                        .messageRole(msg.getMessageRole())
                        .sentAt(msg.getSentAt())
                        .build()
                ).toList();

    }
}


