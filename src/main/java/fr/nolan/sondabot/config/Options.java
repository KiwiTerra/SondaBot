package fr.nolan.sondabot.config;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

public class Options {

    private final String prefix;
    private final Guild guild;
    private final TextChannel pollChannel, verifyPollChannel;

    public Options(String prefix, Guild guild, TextChannel pollChannel, TextChannel verifyPollChannel) {
        this.prefix = prefix;
        this.guild = guild;
        this.pollChannel = pollChannel;
        this.verifyPollChannel = verifyPollChannel;
    }

    public String getPrefix() {
        return prefix;
    }

    public Guild getGuild() {
        return guild;
    }

    public TextChannel getVerifyPollChannel() {
        return verifyPollChannel;
    }

    public TextChannel getPollChannel() {
        return pollChannel;
    }
}
