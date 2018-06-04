package me.savimisha.bot.commands;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import me.savimisha.Config;
import me.savimisha.bot.Bot;
import me.savimisha.utils.DataBase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;;


public class RevokeCommand extends BotCommand {

    private static final String TAG = RevokeCommand.class.getSimpleName();

    public RevokeCommand() {
        super("revoke");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, Integer date) {
        try {
            InputStream in = StartCommand.class.getResourceAsStream(Config.OAUTH_SECRET_RESOURCE_PATH);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            java.io.File credentialsFile = new java.io.File(System.getProperty("user.home"), Config.CREDENTIALS_PATH);
            DataStoreFactory dataStoreFactory = new FileDataStoreFactory(credentialsFile);
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JacksonFactory.getDefaultInstance(), clientSecrets, Arrays.asList(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE))
                    .setDataStoreFactory(dataStoreFactory)
                    .setAccessType("offline")
                    .build();
            if (flow.getCredentialDataStore().containsKey(String.valueOf(user.id()))) {
                flow.getCredentialDataStore().delete(String.valueOf(user.id()));
                bot.sendMessage(chat.id(), "Аутентификация аннулирована.");
            } else {
                bot.sendMessage(chat.id(), "У меня и не было доступа к твоему Google Drive.");
            }
        } catch (Exception e) {
            DataBase.error(TAG, "", e);
        }
    }
}
