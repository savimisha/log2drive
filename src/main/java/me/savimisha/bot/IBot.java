package me.savimisha.bot;

import com.pengrad.telegrambot.model.request.ParseMode;

public interface IBot {
    void sendMessage(long chat_id, String text, ParseMode parseMode);
    void sendMessage(long chat_id, String text);
    void leaveChat(long chat_id);
}
