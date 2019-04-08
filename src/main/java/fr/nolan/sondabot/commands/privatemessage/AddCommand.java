package fr.nolan.sondabot.commands.privatemessage;

import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AddCommand extends SondaCommand {

    public AddCommand() {
        super("add", ChannelType.PRIVATE);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> optionalPoll, String... args) {
        if(!optionalPoll.isPresent())
            return;
        Poll poll = optionalPoll.get();
        MessageChannel channel = message.getChannel();

        if (poll.getAnswers().size() >= 20) {
            channel.sendMessage("La limite de réponses est atteinte !").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        String answerBuilder = Arrays.stream(args).map(arg -> arg + " ").collect(Collectors.joining());
        poll.addAnswer(answerBuilder);

        poll.getMessage(channel).queue(pollMessage -> {
            MessageEmbed embed = pollMessage.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed);
            builder.setDescription(poll.getDescription());
            pollMessage.editMessage(builder.build()).queue(poll::setMessage);
        });
    }
}
