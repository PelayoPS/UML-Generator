package bot.commands.modules.management;

import bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Clase que implementa el comando para eliminar un rol en Discord.
 */
public class DeleteRole implements ICommand {

    private String name = "deleterole";

    /**
     * Ejecuta el comando para eliminar un rol.
     * 
     * @param event El evento de interacción del comando.
     */
    public void execute(SlashCommandInteractionEvent event) {
        // Comprueba que el rol existe
        if (event.getOption("rol") != null) {
            // Elimina el rol
            event.getOption("rol").getAsRole().delete().queue();
            event.reply("Rol eliminado correctamente").setEphemeral(true).queue();
        } else {
            event.reply("El rol no existe").setEphemeral(true).queue();
        }
    }

    /**
     * Devuelve la información del comando de slash.
     * 
     * @return SlashCommandData La información del comando de slash.
     */
    public SlashCommandData getSlash() {
        SlashCommandData slash = Commands.slash("deleterole", "Elimina un rol")
                .addOption(OptionType.ROLE, "rol", "El rol a eliminar", true);
        return slash;
    }

    /**
     * Devuelve el nombre del comando.
     * 
     * @return String El nombre del comando.
     */
    public String getName() {
        return name;
    }

}
