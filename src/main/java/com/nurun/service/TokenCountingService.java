package com.nurun.service;

import com.nurun.model.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenCountingService {

    // research show 1 token = 4 character in English
    private static final double CHARS_PER_TOKEN = 4.0;

    //estimate token in single text string
    public int estimateTokens(String text) {

        if (text == null) return 0;
        if (text.trim().isEmpty()) return 0;

        int chatCount = text.length();

        return (int) Math.ceil(chatCount / CHARS_PER_TOKEN);
    }

    // estimate token in conversation history + new message + summary
    public int estimateConversationTokens(List<Message> history, String newMessage, String summary) {

        int totalTokens = 0;

        // count summary token
        if (summary != null && !summary.trim().isEmpty()) {
            totalTokens += estimateTokens(summary);
        }
        // count history token
        if (history != null) {
            for (Message message : history) {
                totalTokens += estimateTokens(message.getContent());
            }
        }

        // count new message token
        totalTokens += estimateTokens(newMessage);

        return totalTokens;
    }




}
