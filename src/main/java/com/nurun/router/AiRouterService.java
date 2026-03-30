package com.nurun.router;

import com.nurun.dto.AiRequest;
import com.nurun.dto.AiResponse;
import com.nurun.enumlist.SelectionMode;
import com.nurun.exception.AllProvidersFailedException;
import com.nurun.exception.ModelNotSupportedException;
import com.nurun.exception.ModelUnavailableException;
import com.nurun.exception.RateLimitException;
import com.nurun.provider.AiProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiRouterService implements AiRouter{

    private final List<AiProvider> aiProvider;

    public AiRouterService(List<AiProvider> aiProvider) {
        this.aiProvider = aiProvider;
    }


    @Override
    public AiResponse generate(AiRequest request) {

        List<AiProvider> supportingProviders = aiProvider.stream().filter(
                provider -> provider.supports(request.getModelName())).toList();

        if (supportingProviders.isEmpty()) {
            throw new ModelNotSupportedException("No provider supports model: " + request.getModelName());
        }


        // Manual Mode

        if (request.getSelectionMode() == SelectionMode.USER_SELECTED) {
            AiProvider provider = supportingProviders.get(0);

            if (!provider.isAvailable()) {
                throw new ModelUnavailableException("Selected model currently unavailable");
            }
            try {
                String response = provider.generateResponse(
                        request.getHistory(),
                        request.getNewMessage(),
                        request.getSummary()
                );

                return AiResponse.builder()
                        .content(response)
                        .modelName(request.getModelName())
                        .providerName(provider.getProviderName())
                        .fallBackUsed(false)
                        .build();

            } catch (Exception e) {
                throw new RuntimeException("Primary provider failed: " + e.getMessage()); // in manual, it fails means it's fail
            }


        }
        // Auto select logic
        boolean fallbackUsed = false;
        int maxRetriesPerProvider = 1;

        for (AiProvider currentProvider: supportingProviders) {
            if (!currentProvider.isAvailable()) {
                fallbackUsed = true;
                continue;
            }

            for (int attempt = 0; attempt <= maxRetriesPerProvider; attempt++) {


                try {
                    String aiResponse = currentProvider.generateResponse(
                            request.getHistory(),
                            request.getNewMessage(),
                            request.getSummary()
                    );

                    return AiResponse.builder()
                            .content(aiResponse)
                            .modelName(request.getModelName())
                            .providerName(currentProvider.getProviderName())
                            .fallBackUsed(fallbackUsed)
                            .build();

                } catch (RateLimitException e) {
                    System.err.println("Provider " + currentProvider.getProviderName() + " hit Rate Limit.");

                    currentProvider.markUnavailable(); // mark dead
                    break; // break the loop , don't retry , move to the next

                } catch (Exception e) {
                    System.err.println("Attempt " + (attempt + 1) + " failed for " + currentProvider.getProviderName() + ": " + e.getMessage());

                    if (attempt == maxRetriesPerProvider) {
                        break;
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }


            }
            fallbackUsed = true;

        }

        throw new AllProvidersFailedException("All providers failed for model: " + request.getModelName());

        }


    }


