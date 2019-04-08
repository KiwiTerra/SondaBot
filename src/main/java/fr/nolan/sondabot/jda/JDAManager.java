package fr.nolan.sondabot.jda;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.config.ConfigurationBuilder;
import fr.nolan.sondabot.modules.InitializationModule;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class JDAManager {

    private static ShardManager client;

    public JDAManager() {
        File file = new File("bot.properties");
        try {
            if (!file.exists())
                FileUtils.copyURLToFile(Objects.requireNonNull(getClass().getClassLoader().getResource("bot.properties")), new File("bot.properties"));
        } catch (IOException ex) {
            // Impossible
        }

        Configuration properties = new ConfigurationBuilder(PropertiesConfiguration.class, file).getConfiguration();
        SondaBot.getInstance().setProperties(properties);

        String token = properties.getString("token");

        if (token == null) {
            SondaBot.getLogger().error("Token cannot be null");
            return;
        }

        client = buildShard(token);

        client.setGame(Game.playing(".poll - V1.5"));
        client.setStatus(OnlineStatus.ONLINE);

        new InitializationModule();

    }

    private ShardManager buildShard(String token) {
        try {
            return new DefaultShardManagerBuilder().setToken(token).setShardsTotal(1).build();
        } catch (LoginException e) {
            SondaBot.getLogger().error(e.getMessage(), e);
            System.exit(-1);
        }
        return null;
    }

    public static ShardManager getClient() {
        return client;
    }

}
