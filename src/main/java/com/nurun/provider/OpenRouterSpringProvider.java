package com.nurun.provider;

import com.nurun.enumlist.MessageRole;
import com.nurun.exception.RateLimitException;
import com.nurun.model.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Order(1)
@ConditionalOnProperty(
        prefix = "provider", name = "openrouter-mode", havingValue = "SPRING", matchIfMissing = false
)
public class OpenRouterSpringProvider implements AiProvider{

    private final ChatClient chatClient;

    @Value("${openrouter.api-key:}")
    private String apiKey;

    @Value("${openrouter.default-model:openrouter/free}")
    private String defaultModel;

    @Value("${app.openrouter.http-referer:http://localhost:8080}")
    private String httpReferer;

    @Value("${app.openrouter.x-title:Nurun AI Gateway}")
    private String xTitle;

    private volatile boolean available = true;

    private static final Set<String> SUPPORTED = Set.of(
            "nurun-auto",
            "openrouter/free",
            "openrouter/auto",
            "google/gemma-4-26b-a4b:free",
            "qwen/qwen-3.6-plus:free",
            "meta-llama/llama-3.3-70b-instruct:free"
    );

    public OpenRouterSpringProvider(@Qualifier("OpenRouterChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostConstruct
    void initAvailability() {
        this.available = apiKey != null && !apiKey.isBlank();
    }





    @Override
    public String generateResponse(List<Message> conversationHistory, String newUserMessage, String summary, String modelName) {

        if (!available) {
            throw new RuntimeException("OpenRouter API key not configured");
        }



        String targetModel = resolveModel(modelName);





        List<org.springframework.ai.chat.messages.Message> prompt = buildPrompt(conversationHistory, newUserMessage, summary);

        try {
            String content = chatClient.prompt().messages(prompt)
                    .options(OpenAiChatOptions.builder()
                            .model(targetModel)
                            .httpHeaders(Map.of(
                                    "HTTP-Referer", httpReferer,
                                    "X-Title", xTitle
                            ))
                            .build()
                    )
                    .call()
                    .content();


            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("Empty response from openrouter");
            }
            return content;
        } catch (Exception e) {
            String lower = e.getMessage() == null ? "" :  e.getMessage().toLowerCase();
            if (lower.contains("429")) {
                throw new RateLimitException("OpenRouter rate limit exceeded");
            }
            if (lower.contains("401") || lower.contains("invalid api key")) {
                this.available = false;
                throw new RuntimeException("OpenRouter invalid API key");
            }
            throw new RuntimeException("OpenRouter failed: " + e.getMessage());
        }
    }


    private String resolveModel(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank() || "nurun-auto".equalsIgnoreCase(requestedModel)) {
            return defaultModel;
        }
        String normalized = requestedModel.toLowerCase();
        if (normalized.startsWith("openrouter/")) {
            return requestedModel.substring("openrouter/".length());
        }
        if (SUPPORTED.contains(normalized)) {
            return requestedModel;
        }
        return defaultModel;
    }

    private List<org.springframework.ai.chat.messages.Message> buildPrompt(List<Message> conversationHistory, String newUserMessage, String summary) {

        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();

        StringBuilder sb = new StringBuilder("You are a helpful AI assistant");
        if (summary != null && !summary.trim().isEmpty()) {
            sb.append("\nConversation Summary:\n").append(summary).append("\n");
        }
        messages.add(new SystemMessage(sb.toString()));
        if (conversationHistory != null) {
            for (Message msg : conversationHistory) {
                if (msg == null || msg.getContent() == null) {
                    continue;
                }
                MessageRole role = msg.getMessageRole();
                String content = msg.getContent();
                if (role == MessageRole.USER) {
                    messages.add(new UserMessage(content));
                } else if (role == MessageRole.ASSISTANT) {
                    messages.add(new AssistantMessage(content));
                } else {
                    messages.add(new SystemMessage(content));
                }
            }
        }
        messages.add(new UserMessage(newUserMessage == null ? "" : newUserMessage));
        return messages;
    }

    @Override
    public String getProviderName() {
        return "OpenRouter";
    }

    @Override
    public boolean supports(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            return false;
        }
        String normalized = modelName.toLowerCase();
        return "nurun-auto".equals(normalized)
                || normalized.startsWith("openrouter/")
                || SUPPORTED.contains(normalized)
                || normalized.equals(defaultModel.toLowerCase());
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
                .providerName("OpenRouter")
                .maxTokensPerRequest(128000)
                .maxTokensPerMinute(60000)
                .averageLatencyMs(1200)
                .costScore(2)
                .priority(1)
                .build();
    }
}
