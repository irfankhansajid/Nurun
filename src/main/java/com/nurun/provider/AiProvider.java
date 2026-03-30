package com.nurun.provider;


import com.nurun.model.Message;

import java.util.List;

public interface AiProvider {
    String generateResponse(List<Message> conversationHistory, String newUserMessage, String summary, String modelName);
    String getProviderName();
    boolean supports(String modelName);
    boolean isAvailable();
    void markUnavailable();
}