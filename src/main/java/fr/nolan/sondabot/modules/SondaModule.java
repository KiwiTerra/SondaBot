package fr.nolan.sondabot.modules;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.jda.JDAManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

abstract class SondaModule extends ListenerAdapter {

    SondaModule(String name) {
        JDAManager.getClient().addEventListener(this);
        SondaBot.getLogger().info("Initialization of " + name);
    }

}
