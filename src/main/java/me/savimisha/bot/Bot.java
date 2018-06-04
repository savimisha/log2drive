package me.savimisha.bot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetWebhookInfo;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetWebhookInfoResponse;
import com.pengrad.telegrambot.response.SendResponse;
import me.savimisha.*;
import me.savimisha.bot.commands.*;
import me.savimisha.utils.DataBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;

public class Bot extends TelegramCommandBot implements IBot {

    private static final String TAG = Bot.class.getSimpleName();

    private CommandRegistry commandRegistry;


    public Bot() {
        super(Config.BOT_TOKEN);
        commandRegistry = new CommandRegistry();
        commandRegistry.register(new StartCommand());
        commandRegistry.register(new HelpCommand());
        commandRegistry.register(new TextCommand());
        commandRegistry.register(new SheetIdCommand());
        commandRegistry.register(new FolderIdCommand());
        commandRegistry.register(new PhotoCommand());
        commandRegistry.register(new RevokeCommand());
        commandRegistry.register(new AuthCodeCommand());
    }

    public synchronized void onUpdateReceived(Update update) {
        Message message = update.message();
        if (message == null)
            message = update.editedMessage();
        if (message == null)
            return;
        if (!checkChatAvailability(message.chat()))
            return;

        Boolean groupChatCreated = message.groupChatCreated();

        if ((groupChatCreated != null && groupChatCreated)) {
            try {
                DataBase.updateBotUserId(message.chat().id(), message.from().id());
            }catch (SQLException e){
                DataBase.error(TAG, "", e);
            }
            return;
        }
        User[] newChatMembers = message.newChatMembers();
        if (newChatMembers != null) {
            for (User u: newChatMembers) {
                if (u.isBot() && u.username().equals(Config.BOT_USERNAME)) {
                    try {
                        DataBase.updateBotUserId(message.chat().id(), message.from().id());
                    }catch (SQLException e){
                        DataBase.error(TAG, "", e);
                    }
                    return;
                }
            }
        }

        Long migrateToChatId = message.migrateToChatId();
        if (migrateToChatId != null) {
            try {
                DataBase.newChatId(message.chat().id(), migrateToChatId);
            } catch (SQLException e) {
                DataBase.error(TAG, "", e);
            }
            return;
        }

        String newChatTitle = message.newChatTitle();
        if (newChatTitle != null) {
            try {
                DataBase.newChatTitle(message.chat().id(), newChatTitle);
            } catch (SQLException e) {
                DataBase.error(TAG, "SQLException in newChatTitle", e);
            }
            return;
        }


        try {
            commandRegistry.executeCommand(this, message);
        } catch (Exception e) {
            DataBase.error(TAG, "", e);
        }

    }

    private boolean checkChatAvailability(Chat chat) {
        try {
            if (!DataBase.containsChat(chat.id()))
                DataBase.addChat(chat.id(), chat.title() != null ? chat.title() : chat.firstName() + " " + chat.lastName() + " " + chat.username());
            if (!DataBase.isActiveChat(chat.id())) {
                DataBase.info(TAG, "Message from inactive chat: " + chat.id());
                return false;
            }
        } catch (SQLException e) {
            DataBase.error(TAG, "SQLException in checkChatAvailability:", e);
        }
        return true;
    }

    @Override
    public void sendMessage(long chat_id, String text) {
        SendMessage sendMessage = new SendMessage(chat_id, text);
        sendMessage.parseMode(ParseMode.HTML);
        sendMessage.disableWebPagePreview(true);
        getApi().execute(sendMessage, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                if (!sendResponse.isOk()) {
                    DataBase.error(TAG, "SendMessage failed. error_code = " + sendResponse.errorCode() + ", " + sendResponse.description());
                }
            }

            @Override
            public void onFailure(SendMessage sendMessage, IOException e) {
                DataBase.error(TAG, "SendMessage failure caused by IOException: ", e);
            }
        });
    }

    @Override
    public void leaveChat(long chat_id) {
        LeaveChat leaveChat = new LeaveChat(chat_id);
        getApi().execute(leaveChat, new Callback<LeaveChat, BaseResponse>() {
            @Override
            public void onResponse(LeaveChat leaveChat, BaseResponse baseResponse) {
                if (!baseResponse.isOk()){
                    DataBase.error(TAG, "Could not leaveChat. Status = " + baseResponse.errorCode());
                }
            }

            @Override
            public void onFailure(LeaveChat leaveChat, IOException e) {
                DataBase.error(TAG, "Could not leaveChat.", e);
            }
        });
    }


    @Override
    public void sendMessage(long chat_id, String text, ParseMode parseMode) {

        SendMessage sendMessage = new SendMessage(chat_id, text);
        if (parseMode != null)
            sendMessage.parseMode(parseMode);
        sendMessage.disableWebPagePreview(true);
        getApi().execute(sendMessage, new Callback<SendMessage, SendResponse>() {
            @Override
            public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {
                if (!sendResponse.isOk()) {
                    DataBase.error(TAG, "SendMessage failed. error_code = " + sendResponse.errorCode() + ", " + sendResponse.description());
                }
            }

            @Override
            public void onFailure(SendMessage sendMessage, IOException e) {
                DataBase.error(TAG, "SendMessage failure caused by IOException: ", e);
            }
        });
    }


    Boolean isWebhookSet() {
        GetWebhookInfo getWebhookInfo = new GetWebhookInfo();
        GetWebhookInfoResponse webhookInfoResponse = getApi().execute(getWebhookInfo);
        return webhookInfoResponse.isOk() && webhookInfoResponse.webhookInfo().url().equals(Config.SERVER_URL + Config.BOT_SERVLET_PATH);
    }

    void setWebhook() {
        SetWebhook setWebhook = new SetWebhook();
        setWebhook.url(Config.SERVER_URL + Config.BOT_SERVLET_PATH);
        getApi().execute(setWebhook, new Callback<SetWebhook, BaseResponse>() {
            @Override
            public void onResponse(SetWebhook setWebhook, BaseResponse baseResponse) {
                if (baseResponse.isOk()) {
                    DataBase.info(TAG, "Webhook set");
                } else {
                    DataBase.info(TAG, "Webhook not set: " + baseResponse.errorCode() + ":" + baseResponse.description());
                }
            }

            @Override
            public void onFailure(SetWebhook setWebhook, IOException e) {
                DataBase.info(TAG, "Webhook not set: " + "IOException: " + e.getMessage());
            }
        });
    }


}

