package com.nurun.service;

import com.nurun.dto.AiRequest;
import com.nurun.dto.AiResponse;
import com.nurun.dto.MessageRequestDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.enumlist.SelectionMode;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.repository.MessageRepository;
import com.nurun.router.AiRouter;
import com.nurun.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MessageService {


    private final ConversationPersistenceService conversationPersistenceService;

    private final AiRouter aiRouter;

    private  final MessageRepository messageRepository;

    public MessageService(ConversationPersistenceService conversationPersistenceService, AiRouter aiRouter, MessageRepository messageRepository) {
        this.conversationPersistenceService = conversationPersistenceService;
        this.aiRouter = aiRouter;
        this.messageRepository = messageRepository;
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return principal.getId();
    }



    public MessageResponseDto createMessage(MessageRequestDto messageRequestDto, Long conversationIdFromUrl) {

        Long userId = getCurrentUserId();
        // lazy identity mapping

        String content = messageRequestDto.getContent();

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        if (content.length() > 30000) {
            throw new IllegalArgumentException("Message is too long. Please keep it under 30,000 characters.");
        }


        Conversation conversation = conversationPersistenceService.saveUserMessage(messageRequestDto, conversationIdFromUrl, userId);

        List<Message> recentMessages = messageRepository.findTop15ByConversationIdOrderBySentAtDesc(conversation.getId());

        Collections.reverse(recentMessages);

        Message newlySavedMessage = recentMessages.remove(recentMessages.size()-1);

        List<Message> strictHistory = recentMessages;


        AiRequest request = new AiRequest();
        request.setModelName("nurun-auto");
        request.setNewMessage(newlySavedMessage.getContent());
        request.setHistory(strictHistory);
        request.setSummary(conversation.getSummary());
        request.setConversationId(conversation.getId());
        request.setSelectionMode(SelectionMode.AUTO_SELECTED);



        long start = System.currentTimeMillis();

        AiResponse aiResponse = aiRouter.generate(request);


        long end  = System.currentTimeMillis();

        System.out.println("Ai call look "+ (end - start) + " ms");

        Message finalMessage = conversationPersistenceService.saveAiMessage(conversation.getId(), aiResponse);


        return mapToResponseDto(finalMessage);

    }



    private MessageResponseDto mapToResponseDto(Message message) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageRole(message.getMessageRole())
                .userId(message.getConversation().getUser().getId())
                .sentAt(message.getSentAt())
                .modelUsed(message.getModelUsed())
                .providerUsed(message.getProviderUsed())
                .conversationId(message.getConversation().getId())
                .build();
    }
}
