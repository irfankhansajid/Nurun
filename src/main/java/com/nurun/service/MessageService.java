package com.nurun.service;

import com.nurun.dto.AiRequest;
import com.nurun.dto.AiResponse;
import com.nurun.dto.MessageRequestDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.enumlist.SelectionMode;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.enumlist.MessageRole;
import com.nurun.model.User;
import com.nurun.repository.ConversationRepository;
import com.nurun.repository.UserRepository;
import com.nurun.router.AiRouter;
import com.nurun.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MessageService {


    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    private final AiRouter aiRouter;

    public MessageService(UserRepository userRepository, ConversationRepository conversationRepository, AiRouter aiRouter) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.aiRouter = aiRouter;
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return principal.getId();
    }


    @Transactional
    public MessageResponseDto createMessage(MessageRequestDto messageRequestDto, Long conversationIdFromUrl) {

        Long userId = getCurrentUserId();
        // lazy identity mapping
        User user = userRepository.getReferenceById(userId);

        Conversation conversation;
        if (conversationIdFromUrl == null) {

            conversation = new Conversation();
            conversation.setUser(user);
            conversation.setTitle("Chat " + Instant.now());


        } else {
            conversation = conversationRepository.findById(conversationIdFromUrl)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

            if (!userId.equals(conversation.getUser().getId())) {
                throw new RuntimeException("forbidden access");
            }
        }





        Message userMessage = new Message();
        userMessage.setContent(messageRequestDto.getContent());
        userMessage.setMessageRole(MessageRole.USER);

        List<Message> history = List.copyOf(conversation.getMessageList());


        conversation.addMessage(userMessage);

        conversationRepository.save(conversation);


        AiRequest request = new AiRequest();
        request.setModelName(messageRequestDto.getModelName());
        request.setNewMessage(userMessage.getContent());
        request.setHistory(history);
        request.setConversationId(conversation.getId());
        request.setSelectionMode(SelectionMode.AUTO_SELECTED);


        AiResponse aiResponse = aiRouter.generate(request);

        Message aiMessage = new Message();
        aiMessage.setContent(aiResponse.getContent());
        aiMessage.setMessageRole(MessageRole.ASSISTANT);
        aiMessage.setModelUsed(aiResponse.getModelName());
        aiMessage.setProviderUsed(aiResponse.getProviderName());
        conversation.addMessage(aiMessage);

        Conversation savedConversation = conversationRepository.saveAndFlush(conversation);

        Message persistMessage = savedConversation.getMessageList().get(savedConversation.getMessageList().size() - 1);

        return mapToResponseDto(persistMessage);

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
