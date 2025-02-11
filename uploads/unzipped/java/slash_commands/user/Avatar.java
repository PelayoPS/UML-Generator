package slash_commands.user;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import slash_commands.Code;
import slash_commands.ISlashCommand;

/**
 * Represents a slash command for retrieving the avatar of a user.
 */
public class Avatar implements ISlashCommand {

    private final Code code = Code.USER;

    /* =========================SUPER METHODS=============================== */

    /**
     * Retrieves the slash command information.
     *
     * @return The slash command data.
     */
    @Override
    public SlashCommandData getSlashInfo() {
        SlashCommandData result = Commands.slash("avatar",
                "Description: Shows the avatar of the user provided, default user is the caller.");
        addOptions(result);
        return result;
    }

    /**
     * Executes the slash command.
     *
     * @param event The slash command interaction event.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User user = event.getOption("user") == null ? event.getUser()
                : event.getOption("user").getAsUser();
        event.reply(user.getAvatarUrl()+"?format=png&dynamic=true&size=1024").queue();
    }

    /**
     * Retrieves the help information for the slash command.
     *
     * @return A string containing information about how to use the command.
     */
    @Override
    public String getHelp() {
        return "Usage: /avatar [user]\n" +
                "Description: Shows the avatar of the user provided, default user is the caller.";
    }

    /**
     * Retrieves the command code.
     *
     * @return The command code.
     */
    @Override
    public Code getCode() {
        return code;
    }

    /* ========================PRIVATE METHODS=============================== */

    /**
     * Adds options to the slash command.
     *
     * @param slashCommandData The slash command data to add options to.
     */
    private void addOptions(SlashCommandData slashCommandData) {
        slashCommandData.addOption(OptionType.USER, "user",
                "The user to get the avatar from.", false);

    }

}