/**
 * The CommandManager class is responsible for managing slash commands in a Discord bot.
 * It keeps track of the available commands and handles the execution of commands.
 */
package main;

import java.util.HashMap;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import slash_commands.ISlashCommand;
import slash_commands.mods.Shutdown;
import slash_commands.mods.Uptime;
import slash_commands.user.Avatar;
import slash_commands.user.Banner;
import slash_commands.user.Ping;
import slash_commands.user.UserInfo;
import util.logs.ILogger;

/**
 * Class that manages all command related things
 */
public class CommandManager {

    private HashMap<String, ISlashCommand> commandsHashMap = new HashMap<String, ISlashCommand>();
    private ILogger<String> logConsole;

    /**
     * Constructs a new CommandManager object and initializes the available
     * commands.
     */
    public CommandManager(ILogger<String> logConsole) {
        this.logConsole = logConsole;
        // User commands
        commandsHashMap.put("ping", new Ping());
        commandsHashMap.put("avatar", new Avatar());
        commandsHashMap.put("banner", new Banner());
        commandsHashMap.put("userinfo", new UserInfo());

        // Mod commands
        commandsHashMap.put("uptime", new Uptime());

        // Admin commands
        commandsHashMap.put("shutdown", new Shutdown());

    }

    /**
     * Adds the registered commands to the Discord server.
     * 
     * @param jda The JDA instance representing the Discord bot.
     */
    public void addCommands(JDA jda) {
        // !! change this section to jda|guild depending if you want global commands
        // CommandListUpdateAction commands =
        // jda.getGuildById("1215601141077905438").updateCommands();
        CommandListUpdateAction commands = jda.updateCommands();
        commandsHashMap.forEach((name, command) -> {
            // Shows commands added in blue
            logConsole.logEvent("INFO|" + name + " command added.");

            commands.addCommands(command.getSlashInfo());
        });
        commands.queue();
    }

    /**
     * Executes the specified slash command.
     * 
     * @param event The SlashCommandInteractionEvent representing the interaction
     *              event.
     */
    public void executeCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ISlashCommand command = commandsHashMap.get(commandName);
        if (command != null) {
            command.execute(event);
        }
    }

    /**
     * Returns the logger used by the CommandManager.
     * 
     * @return The logger used by the CommandManager.
     */
    public ILogger<String> getLogger() {
        return logConsole;
    }

}
