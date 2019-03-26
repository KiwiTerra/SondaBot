package fr.nolan.modules;

import fr.nolan.SondaBot;
import fr.nolan.commands.PollCommand;
import fr.nolan.config.Options;
import fr.nolan.jda.JDAManager;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.apache.commons.configuration2.Configuration;

public class InitializationModule extends SondaModule {

    public InitializationModule() {
        super("initialization_module");
    }

    @Override
    public void onReady(ReadyEvent event) {
        SondaBot.getThreadPool().submit(this::ready);
    }

    private void ready() {
        final ShardManager client = JDAManager.getClient();
        final Configuration configuration = SondaBot.getInstance().getProperties();

        final String guildID = configuration.getString("guild"),
                pollChannelID = configuration.getString("pollChannel"),
                verifyPollChannelID = configuration.getString("pollVerifyChannel");

        if (guildID == null || pollChannelID == null || verifyPollChannelID == null) {
            SondaBot.getLogger().error("Id of the " +
                    (guildID == null ? "guild" : (pollChannelID == null ? "#sondages" : "#sondages-verif"))
                    + " cannot be null");
            client.shutdown();
            return;
        }

        final Guild guild = client.getGuildById(guildID);
        if (guild == null) {
            SondaBot.getLogger().error("Guild cannot be found !");
            client.shutdown();
            return;
        }

        final TextChannel pollChannel = guild.getTextChannelById(pollChannelID),
                verifyPollChannel = guild.getTextChannelById(verifyPollChannelID);
        if (pollChannel == null || verifyPollChannel == null) {
            SondaBot.getLogger().error("Channel " + (pollChannel == null ? "#sondages" : "#sondages-verif") + " cannot be found !");
            client.shutdown();
            return;
        }

        final String prefix = configuration.getString("prefix");
        if (prefix == null || prefix.isEmpty()) {
            SondaBot.getLogger().error("Prefix cannot be " + (prefix == null ? "null" : "empty"));
            client.shutdown();
            return;
        }

        final Options options = new Options(prefix, guild, pollChannel, verifyPollChannel);
        SondaBot.getInstance().setOptions(options);

        new GuildMessageModule();
        new PrivateMessageModule();
        new PollCommand();
    }
}
