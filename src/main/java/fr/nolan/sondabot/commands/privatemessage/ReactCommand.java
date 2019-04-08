package fr.nolan.sondabot.commands.privatemessage;

import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ReactCommand extends SondaCommand {

    public ReactCommand() {
        super("react", ChannelType.PRIVATE);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> optionalPoll, String... args) {
        if (!optionalPoll.isPresent())
            return;

        Poll poll = optionalPoll.get();
        MessageChannel channel = message.getChannel();

        if (args.length < 1) {
            channel.sendMessage("Usage: .react :emote:").queue(reactMessage -> reactMessage.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        String emoteString = args[0];

        Message pollMessage = poll.getMessage(channel).complete();
        int reactionBefore = pollMessage.getReactions().size();
        if (reactionBefore + 1 > poll.getAnswers().size()) {
            channel.sendMessage("Les réactions sont déjà attribués aux choix que vous avez donné.").queue(reactMessage -> reactMessage.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        pollMessage.addReaction(emoteString).complete();

        pollMessage = poll.getMessage(channel).complete();
        List<MessageReaction> emotes = pollMessage.getReactions();
        int reactionAfter = emotes.size();

        if (reactionAfter > reactionBefore) {
            poll.addEmotes(emotes.get(reactionAfter - 1).getReactionEmote().getName());

            MessageEmbed embed = pollMessage.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed);
            builder.setDescription(poll.getDescription());
            pollMessage.editMessage(builder.build()).queue(poll::setMessage);
        }
    }
}
