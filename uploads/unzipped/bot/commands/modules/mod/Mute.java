package bot.commands.modules.mod;

import java.time.Duration;

import bot.commands.ICommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Mute implements ICommand{

    private String name = "mute";

    /**
     * Silencia a un usuario.
     * 
     * @param event El evento de interacción del comando.
     */
    public void execute(SlashCommandInteractionEvent event) {
        // Obtiene el usuario a silenciar
        User user = event.getOption("usuario").getAsUser();

        // Obtiene la razón del silencio
        String razon = event.getOption("razon") != null ? event.getOption("razon").getAsString() : "No especificada";

        // Obtiene el tiempo del silencio
        Duration tiempo = parseTime(event.getOption("tiempo").getAsString());

        // Silencia al usuario
        event.getGuild().getMember(user).timeoutFor(tiempo).reason(razon).queue();

        // Responde al usuario
        event.reply("Usuario silenciado correctamente").setEphemeral(true).queue();
    }

    /**
     * Devuelve la información del comando de slash.
     * 
     * @return SlashCommandData La información del comando de slash.
     */
    public SlashCommandData getSlash() {
        SlashCommandData slash = Commands.slash("mute", "Silencia a un usuario")
                .addOption(OptionType.USER, "usuario", "El usuario a silenciar", true)
                .addOption(OptionType.STRING, "razon", "La razón del silencio", false)
                .addOption(OptionType.STRING, "tiempo", "El tiempo del silencio(60s, 5m, 10m, 1h, 2h, 1d, 7d)", true);
        return slash;
    }

    /**
     * Parsea el tiempo del silencio.
     * 60s, 5m, 10m, 1h, 2h, 1d, 7d
     * @param time
     * @return
     */
    private Duration parseTime(String time) {
        switch (time) {
            case "60s":
                return Duration.ofSeconds(60);
            case "5m":
                return Duration.ofMinutes(5);
            case "10m":
                return Duration.ofMinutes(10);
            case "1h":
                return Duration.ofHours(1);
            case "2h":
                return Duration.ofHours(2);
            case "1d":
                return Duration.ofDays(1);
            case "7d":
                return Duration.ofDays(7);
            default:
                return Duration.ofSeconds(60);
        }
    }

    public String getName() {
        return name;
    }

}
