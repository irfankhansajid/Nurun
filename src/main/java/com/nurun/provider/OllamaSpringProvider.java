package com.nurun.provider;


import com.nurun.exception.RateLimitException;
import com.nurun.model.Message;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Order(4)
@ConditionalOnProperty(
        prefix = "provider",
        name = "ollama-mode",
        havingValue = "SPRING",
        matchIfMissing = false
)
public class OllamaSpringProvider implements AiProvider{


    private final ChatClient chatClient;

    private String defaultModel;

    private volatile boolean available = true;

    public OllamaSpringProvider(@Qualifier("ollamaChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    @Override
    public String generateResponse(List<Message> conversationHistory, String newUserMessage, String summary, String modelName) {

        String prompt = buildPrompt(conversationHistory, newUserMessage, summary);

        try {
            String content = chatClient.prompt(prompt).call().content();
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("Empty response for ollama");
            }
            return content;
        } catch (Exception e) {
            String lower = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (lower.contains("429")) {
                throw new RateLimitException("Ollama rate limit exceeded");
            }
            throw new RuntimeException("Ollama failed: " + e.getMessage());
        }
    }

    private String buildPrompt(List<Message> conversationHistory, String newUserMessage, String summary) {

        StringBuilder sb = new StringBuilder("You are a helpful ai assistant");
        if (summary != null && !summary.trim().isEmpty()) {
            sb.append("\nConversation summary:\n").append(summary).append("\n");
        }

        if (conversationHistory != null) {
            for (Message message : conversationHistory) {
                sb.append(message.getMessageRole()).append(": ").append(message.getContent()).append("\n");
            }
        }
        sb.append("USER: ").append(newUserMessage).append("\nASSISTANT");
        return sb.toString();

    }

    @Override
    public String getProviderName() {
        return "Ollama";
    }

    @Override
    public boolean supports(String modelName) {
        if (modelName == null) return false;
        String normalize = modelName.toLowerCase();
        return normalize.equals("nurun-auto") || normalize.equals(defaultModel.toLowerCase()) || normalize.startsWith("ollama/");
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void markUnavailable() {
        this.available = false;
    }

    @Override
    public ProviderCapabilities getCapabilities() {
        return ProviderCapabilities.builder()
                .providerName("Ollama")
                .maxTokensPerRequest(32000)
                .maxTokensPerMinute(32000)
                .averageLatencyMs(1800)
                .costScore(1)
                .priority(30)
                .build();
    }
}
