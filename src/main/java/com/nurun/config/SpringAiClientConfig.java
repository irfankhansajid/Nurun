package com.nurun.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiClientConfig {

    @Bean("OpenRouterChatClient")
    public ChatClient openRouterChatClient(

            @Value("${openrouter.api-key:}") String apiKey,
            @Value("${openrouter.base-url:https://openrouter.ai/api}") String baseUrl,
            @Value("${openrouter.default-model:google/openrouter/free}") String defaultModel,
            @Value("${app.openrouter.http-referer:http://localhost:8080}") String httpReferer,
            @Value("${app.openrouter.x-title:Nurun AI Gateway}") String xTitle
    ) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatModel model = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(defaultModel)
                        .httpHeaders(java.util.Map.of(
                                "HTTP-Referer", httpReferer,
                                "X-Title", xTitle
                        ))
                        .build())
                .build();

        return ChatClient.builder(model)
                .build();
    }

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:qwen2.5:0.5b}") String defaultModel
    ) {
        OllamaApi api = OllamaApi.builder()
                .baseUrl(baseUrl)
                .build();
        OllamaChatModel model = OllamaChatModel.builder()
                .ollamaApi(api)
                .defaultOptions(OllamaChatOptions.builder().model(defaultModel).build())
                .build();
        return ChatClient.builder(model).build();
    }

}
