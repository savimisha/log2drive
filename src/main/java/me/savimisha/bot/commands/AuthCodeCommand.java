package me.savimisha.bot.commands;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import me.savimisha.Config;
import me.savimisha.bot.Bot;
import me.savimisha.utils.DataBase;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class AuthCodeCommand extends BotCommand  {

    private static final String TAG = AuthCodeCommand.class.getSimpleName();

    public AuthCodeCommand() {
        super("auth_code");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, Integer date) {
        if (chat.type().equals(Chat.Type.Private)) {
            if (arguments == null || arguments.length == 0){
                bot.sendMessage(chat.id(), "<b>Ошибка.</b> Не задан параметр code.");
                return;
            }
            try {
                InputStream in = TextCommand.class.getResourceAsStream(Config.OAUTH_SECRET_RESOURCE_PATH);
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

                File credentialsFile = new File(System.getProperty("user.home"), Config.CREDENTIALS_PATH);
                HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                DataStoreFactory dataStoreFactory = new FileDataStoreFactory(credentialsFile);

                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport, JacksonFactory.getDefaultInstance(), clientSecrets, Arrays.asList(SheetsScopes.SPREADSHEETS))
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("offline")
                        .build();


                TokenResponse tokenResponse = flow.newTokenRequest(arguments[0]).setRedirectUri("urn:ietf:wg:oauth:2.0:oob").execute();
                if (tokenResponse == null) {
                    DataBase.info(TAG, "Could not receive token.");
                    bot.sendMessage(chat.id(), "Что-то пошло не так. Не удалось аутентифицироваться. Ты точно перешел по той ссылке, разрешил доступ и правильно скопировал код?");
                    return;
                }
                flow.createAndStoreCredential(tokenResponse, String.valueOf(user.id()));
                DataBase.updateBotUserId(chat.id(), user.id());
                bot.sendMessage(chat.id(), "Аутентификация прошла успешно.");
            }catch (Exception e){
                DataBase.error(TAG, "", e);
            }
        } else {
            bot.sendMessage(chat.id(), "Эту комманду можно выполнять только в личных сообщениях.");
        }
    }
}
