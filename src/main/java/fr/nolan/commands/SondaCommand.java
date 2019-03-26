package fr.nolan.commands;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

abstract class SondaCommand {

    private String command;

    SondaCommand(String command) {
        this.command = command;
        CommandRegister.getInstance().registerCommand(this);
    }

    String getCommand() {
        return command;
    }

    abstract void execute(Member member, TextChannel channel, Message message, String[] args);

}
