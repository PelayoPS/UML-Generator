package bot.commands.modules.management;

import bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Clase que implementa el comando para crear un rol en Discord.
 */
public class CreateRole implements ICommand {

    private String name = "createrole";

    /**
     * Crea un rol en el servidor de Discord.
     * 
     * @param event El evento de interacción del comando.
     */
    public void execute(SlashCommandInteractionEvent event) {
        String name = event.getOption("nombre").getAsString();
        // Crea el rol en el servidor donde se ejecutó el comando.
        event.getGuild().createRole().setName(name).queue();
        // Copia los permisos del rol mencionado.
        if (event.getOption("rol") != null) {
            event.getGuild().getRolesByName(event.getOption("rol").getAsRole().getName(), true).get(0).getPermissions().forEach(perm -> {
                event.getGuild().getRolesByName(name, true).get(0).getManager().givePermissions(perm).queue();
            });
        }

        event.reply(name + " creado correctamente").setEphemeral(true).queue();
    }

    /**
     * Devuelve la información del comando de slash.
     * 
     * @return SlashCommandData La información del comando de slash.
     */
    public SlashCommandData getSlash() {
        SlashCommandData slash = Commands.slash("createrole", "Crea un rol")
                .addOption(OptionType.STRING, "nombre", "El nombre del rol", true)
                .addOption(OptionType.ROLE, "rol", "El rol a copiar los permisos", false);
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
