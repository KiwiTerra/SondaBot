package fr.nolan.modules;

import fr.nolan.SondaBot;
import fr.nolan.jda.JDAManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

abstract class SondaModule extends ListenerAdapter {

    SondaModule(String name) {
        JDAManager.getClient().addEventListener(this);
        SondaBot.getLogger().info("Initialization of " + name);
    }

}
