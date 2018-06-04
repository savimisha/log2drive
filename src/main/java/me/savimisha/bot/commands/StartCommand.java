package me.savimisha.bot.commands;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import me.savimisha.Config;
import me.savimisha.utils.DataBase;
import me.savimisha.bot.Bot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class StartCommand extends BotCommand {
    public static final String TAG = StartCommand.class.getSimpleName();

    public StartCommand() {
        super("start");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, Integer date) {
        if (chat.type().equals(Chat.Type.Private)) {
            GoogleAuthorizationCodeFlow flow;
            try {
                InputStream in = StartCommand.class.getResourceAsStream(Config.OAUTH_SECRET_RESOURCE_PATH);
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JacksonFactory.getDefaultInstance(), clientSecrets, Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE))
                        .setAccessType("offline")
                        .build();

            } catch (Exception e) {
                DataBase.error(TAG, "", e);
                return;
            }
            String url = flow.newAuthorizationUrl().setRedirectUri("urn:ietf:wg:oauth:2.0:oob").build();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton("Дать разрешения");
            inlineKeyboardButton.url(url);
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton[][]{new InlineKeyboardButton[]{inlineKeyboardButton}});
            SendMessage sendMessage = new SendMessage(chat.id(), "Привет! Мне нужны разрешения на доступ к Google Drive. " +
                    "Нужно перейти по ссылке, разрешить доступ, а полученный код отправить мне, с помощью команды /auth_code <code>.");
            sendMessage.replyMarkup(inlineKeyboardMarkup);
            bot.getApi().execute(sendMessage, new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {

                }

                @Override
                public void onFailure(SendMessage sendMessage, IOException e) {
                    DataBase.error(TAG, "SendMessage fail", e);
                }
            });
        }
    }
}
