package com.nurun.service;

import com.nurun.dto.AiResponse;
import com.nurun.dto.MessageRequestDto;
import com.nurun.enumlist.MessageRole;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.model.User;
import com.nurun.record.ConversationUpdatedEvent;
import com.nurun.repository.ConversationRepository;
import com.nurun.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;



@Service
public class ConversationPersistenceService {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConversationPersistenceService(UserRepository userRepository, ConversationRepository conversationRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Conversation saveUserMessage(MessageRequestDto messageRequestDto, Long conversationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Conversation conversation;

        if (conversationId == null) {
            conversation = new Conversation();
            conversation.setUser(user);

            String content = messageRequestDto.getContent();
            String title = content.length() > 50 ? content.substring(0, 50) + "..." : content;
            conversation.setTitle(title);

        } else {
            conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

            if (!userId.equals(conversation.getUser().getId())) {
                throw new RuntimeException("Forbidden access");
            }
        }

        Message userMessage = new Message();

        userMessage.setContent(messageRequestDto.getContent());
        userMessage.setMessageRole(MessageRole.USER);


        conversation.addMessage(userMessage);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public Message saveAiMessage(Long conversationId, AiResponse aiResponse) {

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        Message aiMessage = new Message();
        aiMessage.setContent(aiResponse.getContent());
        aiMessage.setMessageRole(MessageRole.ASSISTANT);
        aiMessage.setModelUsed(aiResponse.getModelName());
        aiMessage.setProviderUsed(aiResponse.getProviderName());

        conversation.addMessage(aiMessage);

        Conversation savedConversation = conversationRepository.saveAndFlush(conversation);

        if (savedConversation.getMessageList().size() > 10 && savedConversation.getMessageList().size() % 5 == 0) {
            eventPublisher.publishEvent(new ConversationUpdatedEvent(conversationId));
        }

        return savedConversation.getMessageList().get(savedConversation.getMessageList().size() - 1);

    }


}
