package me.savimisha.bot.commands;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import com.vdurmont.emoji.EmojiParser;
import me.savimisha.Config;
import me.savimisha.utils.DataBase;
import me.savimisha.bot.Bot;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TextCommand extends BotCommand {

    private static final String TAG = TextCommand.class.getSimpleName();

    public TextCommand() {
        super("call");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, Integer date) {
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


            Credential credential = flow.loadCredential(String.valueOf(DataBase.getBotUserId(chat.id())));
            if (credential == null) {
                DataBase.info(TAG, "Could not load credential.");
                bot.sendMessage(chat.id(), "<b>Ошибка.</b> Нет доступа к Google Drive. Разреши доступ через личные сообщения.");
                return;
            }
            if (credential.getExpiresInSeconds() <= 60 && !credential.refreshToken()) {
                DataBase.error(TAG, "Could not refresh token");
                bot.sendMessage(chat.id(), "<b>Ошибка.</b> Нет доступа к Google Drive. Разреши доступ через личные сообщения.");
                return;
            }
            Sheets sheets = new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                    .build();

            String spreadsheetId = DataBase.getSheetId(chat.id());
            if (spreadsheetId != null) {
                try {
                    sheets.spreadsheets().values().get(spreadsheetId, "A1:A1").execute();
                } catch (Exception e) {
                    DataBase.info(TAG, "Spreadsheet not found. Creating new.");
                    spreadsheetId = null;
                }
            }
            if (spreadsheetId == null) {
                Spreadsheet spreadsheet = new Spreadsheet();
                String spreadsheetTitle = (chat.type().equals(Chat.Type.Private) ? user.username() : chat.title()) + " LOG";
                spreadsheet.setProperties(new SpreadsheetProperties().setTitle(spreadsheetTitle));
                spreadsheet = sheets.spreadsheets().create(spreadsheet).execute();
                DataBase.updateSheetId(chat.id(), spreadsheet.getSpreadsheetId());
                spreadsheetId = spreadsheet.getSpreadsheetId();
                bot.sendMessage(chat.id(), "Создан новый документ \"" + spreadsheetTitle + "\"");
            }

            int lastCell = DataBase.getLastCell(chat.id());

            List<List<Object>> writeData = new ArrayList<>();
            List<Object> dataRow = new ArrayList<>();
            StringBuilder userInfo = new StringBuilder();
            /*if (user.firstName() != null && !user.firstName().isEmpty()) {
                userInfo.append(user.firstName());
                userInfo.append(" ");
            }
            if (user.lastName() != null && !user.lastName().isEmpty()) {
                userInfo.append(user.lastName());
                userInfo.append(" ");
            }*/
            if (user.username() != null && !user.username().isEmpty()) {
                userInfo.append(user.username());
            }
            dataRow.add(userInfo.toString());
            dataRow.add(EmojiParser.parseToAliases(arguments[0]));
            Date dateTime = new Date();
            dateTime.setTime(date.longValue() * 1000);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
            dataRow.add(df.format(dateTime));
            writeData.add(dataRow);

            ValueRange vr = new ValueRange().setValues(writeData).setMajorDimension("ROWS");

            sheets.spreadsheets().values()
                    .update(spreadsheetId, "A" + lastCell + ":C" + lastCell, vr)
                    .setValueInputOption("RAW")
                    .execute();
            DataBase.updateLastCell(chat.id(), lastCell + 1);

        } catch (Exception e) {
            DataBase.error(TAG, "", e);
        }
    }
}
