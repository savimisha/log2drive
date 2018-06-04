package me.savimisha.bot.commands;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import me.savimisha.utils.DataBase;
import me.savimisha.bot.Bot;

import java.sql.SQLException;


public class FolderIdCommand extends BotCommand {

    private static final String TAG = FolderIdCommand.class.getSimpleName();

    public FolderIdCommand() {
        super("folder_id");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, Integer date) {
        if ((chat.type().equals(Chat.Type.group) || chat.type().equals(Chat.Type.supergroup))) {
            if (arguments != null && arguments.length == 1) {
                try {
                    if (DataBase.containsChat(chat.id()) && DataBase.getBotUserId(chat.id()) == user.id()) {
                        DataBase.updateFolderId(chat.id(), arguments[0]);
                        bot.sendMessage(chat.id(), "<b>Запомнил.</b>");
                    } else {
                        bot.sendMessage(chat.id(), "Задавать папку может только тот, кто пригласил меня в этот чат.");
                        DataBase.error(TAG, "Chat does not contains in chats table.");
                    }
                } catch (SQLException e) {
                    DataBase.error(TAG, "", e);
                }
            } else {
                bot.sendMessage(chat.id(), "<b>Ошибка.</b> Отсутствует параметр.");
            }
        } else {
            bot.sendMessage(chat.id(), "Задавать папку можно только в чате.");
        }
    }
}
