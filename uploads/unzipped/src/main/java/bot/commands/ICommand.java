package bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {

    String name = null;

    void execute(SlashCommandInteractionEvent event);

    SlashCommandData getSlash();

    String getName();

}
