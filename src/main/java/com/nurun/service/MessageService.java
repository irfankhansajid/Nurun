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
    private final AiClient aiClient;

    public MessageService(MessageRepository messageRepository, ConversationRepository conversationRepository, AiClient aiClient) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.aiClient = aiClient;
    }

    public MessageResponseDto createMessage(String content, Conversation conversation, MessageRole role) {

            Message createMessage = new Message();

            createMessage.setContent(content);

            createMessage.setMessageRole(role);
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

    private String buildChatContext(Long conversationId) {
        List<Message> messages = messageRepository.findAllByConversationIdOrderBySentAtAsc(conversationId);
        StringBuilder context = new StringBuilder();
        for (Message msg: messages) {
            String role = msg.getMessageRole() == MessageRole.USER ? "USER" : "AI";
            context.append("[").append(role).append("]: ").append(msg.getContent()).append("\n");
        }
        return context.toString();
    }

    public MessageResponseDto sendMessageWithAi(String content, Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(
                () -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Conversation not found");
        }
        MessageResponseDto saveUserMessage = createMessage(content, conversation, MessageRole.USER);

        String chatContext = buildChatContext(conversationId);

        try {
            String aiTextResponse = aiClient.generateResponse(chatContext);
            MessageResponseDto aiMessage = createMessage(aiTextResponse, conversation, MessageRole.ASSISTANT);
            return aiMessage;
        } catch (Exception ex) {
            throw new RuntimeException("Ai service is unavailable");
        }


    }

    public List<MessageResponseDto> getMessage(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conversation.getUser().getId().equals(userId)) {
            throw new RuntimeException("Conversation not found");
        }

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


