package bot;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bot.commands.ModuleManager;
import bot.commands.modules.CommandManager;
import bot.commands.modules.ManageCommands;
import bot.commands.modules.ModCommands;
import bot.commands.modules.UserCommands;
import bot.events.EventListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public static JDA jda;
    public static ModuleManager moduleManager;

    /**
     * Inicia el bot.
     * 
     * Registra los módulos y los comandos de slash.
     * 
     * Activa los módulos por defecto.
     */
    public void start(String token) {
        try {
            logger.info("Iniciando bot...");
            JDABuilder builder = JDABuilder.createDefault(token);
            logger.info("Token válido");
            // Añade todos los intents
            builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
            logger.info("Intents añadidos");
            jda = builder.build();

            moduleManager = new ModuleManager();
            moduleManager.registerModule("mod", new ModCommands());
            moduleManager.registerModule("manage", new ManageCommands());
            moduleManager.registerModule("user", new UserCommands());
            logger.info("Módulos registrados");

            List<EventListener> modules = moduleManager.getModules().values().stream().toList();

            // Registrar los módulos como EventListener
            for (EventListener module : modules) {
                jda.addEventListener(module);
            }
            logger.info("EventListeners registrados");

            // Lista de SlashCommndData
            List<SlashCommandData> slashCommands = new ArrayList<SlashCommandData>();

            // Obtener los comandos de los módulos
            for (EventListener module : modules) {
                if (module instanceof CommandManager) {
                    slashCommands.addAll(((CommandManager) module).getSlash());
                }
            }

            // Registrar los slash commands
            jda.updateCommands()
                    .addCommands(
                            slashCommands)
                    .queue();

            logger.info("Comandos de slash registrados");

            // Activar módulos por defecto
            moduleManager.getModules().forEach((name, module) -> {
                module.setCommandEnabled(true);
            });

            logger.info("Módulos activados");

            // Esperar a que JDA esté listo
            jda.awaitReady();

            logger.info("Bot listo");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
