package fr.nolan.sondabot.commands;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.entities.*;

import java.util.Optional;

public abstract class SondaCommand {

    private String command;
    private ChannelType type;

    public SondaCommand(String command, ChannelType type) {
        this.command = command;
        this.type = type;
        CommandRegister.getInstance().registerCommand(this);
        SondaBot.getLogger().info("Initialization of " + getClass().getSimpleName());
    }

    ChannelType getType() {
        return type;
    }
    String getCommand() {
        return command;
    }

    public abstract void execute(User user, Message message, Optional<Poll> poll, String... args);

}
