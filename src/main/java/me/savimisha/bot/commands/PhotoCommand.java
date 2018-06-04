package me.savimisha.bot.commands;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.impl.FileApi;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import me.savimisha.Config;
import me.savimisha.utils.DataBase;
import me.savimisha.utils.ImageSaver;
import me.savimisha.bot.Bot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class PhotoCommand extends BotCommand {

    private static final String TAG = PhotoCommand.class.getSimpleName();

    public PhotoCommand() {
        super("photo");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, final Integer date) {
        if (chat.type().equals(Chat.Type.Private))
            return;
        final Drive drive;
        final String folderIdFinal;
        try {
            InputStream in = PhotoCommand.class.getResourceAsStream(Config.OAUTH_SECRET_RESOURCE_PATH);
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(in));
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            java.io.File credentialsFile = new java.io.File(System.getProperty("user.home"), Config.CREDENTIALS_PATH);
            DataStoreFactory dataStoreFactory = new FileDataStoreFactory(credentialsFile);
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JacksonFactory.getDefaultInstance(), clientSecrets, Arrays.asList(DriveScopes.DRIVE))
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
            drive = new Drive.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                    .build();
            String folderId = DataBase.getFolderId(chat.id());
            if (folderId != null) {
                try {
                    drive.files().get(folderId).execute();
                } catch (Exception e) {
                    DataBase.error(TAG, "Could not find folder", e);
                    folderId = null;
                }
            }
            if (folderId == null) {
                File fileMetadata = new File();
                String folderName = (chat.type().equals(Chat.Type.Private) ? user.username() : chat.title()) + " PHOTO";
                fileMetadata.setName(folderName);
                fileMetadata.setMimeType("application/vnd.google-apps.folder");
                File file = drive.files().create(fileMetadata).execute();
                folderId = file.getId();
                DataBase.updateFolderId(chat.id(), folderId);
                bot.sendMessage(chat.id(), "Создана новая папка для фото \"" + folderName + "\"");
            }
            folderIdFinal = folderId;

        } catch (Exception e) {
            DataBase.error(TAG, "", e);
            return;
        }
        if (photos[photos.length - 1] != null) {
            GetFile getFile = new GetFile(photos[photos.length - 1].fileId());
            bot.getApi().execute(getFile, new Callback<GetFile, GetFileResponse>() {
                @Override
                public void onResponse(GetFile getFile, GetFileResponse getFileResponse) {
                    if (getFileResponse.isOk()) {
                        FileApi fileApi = new FileApi(Config.BOT_TOKEN);
                        ImageSaver.save(fileApi.getFullFilePath(getFileResponse.file().filePath()), new ImageSaver.Callback() {
                            @Override
                            public void onSuccess(java.io.File file) {
                                try {
                                    Date dateTime = new Date();
                                    dateTime.setTime(date.longValue() * 1000);
                                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss_dd.MM.yyyy");
                                    Random random = new Random(System.currentTimeMillis());
                                    File fileMetadata = new File();
                                    fileMetadata.setName(df.format(dateTime) + "_" + String.valueOf(random.nextLong()));
                                    fileMetadata.setParents(Collections.singletonList(folderIdFinal));
                                    FileContent fileContent = new FileContent(URLConnection.guessContentTypeFromName(file.getName()), file);
                                    drive.files().create(fileMetadata, fileContent)
                                            .setFields("id, parents")
                                            .execute();
                                } catch (Exception e) {
                                    DataBase.error(TAG, "", e);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                DataBase.error(TAG, "Could not save file.", e);
                            }
                        });


                    } else {
                        DataBase.error(TAG, "GetFile failed. Status = " + getFileResponse.errorCode());
                    }
                }

                @Override
                public void onFailure(GetFile getFile, IOException e) {
                    DataBase.error(TAG, "GetFile failed.", e);
                }
            });
        }

    }
}
