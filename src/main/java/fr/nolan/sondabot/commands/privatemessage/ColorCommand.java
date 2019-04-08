package fr.nolan.sondabot.commands.privatemessage;

import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ColorCommand extends SondaCommand {

    public ColorCommand() {
        super("color", ChannelType.PRIVATE);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> optionalPoll, String... args) {
        if (!optionalPoll.isPresent())
            return;
        MessageChannel channel = message.getChannel();

        if (args.length > 0) {
            Color color = getColorByName(args[0]);
            if (color == null) {
                channel.sendMessage("Couleur incorrecte ! Utilisation: .color <RED | CYAN | BLUE | ORANGE | BLACK | GREEN | MAGENTA | YELLOW>")
                        .queue(colorUsage -> colorUsage.delete().queueAfter(5, TimeUnit.SECONDS));
                return;
            }

            Poll poll = optionalPoll.get();

            poll.getMessage(channel).queue(pollMessage -> {
                MessageEmbed embed = pollMessage.getEmbeds().get(0);
                EmbedBuilder builder = new EmbedBuilder(embed);

                builder.setColor(color);
                poll.setColor(color);
                pollMessage.editMessage(builder.build()).queue(poll::setMessage);
            });

        } else {
            channel.sendMessage("Utilisation: .color <RED | CYAN | BLUE | ORANGE | BLACK | GREEN | MAGENTA | YELLOW>")
                    .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
        }
    }

    private Color getColorByName(String name) {
        try {
            return (Color) Color.class.getField(name.toUpperCase()).get(null);
        } catch (Exception e) {
            return null;
        }
    }

}
