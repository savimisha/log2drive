package me.savimisha.bot;

import com.pengrad.telegrambot.TelegramBot;

public class TelegramCommandBot {
    private TelegramBot api;
    public TelegramCommandBot(String token){
        this.api = new TelegramBot(token);
    }
    public TelegramBot getApi(){
        return api;
    }
}
