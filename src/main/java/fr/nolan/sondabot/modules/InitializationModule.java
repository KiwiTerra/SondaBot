package fr.nolan.sondabot.modules;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.commands.PollCommand;
import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.commands.privatemessage.*;
import fr.nolan.sondabot.config.Options;
import fr.nolan.sondabot.jda.JDAManager;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import org.apache.commons.configuration2.Configuration;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class InitializationModule extends SondaModule {

    public InitializationModule() {
        super("initialization_module");
    }

    @Override
    public void onReady(ReadyEvent event) {
        SondaBot.getThreadPool().submit(this::ready);
    }

    private void ready() {
        ShardManager client = JDAManager.getClient();
        Configuration configuration = SondaBot.getInstance().getProperties();

        String guildID = configuration.getString("guild"),
                pollChannelID = configuration.getString("pollChannel"),
                verifyPollChannelID = configuration.getString("pollVerifyChannel");

        if (guildID == null || pollChannelID == null || verifyPollChannelID == null) {
            SondaBot.getLogger().error("Id of the " +
                    (guildID == null ? "guild" : (pollChannelID == null ? "#sondages" : "#sondages-verif"))
                    + " cannot be null");
            client.shutdown();
            return;
        }

        Guild guild = client.getGuildById(guildID);
        if (guild == null) {
            SondaBot.getLogger().error("Guild cannot be found !");
            client.shutdown();
            return;
        }

        TextChannel pollChannel = guild.getTextChannelById(pollChannelID),
                verifyPollChannel = guild.getTextChannelById(verifyPollChannelID);
        if (pollChannel == null || verifyPollChannel == null) {
            SondaBot.getLogger().error("Channel " + (pollChannel == null ? "#sondages" : "#sondages-verif") + " cannot be found !");
            client.shutdown();
            return;
        }

        String prefix = configuration.getString("prefix");
        if (prefix == null || prefix.isEmpty()) {
            SondaBot.getLogger().error("Prefix cannot be " + (prefix == null ? "null" : "empty"));
            client.shutdown();
            return;
        }

        Options options = new Options(prefix, guild, pollChannel, verifyPollChannel);
        SondaBot.getInstance().setOptions(options);

        // Initialize Modules
        new GuildMessageModule();
        new PrivateMessageModule();

        // Initialize Commands
        new PollCommand();

        // Initialize Commands from PM
        new AddCommand();
        new AskCommand();
        new ColorCommand();
        new EndCommand();
        new ReactCommand();
        new CancelCommand();

        Reflections reflections = new Reflections("fr.nolan.sondabot.commands");
        Set<Class<? extends SondaCommand>> allClasses = reflections.getSubTypesOf(SondaCommand.class);

        for (Class<? extends SondaCommand> command : allClasses) {
            try {
                command.getConstructors()[0].newInstance();
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                SondaBot.getLogger().info("Error when initialization of " + command.getSimpleName());
            }
        }

    }
}
