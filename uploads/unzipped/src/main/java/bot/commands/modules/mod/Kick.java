package bot.commands.modules.mod;

import bot.commands.ICommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Kick implements ICommand {

    private String name = "kick";

    /**
     * Expulsa a un usuario.
     * 
     * @param event El evento de interacción del comando.
     */
    public void execute(SlashCommandInteractionEvent event) {
        // Obtiene el usuario a expulsar
        User user = event.getOption("usuario").getAsUser();

        // Obtiene la razón de la expulsión
        String razon = event.getOption("razon") != null ? event.getOption("razon").getAsString() : "No especificada";

        // Expulsa al usuario
        event.getGuild().kick(user).reason(razon).queue();

        // Responde al usuario
        event.reply("Usuario expulsado correctamente").setEphemeral(true).queue();
    }

    /**
     * Devuelve la información del comando de slash.
     * 
     * @return SlashCommandData La información del comando de slash.
     */
    public SlashCommandData getSlash() {
        SlashCommandData slash = Commands.slash("kick", "Expulsa a un usuario")
                .addOption(OptionType.USER, "usuario", "El usuario a expulsar", true)
                .addOption(OptionType.STRING, "razon", "La razón de la expulsión", false);
        return slash;
    }

    public String getName() {
        return name;
    }

}
