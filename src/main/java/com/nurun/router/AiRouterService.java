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

        List<AiProvider> supportingProviders = aiProvider.stream().filter(provider -> provider.supports(request.getModelName())).toList();

        if (supportingProviders.isEmpty()) {
            throw new ModelNotSupportedException("No provider supports model: " + request.getModelName());
        }

        boolean fallbackUsed = false;

        if (request.getSelectionMode() == SelectionMode.USER_SELECTED) {
            AiProvider provider = supportingProviders.get(0);

            if (!provider.isAvailable()) {
                throw new ModelUnavailableException("Selected model currently unavailable");
            }

            String response = provider.generateResponse(
                    request.getHistory(),
                    request.getNewMessage()
            );

            return AiResponse.builder()
                    .content(response)
                    .modelName(request.getModelName())
                    .providerName(provider.getProviderName())
                    .fallBackUsed(false)
                    .build();

        }
        // Auto select logic


        for (AiProvider prov: supportingProviders) {
                if (!prov.isAvailable()) {
                    continue;
                }

                try {
                    String aiResponse = prov.generateResponse(
                            request.getHistory(),
                            request.getNewMessage()
                    );

                    return AiResponse.builder()
                            .content(aiResponse)
                            .modelName(request.getModelName())
                            .providerName(prov.getProviderName())
                            .fallBackUsed(fallbackUsed)
                            .build();
                } catch (RateLimitException e) {
                    prov.markUnavailable();
                    fallbackUsed = true;
                }
            }

            throw new AllProvidersFailedException("All providers failed for model: " + request.getModelName());

        }


    }


