package com.nurun.provider;

import com.nurun.enumlist.MessageRole;
import com.nurun.exception.RateLimitException;
import com.nurun.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GeminiProvider implements AiProvider {

    private volatile boolean available = true;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String url;



    private final RestClient restClient = RestClient.create();

    @Override
    public String generateResponse(List<Message> conversationHistory, String newUserMessage, String summary) {
        try {
            String prompt = buildPrompt(conversationHistory, newUserMessage, summary);

            return callGemini(prompt);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new RateLimitException("Gemini rate limit exceeded");
        }
    }

    private String callGemini(String prompt) {

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        Map response = restClient.post()
                .uri(url + "?key=" + apiKey)
                .body(body)
                .retrieve()
                .onStatus(status -> status.value() == 429, ((request, resp) -> {
                    throw new HttpClientErrorException(resp.getStatusCode(), "Gemini rate limit exceeded");
                }))
                .onStatus(status -> status.isError(), ((request, resp) -> {
                    throw new RuntimeException("Gemini API Error: " + resp.getStatusCode());
                }))
                .body(Map.class);

        if (response == null || !response.containsKey("candidates")) {
            throw new RuntimeException("Empty response from Gemini");
        }

        List candidates = (List) response.get("candidates");
        Map candidate = (Map) candidates.get(0);
        Map content = (Map) candidate.get("content");
        List parts = (List) content.get("parts");
        Map part = (Map) parts.get(0);



        return (String) part.get("text");
    }

    private String buildPrompt(List<Message> strictHistory, String newUserMessage, String summary) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("System: You are a helpful AI assistant. \n");

        if (summary != null && !summary.trim().isEmpty()) {
            prompt.append("System: Here is a summary of the earlier conversation to give you context: [");
            prompt.append(summary);
            prompt.append("]\n");
        }

        for (Message msg: strictHistory) {
            String role = (msg.getMessageRole() == MessageRole.USER) ? "USER" : "ASSISTANT";
            prompt.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        prompt.append("User: ").append(newUserMessage).append("\n");
        prompt.append("Assistant: ");

        System.out.println("Prompt length: " + prompt.length());

        return prompt.toString();
    }

    @Override
    public String getProviderName() {
        return "Gemini";
    }

    private static final Set<String> SUPPORTED_MODELS = Set.of(
            "gemini-3-pro-preview",
            "gemini-3-flash-preview",
            "gemini-2.5-pro",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite-preview"
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
