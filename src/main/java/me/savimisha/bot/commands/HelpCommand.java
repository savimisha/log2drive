package me.savimisha.bot.commands;


import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import me.savimisha.bot.Bot;


public class HelpCommand extends BotCommand {

    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(Bot bot, User user, Chat chat, String[] arguments, PhotoSize[] photos, Integer date) {
        bot.sendMessage(chat.id(),
                "/start - начало работы (только в личных сообщениях)\n"+
                        "/auth_code <code> - отправить код, для доступа к Google Drive (только в личных сообщениях)\n" +
                        "/sheet_id <id> - задать id документа (только в чате)\n"+
                        "/folder_id <id> - задать id папки для фото (только в чате)\n" +
                        "/revoke - аннулировать доступ к Google Drive и выйти из всех чатов\n" +
                        "/help - хэлп", null);
    }
}
