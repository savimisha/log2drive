package me.savimisha.bot.commands;


import java.util.Collection;
import java.util.Map;

/**
 * This Interface represents the gateway for registering and deregistering me.savimisha.commands.
 *
 * @author Timo Schulz (Mit0x2)
 */
public interface ICommandRegistry {


    /**
     * register a command
     *
     * @param botCommand the command to register
     * @return whether the command could be registered, was not already registered
     */
    boolean register(BotCommand botCommand);

    /**
     * register multiple me.savimisha.commands
     *
     * @param botCommands me.savimisha.commands to register
     * @return map with results of the command register per command
     */
    Map<BotCommand, Boolean> registerAll(BotCommand... botCommands);

    /**
     * deregister a command
     *
     * @param botCommand the command to deregister
     * @return whether the command could be deregistered, was registered
     */
    boolean deregister(BotCommand botCommand);

    /**
     * deregister multiple me.savimisha.commands
     *
     * @param botCommands me.savimisha.commands to deregister
     * @return map with results of the command deregistered per command
     */
    Map<BotCommand, Boolean> deregisterAll(BotCommand... botCommands);

    /**
     * getName a collection of all registered me.savimisha.commands
     *
     * @return a collection of registered me.savimisha.commands
     */
    Collection<BotCommand> getRegisteredCommands();

    BotCommand getCommand(String key);

}