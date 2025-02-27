package bot.commands.modules;

import bot.commands.modules.mod.Ban;
import bot.commands.modules.mod.Kick;
import bot.commands.modules.mod.Mute;
import bot.events.EventListener;

/**
 * Clase que maneja los comandos de moderación del bot.
 * Extiende de {@link EventListener} para manejar eventos de interacción de
 * comandos.
 */
public class ModCommands extends CommandManager {

    /**
     * Constructor de la clase ModCommands.
     * 
     * Agrega los comandos de moderación a la lista de comandos.
     */
    public ModCommands() {
        commands.add(new Ban());
        commands.add(new Kick());
        commands.add(new Mute());
    }

}