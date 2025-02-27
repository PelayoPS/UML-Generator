package bot.commands.modules.mod;

import java.util.concurrent.TimeUnit;

import bot.commands.ICommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Ban implements ICommand {

    private String name = "ban";

    /**
     * Banea a un usuario.
     * 
     * @param event El evento de interacción del comando.
     */
    @Override
    public void execute(SlashCommandInteractionEvent event) {

        // Obtiene el usuario a banear
        User user = event.getOption("usuario").getAsUser();

        // Obtiene la razón del baneo
        String razon = event.getOption("razon") != null ? event.getOption("razon").getAsString() : "No especificada";

        // Obtiene los días de historial a borrar
        int tiempo = parseTime(
                event.getOption("borrar_mensajes") != null ? event.getOption("borrar_mensajes").getAsString() : "0");
        // Banea al usuario
        event.getGuild().ban(user, tiempo, TimeUnit.HOURS).reason(razon).queue();

        // Responde al usuario
        event.reply("Usuario baneado correctamente").setEphemeral(true).queue();
    }

    /**
     * Devuelve la información del comando de slash.
     * 
     * borrar_mensajes: Selecciona cuánto historial borrar
     * 1h, 6h, 12h, 24h, 48h, 3d, 7d
     *
     * @return SlashCommandData La información del comando de slash.
     */
    public SlashCommandData getSlash() {
        SlashCommandData slash = Commands.slash("ban", "Banear a un usuario")
                .addOption(OptionType.USER, "usuario", "El usuario a banear", true)
                .addOption(OptionType.STRING, "razon", "La razón del baneo", false)
                .addOption(OptionType.STRING, "borrar_mensajes",
                        "Seleciona cuánto historial borrar ( 1h , 6h, 12h, 24h, 48h , 3d, 7d)",
                        false);
        return slash;
    }

    private int parseTime(String time) {
        switch (time) {
            case "1h":
                return 1;
            case "6h":
                return 6;
            case "12h":
                return 12;
            case "24h":
                return 24;
            case "48h":
                return 48;
            case "3d":
                return 72;
            case "7d":
                return 168;
            default:
                return 0;
        }
    }

    public String getName() {
        return name;
    }

}
