package me.savimisha.utils;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.vdurmont.emoji.EmojiParser;
import me.savimisha.Config;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBase {
    private static Connection connection = null;

    public static void loadAdmins(List<Long> admins) throws SQLException {
        String query = "SELECT * FROM admins";
        PreparedStatement statement = connection().prepareStatement(query);
        ResultSet result = statement.executeQuery();
        admins.clear();
        while (result.next()) {
            admins.add(result.getLong("user_id"));
        }
        statement.close();
        result.close();
    }


    public static void newChatId(long chat_id, long new_chat_id) throws SQLException{
        String query = "UPDATE chats SET chat_id = (?) WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, new_chat_id);
        statement.setLong(2, chat_id);
        statement.execute();
        statement.close();
    }

     public static void update(Update update) throws SQLException {
        String query = "INSERT INTO hooks (from_id, first_name, last_name, username, chat_id, type, title, text, full_update, date)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Message message = update.message();
        if (message == null)
            message = update.editedMessage();
        if (message == null)
            return;

        PreparedStatement statement = connection().prepareStatement(query);
        User from = message.from();
        if (from != null) {
            statement.setLong(1, from.id());
            statement.setString(2, from.firstName() == null ? "" : EmojiParser.parseToAliases(from.firstName()));
            statement.setString(3, from.lastName() == null ? "" : EmojiParser.parseToAliases(from.lastName()));
            statement.setString(4, from.username() == null ? "" : from.username());
        } else {
            statement.setLong(1, 0L);
            statement.setString(2, "");
            statement.setString(3, "");
            statement.setString(4, "");
        }
        Chat chat = message.chat();
        if (chat != null) {
            statement.setLong(5, chat.id());
            statement.setString(6, chat.type().name() == null ? "" : chat.type().name());
            statement.setString(7, chat.title() == null ? "" : EmojiParser.parseToAliases(chat.title()));
        } else {
            statement.setLong(5, 0L);
            statement.setString(6, "");
            statement.setString(7, "");
        }
        statement.setString(8, message.text() != null ? EmojiParser.parseToAliases(message.text()) : "");
        statement.setString(9, EmojiParser.parseToAliases(update.toString()));
        statement.setLong(10, message.date());

        statement.execute();
        statement.close();
    }

    public static boolean containsChat(long chat_id) throws SQLException {
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        boolean contains = result.next();
        statement.close();
        result.close();
        return contains;
    }

    public static void addChat(long chat_id, String name) throws SQLException {
        String query = "INSERT INTO chats (chat_id, title, active) VALUES (?, ?, ?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        statement.setString(2, name);
        statement.setBoolean(3, true);
        statement.execute();
        statement.close();
    }

    public static Integer getCid(Long chat_id) throws SQLException {
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        Integer cid = null;
        if (result.next())
            cid = result.getInt("cid");
        statement.close();
        result.close();
        return cid;
    }

    public static void newChatTitle(long chat_id, String newTitle) throws SQLException {
        String query = "UPDATE chats SET title = (?) WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setString(1, newTitle);
        statement.setLong(2, chat_id);
        statement.execute();
        statement.close();
    }

    public static String getSheetId(long chat_id) throws SQLException{
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        String sheetId = null;
        if (result.next())
            sheetId = result.getString("sheet_id");
        statement.close();
        result.close();
        return sheetId;
    }

    public static void updateSheetId(long chat_id, String sheetId) throws SQLException{
        String query = "UPDATE chats SET sheet_id = (?), last_cell = 1 WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setString(1, sheetId);
        statement.setLong(2, chat_id);
        statement.execute();
        statement.close();
    }

    public static String getFolderId(long chat_id) throws SQLException{
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        String folderId = null;
        if (result.next())
            folderId = result.getString("folder_id");
        statement.close();
        result.close();
        return folderId;
    }

    public static void updateFolderId(long chat_id, String folderId) throws SQLException{
        String query = "UPDATE chats SET folder_id = (?) WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setString(1, folderId);
        statement.setLong(2, chat_id);
        statement.execute();
        statement.close();
    }

    public static int getLastCell(Long chat_id) throws SQLException {
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        int lastCell = 0;
        if (result.next())
            lastCell = result.getInt("last_cell");
        statement.close();
        result.close();
        return lastCell;
    }

    public static void updateLastCell(long chat_id, int lastCell) throws SQLException{
        String query = "UPDATE chats SET last_cell = (?) WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setInt(1, lastCell);
        statement.setLong(2, chat_id);
        statement.execute();
        statement.close();
    }

    public static long getBotUserId(Long chat_id) throws SQLException {
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        long botUserId = 0;
        if (result.next())
            botUserId = result.getLong("bot_user_id");
        statement.close();
        result.close();
        return botUserId;
    }

    public static void updateBotUserId(long chat_id, long botUserId) throws SQLException{
        String query = "UPDATE chats SET bot_user_id = (?) WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, botUserId);
        statement.setLong(2, chat_id);
        statement.execute();
        statement.close();
    }

    public static List<Long> getAllUserChats(long botUserId) throws SQLException{
        String query = "SELECT * FROM chats WHERE bot_user_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, botUserId);
        ResultSet result = statement.executeQuery();
        List<Long> allChats = new ArrayList<>();
        if (result.next())
            allChats.add(result.getLong("chat_id"));
        statement.close();
        result.close();
        return allChats;
    }

    public static void removeBotUserId(long chat_id) throws SQLException{
        String query = "UPDATE chats SET bot_user_id = NULL WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        statement.execute();
        statement.close();
    }



    public static boolean isActiveChat(long chat_id) throws SQLException {
        String query = "SELECT * FROM chats WHERE chat_id = (?)";
        PreparedStatement statement = connection().prepareStatement(query);
        statement.setLong(1, chat_id);
        ResultSet result = statement.executeQuery();
        boolean active = false;
        while (result.next()) {
            active = result.getBoolean("active");
        }
        statement.close();
        result.close();
        return active;
    }



    public static void error(String tag, String message, Throwable e) {
        log(tag, "e", message, e);
    }

    public static void info(String tag, String message, Throwable e) {
        log(tag, "i", message, e);
    }

    public static void error(String tag, String message) {
        log(tag, "e", message);
    }

    public static void info(String tag, String message) {
        log(tag, "i", message);
    }

    private static void log(String tag, String type, String message, Throwable e) {
        try {
            long date = System.currentTimeMillis() / 1000L;
            String query = "INSERT INTO logs (tag, type, message, date)" +
                    " VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection().prepareStatement(query);
            statement.setString(1, tag);
            statement.setString(2, type);
            if (message != null && !message.isEmpty())
                statement.setString(3, message + "\n" + ExceptionUtils.getStackTrace(e));
            else
                statement.setString(3, ExceptionUtils.getStackTrace(e));
            statement.setLong(4, date);

            statement.execute();
            statement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getSimpleName()).log(Level.SEVERE, "SQLException" + ex.getMessage() + "\n" +
                    ExceptionUtils.getStackTrace(ex) + "\n\n" + tag + ":" + type + "\n" + message +
                    ExceptionUtils.getStackTrace(e));
        }
    }

    private static void log(String tag, String type, String message) {
        try {
            long date = System.currentTimeMillis() / 1000L;
            String query = "INSERT INTO logs (tag, type, message, date)" +
                    " VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection().prepareStatement(query);
            statement.setString(1, tag);
            statement.setString(2, type);
            statement.setString(3, message);
            statement.setLong(4, date);
            statement.execute();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getSimpleName()).log(Level.SEVERE, "SQLException" + ex.getMessage() + "\n" +
                    ExceptionUtils.getStackTrace(ex) + "\n\n" + tag + ":" + type + "\n" + message);
        }
    }

    public static Connection connection() throws SQLException {
        if (connection == null || connection.isClosed())
            connect();
        return connection;
    }

    private static void connect() throws SQLException {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        Properties properties = new Properties();
        properties.setProperty("user", Config.DB_USERNAME);
        properties.setProperty("password", Config.DB_PASSWORD);
        properties.setProperty("useUnicode", "true");
        properties.setProperty("characterEncoding", "UTF-8");
        connection = DriverManager.getConnection("jdbc:mysql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME, properties);
    }
}
