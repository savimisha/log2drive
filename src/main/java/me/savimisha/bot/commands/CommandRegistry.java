package me.savimisha.bot.commands;
import com.pengrad.telegrambot.model.Message;
import me.savimisha.Config;
import me.savimisha.utils.DataBase;
import me.savimisha.bot.Bot;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public final class CommandRegistry implements ICommandRegistry {

    private static final String TAG = CommandRegistry.class.getSimpleName();

    private final Map<String, BotCommand> commandRegistryMap = new HashMap<String, BotCommand>();

    @Override
    public final boolean register(BotCommand botCommand) {
        if (commandRegistryMap.containsKey(botCommand.getCommandIdentifier())) {
            return false;
        }
        commandRegistryMap.put(botCommand.getCommandIdentifier(), botCommand);
        return true;
    }

    @Override
    public final Map<BotCommand, Boolean> registerAll(BotCommand... botCommands) {
        Map<BotCommand, Boolean> resultMap = new HashMap<BotCommand, Boolean>(botCommands.length);
        for (BotCommand botCommand : botCommands) {
            resultMap.put(botCommand, register(botCommand));
        }
        return resultMap;
    }

    @Override
    public final boolean deregister(BotCommand botCommand) {
        if (commandRegistryMap.containsKey(botCommand.getCommandIdentifier())) {
            commandRegistryMap.remove(botCommand.getCommandIdentifier());
            return true;
        }
        return false;
    }

    @Override
    public final Map<BotCommand, Boolean> deregisterAll(BotCommand... botCommands) {
        Map<BotCommand, Boolean> resultMap = new HashMap<BotCommand, Boolean>(botCommands.length);
        for (BotCommand botCommand : botCommands) {
            resultMap.put(botCommand, deregister(botCommand));
        }
        return resultMap;
    }

    @Override
    public final Collection<BotCommand> getRegisteredCommands() {
        return commandRegistryMap.values();
    }

    @Override
    public BotCommand getCommand(String key) {
        if (commandRegistryMap.containsKey(key))
            return commandRegistryMap.get(key);
        else
            return null;
    }


    public final boolean executeCommand(Bot bot, Message message) {
        if (message.photo() != null) {
            commandRegistryMap.get("photo").execute(bot, message.from(), message.chat(), null, message.photo(), message.date());
            return true;
        }
        if (message.text() != null) {
            String text = message.text();
            if (text.startsWith(BotCommand.COMMAND_INIT_CHARACTER)) {
                String commandMessage = text.substring(1);
                String[] commandSplit = commandMessage.split(BotCommand.COMMAND_PARAMETER_SEPARATOR);

                String command = commandSplit[0];
                String fixCommand;
                try {
                    if (command.endsWith(Config.BOT_USERNAME)) {
                        fixCommand = command.substring(0, command.length() - Config.BOT_USERNAME.length() - 1);
                        command = fixCommand;
                    }
                }catch (IndexOutOfBoundsException e){
                    DataBase.info(TAG, "IndexOutOfBoundsException: " + e.getMessage());
                    return false;
                }

                command = command.substring(0, 1).toLowerCase() + command.substring(1);
                if (commandRegistryMap.containsKey(command)) {
                    String[] parameters = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
                    commandRegistryMap.get(command).execute(bot, message.from(), message.chat(), parameters, null, message.date());
                    return true;
                } else {
                    commandRegistryMap.get("call").execute(bot, message.from(), message.chat(), new String[] { message.text().substring(1) }, null, message.date());
                    return true;
                }
            }
        }
        return false;
    }
}