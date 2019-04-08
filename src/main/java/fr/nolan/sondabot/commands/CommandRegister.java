package fr.nolan.sondabot.commands;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.jda.JDAManager;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        SondaBot.getThreadPool().submit(() -> onCommand(event.getAuthor(), event.getChannel(), event.getMessage()));
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        SondaBot.getThreadPool().submit(() -> onCommand(event.getAuthor(), event.getChannel(), event.getMessage()));
    }

    private void onCommand(User user, MessageChannel channel, Message message) {
        String contentRaw = message.getContentRaw();
        if (contentRaw.isEmpty() || user.isBot() || contentRaw.length() <= 1)
            return;

        char prefix = contentRaw.charAt(0);
        if (prefix != SondaBot.getInstance().getOptions().getPrefix().charAt(0))
            return;

        String cmd = contentRaw.substring(1);
        String[] args = cmd.split(" ");

        Optional<Poll> poll = Poll.getPollByUser(user);

        listCommands.stream()
                .filter(command -> args[0].equals(command.getCommand()))
                .filter(command -> channel.getType().equals(command.getType()))
                .findFirst()
                .ifPresent(command -> command.execute(user, message, poll, ArrayUtils.remove(args, 0)));

    }

    static CommandRegister getInstance() {
        return instance == null ? new CommandRegister() : instance;
    }
}
