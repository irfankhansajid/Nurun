package com.nurun.service;

import org.springframework.stereotype.Service;

@Service
public interface AiClient {
    String generateResponse(String userMessage);
}
