package com.nurun.utils;

import com.nurun.dto.AiRequest;
import com.nurun.dto.AiResponse;
import com.nurun.exception.ResourceNotFoundException;
import com.nurun.model.Conversation;
import com.nurun.model.Message;
import com.nurun.record.ConversationUpdatedEvent;
import com.nurun.repository.ConversationRepository;
import com.nurun.router.AiRouter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ConversationSummaryWorker {

    private final ConversationRepository conversationRepository;
    private final AiRouter aiRouter;

    public ConversationSummaryWorker(ConversationRepository conversationRepository, AiRouter aiRouter) {
        this.conversationRepository = conversationRepository;
        this.aiRouter = aiRouter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void summarizeConversation(ConversationUpdatedEvent event) {

        Long conversationId = event.conversationId();

        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Background task started: Generating summary for Conversation) " + conversationId);

                Conversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

                List<Message> allMessages = conversation.getMessageList();
                int totalMessage = allMessages.size();

                // Never run if we have 10 or fewer messages.
                if (totalMessage <= 10) {
                    return;
                }


                int startSummaryIndex = Math.max(0, totalMessage - 15);
                int endSummaryIndex = totalMessage - 10;

                if (startSummaryIndex >= endSummaryIndex) {
                    return;
                }

                List<Message> dropMessages = new ArrayList<>(allMessages.subList(startSummaryIndex, endSummaryIndex));

                StringBuilder textToSummary = new StringBuilder();
                if (conversation.getSummary() != null && !conversation.getSummary().isEmpty()) {
                    textToSummary.append("Previous Summary ").append(conversation.getSummary()).append("\n\n");
                }

                textToSummary.append("Recent Message to add to summary:\n");
                for (Message message : dropMessages) {
                    textToSummary.append(message.getMessageRole()).append(": ").append(message.getContent()).append("\n\n");
                }

                AiRequest summaryRequest = new AiRequest();
                summaryRequest.setModelName("gemini-3-flash-preview");
                summaryRequest.setHistory(List.of());
                summaryRequest.setNewMessage("You are a core memory manager for an AI. Your job is to update the 'Old Summary' using the 'New messages'. " +
                        "CRITICAL RULES: " +
                        "1. Keep the final output STRICTLY under 5 sentences. " +
                        "2. Only retain vital facts, user preferences, and main topics. " +
                        "3. DO NOT output a conversational reply. DO NOT include pleasantries. " +
                        "Just output the updated summary text.\n\n" +
                        textToSummary.toString());

                long start = System.currentTimeMillis();
                AiResponse aiResponse = aiRouter.generate(summaryRequest);
                long end = System.currentTimeMillis();
                System.out.println("Ai call look summary " + (end - start) + " ms");

                conversation.setSummary(aiResponse.getContent());
                conversationRepository.save(conversation);

                System.out.println("Background summary updated successfully!");


            } catch (Exception e) {
                System.err.println("Failed to generate background summary: " + e.getMessage());
            }
        });

    }
}
