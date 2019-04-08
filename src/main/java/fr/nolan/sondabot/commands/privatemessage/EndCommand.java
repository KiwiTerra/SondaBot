package fr.nolan.sondabot.commands.privatemessage;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Optional;

public class EndCommand extends SondaCommand {

    public EndCommand() {
        super("end", ChannelType.PRIVATE);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> optionalPoll, String... args) {
        if (!optionalPoll.isPresent())
            return;
        Poll poll = optionalPoll.get();


        if (poll.getAnswers().size() >= 2 && poll.getQuestion() != null && !poll.getQuestion().isEmpty()) {
            poll.end(user, message.getChannel(), false);
            SondaBot.getLogger().info("[Send " + user.getAsTag() + "] " + poll.toString());
        }
    }
}
