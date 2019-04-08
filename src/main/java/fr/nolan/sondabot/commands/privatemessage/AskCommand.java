package fr.nolan.sondabot.commands.privatemessage;

import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.util.Optional;

public class AskCommand extends SondaCommand {

    public AskCommand() {
        super("ask", ChannelType.PRIVATE);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> optionalPoll, String... args) {
        if (!optionalPoll.isPresent())
            return;
        Poll poll = optionalPoll.get();
        MessageChannel channel = message.getChannel();


        String messageWithoutFormat = message.getContentStripped();
        String[] argsWithoutFormat = messageWithoutFormat.split(" ");

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < argsWithoutFormat.length; i++) {
            stringBuilder.append(argsWithoutFormat[i]).append(" ");
        }
        String question = stringBuilder.toString();
        poll.setQuestion(question);

        poll.getMessage(channel).queue(pollMessage -> {
            MessageEmbed embed = pollMessage.getEmbeds().get(0);
            EmbedBuilder builder = new EmbedBuilder(embed).setTitle(question.length() > 255 ? null : question);

            builder.setDescription(poll.getDescription());
            pollMessage.editMessage(builder.build()).queue(poll::setMessage);
        });
    }
}
