package com.nurun.service;

import com.nurun.dto.MessageRequestDto;
import com.nurun.dto.MessageResponseDto;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Message;
import com.nurun.model.MessageRole;
import com.nurun.model.User;
import com.nurun.repository.MessageRepository;
import com.nurun.repository.UserRepository;
import com.nurun.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class MessageService {


    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MessageService(UserRepository userRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    private Long getCurrentUserId() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return principal.getId();
    }


    public MessageResponseDto createMessage(MessageRequestDto messageRequestDto) {

        // lazy identity mapping
        User user = userRepository.getReferenceById(getCurrentUserId());

        Message message = new Message();
        message.setContent(messageRequestDto.getContent());
        message.setUser(user);
        message.setMessageRole(MessageRole.USER);

        Message savedMessage = messageRepository.save(message);
        return mapToResponseDto(savedMessage);



    }

    private MessageResponseDto mapToResponseDto(Message message) {
        return MessageResponseDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .messageRole(message.getMessageRole())
                .userId(message.getUser().getId())
                .sentAt(message.getSentAt())
                .build();
    }
}
