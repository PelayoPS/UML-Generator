package bot.commands;

import java.util.HashMap;
import java.util.Map;

import bot.events.EventListener;

public class ModuleManager {
    private final Map<String, EventListener> modules = new HashMap<>();

    public void registerModule(String name, EventListener module) {
        modules.put(name, module);
    }

    public void enableModule(String name) {
        EventListener module = modules.get(name);
        if (module != null) {
            module.setCommandEnabled(true);
        }
    }

    public void disableModule(String name) {
        EventListener module = modules.get(name);
        if (module != null) {
            module.setCommandEnabled(false);
        }
    }

    public boolean isModuleEnabled(String name) {
        EventListener module = modules.get(name);
        return module.isCommandEnabled();
    }

    public Map<String, EventListener> getModules() {
        return modules;
    }
}