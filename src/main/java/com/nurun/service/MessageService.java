package com.nurun.service;

import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Message;
import com.nurun.model.User;
import com.nurun.repository.UserRepository;
import com.nurun.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final UserPrincipal userPrincipal;
    private final UserRepository userRepository;

    public MessageService(UserPrincipal userPrincipal, UserRepository userRepository) {
        this.userPrincipal = userPrincipal;
        this.userRepository = userRepository;
    }

    public Message createMessage() {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));


    }
}
