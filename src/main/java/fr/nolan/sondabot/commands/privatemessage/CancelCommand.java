package fr.nolan.sondabot.commands.privatemessage;

import fr.nolan.sondabot.commands.SondaCommand;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Optional;

public class CancelCommand extends SondaCommand {

    public CancelCommand() {
        super("cancel", ChannelType.PRIVATE);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> optionalPoll, String... args) {
        if (!optionalPoll.isPresent())
            return;
        Poll poll = optionalPoll.get();
        poll.cancel(message.getChannel());
    }

}