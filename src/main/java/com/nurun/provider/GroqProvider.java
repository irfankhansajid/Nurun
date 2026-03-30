package com.nurun.provider;

import com.nurun.enumlist.MessageRole;
import com.nurun.exception.RateLimitException;
import com.nurun.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Order(2)
public class GroqProvider implements AiProvider{


    private volatile boolean available = true;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.url}")
    private String url;

    private final RestClient restClient = RestClient.create();


    @Override
    public String generateResponse(List<Message> conversationHistory, String newUserMessage, String summary, String modelName) {

        try {
            Map<String, Object> requestBody = buildRequestBody(conversationHistory, newUserMessage, summary, modelName);
            return callGroq(requestBody);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Groq API Limit Exceeded");
        }
    }

    private Map<String, Object> buildRequestBody(List<Message> conversationHistory, String newUserMessage, String summary, String modelName) {
        List<Map<String, Object>> messages = new ArrayList<>();

        String actualModel = modelName.equals("nurun-auto") ? "llama-3.1-8b-instant" : modelName;

        StringBuilder systemContent = new StringBuilder("You are a helpful AI assistant.\n");

        if (summary != null &&  !summary.trim().isEmpty()) {
            systemContent.append("\nSystem Core Memory (Context of previous conversation):\n").append(summary);
        }

        messages.add(Map.of("role", "system", "content", systemContent.toString()));

        for (Message msg : conversationHistory) {
            String role = (msg.getMessageRole() == MessageRole.USER) ? "user" : "assistant";
            messages.add(Map.of("role", role, "content", msg.getContent()));
        }

        messages.add(Map.of("role", "user", "content", newUserMessage));

        return Map.of(
                "model", actualModel,
                "messages", messages
        );
    }

    private String callGroq(Map<String, Object> body) {
        Map response = restClient.post()
                .uri(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, ((request, resp) -> {
                    throw new HttpClientErrorException(resp.getStatusCode(), "Groq rate limit exceeded");
                }))
                .onStatus(status -> status.isError(), ((request, resp) -> {
                    String errorBody = new String(resp.getBody().readAllBytes());
                    System.err.println("GROQ EXACT ERROR: " + errorBody); // Print it to your terminal
                    throw new RuntimeException("Groq API Error: " + resp.getStatusCode() + " - " + errorBody);
                }))
                .body(Map.class);

        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("Empty response from Groq");
        }

        List choices = (List) response.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");

        return (String)  message.get("content");

    }


    @Override
    public String getProviderName() {
        return "Groq";
    }

    private static final Set<String> SUPPORTED_MODELS = Set.of(
            "llama-3.1-8b-instant",       // The new Llama 3.1
            "llama-3.3-70b-versatile",    // The massive Llama 3.3
            "mixtral-8x7b-32768",
            "gemma2-9b-it",
            "openai/gpt-oss-120b",        // The custom one you added
            "nurun-auto"
    );

    @Override
    public boolean supports(String modelName) {
        if (modelName == null) return false;
        return SUPPORTED_MODELS.contains(modelName.toLowerCase());
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void markUnavailable() {
        this.available = false;
    }
}
