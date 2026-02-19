package com.nurun.service.serviceImpl;

import com.nurun.service.AiClient;

public class MockClient implements AiClient {
    @Override
    public String generateResponse(String userMessage) {
        return "Ai says "+ userMessage;
    }
}
