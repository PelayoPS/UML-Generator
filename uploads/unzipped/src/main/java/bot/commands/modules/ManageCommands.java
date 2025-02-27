package bot.commands.modules;

import bot.commands.modules.management.CreateRole;
import bot.commands.modules.management.DeleteRole;
import bot.events.EventListener;

/**
 * Clase que maneja los comandos de gestión del bot.
 * Extiende de {@link EventListener} para manejar eventos de interacción de
 * comandos.
 */
public class ManageCommands extends CommandManager {

    /**
     * Constructor de la clase ManageCommands.
     * 
     * Agrega los comandos de gestión a la lista de comandos.
     */
    public ManageCommands() {
        commands.add(new CreateRole());
        commands.add(new DeleteRole());
    }
}