package fr.nolan.commands;

import fr.nolan.SondaBot;
import fr.nolan.jda.JDAManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CommandRegister extends ListenerAdapter {

    private static CommandRegister instance;
    private static List<SondaCommand> listCommands;

    private CommandRegister() {
        instance = this;
        listCommands = new ArrayList<>();
        JDAManager.getClient().addEventListener(this);
    }

    void registerCommand(SondaCommand command) {
        listCommands.add(command);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        final Member member = event.getMember();
        final String message = event.getMessage().getContentRaw();

        if (message.isEmpty() || member.getUser().isBot() || message.length() <= 1)
            return;

        final char prefix = message.charAt(0);
        if (prefix != SondaBot.getInstance().getOptions().getPrefix().charAt(0))
            return;

        final String cmd = message.substring(1);
        final String[] args = cmd.split(" ");
        listCommands.forEach(command -> {
            if (args[0].equalsIgnoreCase(command.getCommand()))
                command.execute(member, event.getChannel(), event.getMessage(), message.substring(1).split(" "));
        });
    }

    static CommandRegister getInstance() {
        return instance == null ? new CommandRegister() : instance;
    }
}
