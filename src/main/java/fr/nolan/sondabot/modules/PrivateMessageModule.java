package fr.nolan.sondabot.modules;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.config.Options;
import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;

import java.util.Optional;

public class PrivateMessageModule extends SondaModule {

    PrivateMessageModule() {
        super("privatemessage_module");
    }

    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        SondaBot.getThreadPool().submit(() -> privateMessageReactionAdd(event));
    }

    private void privateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        User user = event.getUser();
        if (user.isBot())
            return;

        Optional<Poll> optionalPoll = Poll.getPollByUser(user);
        if (!optionalPoll.isPresent())
            return;

        Poll poll = optionalPoll.get();

        PrivateChannel channel = event.getChannel();
        String emote = event.getReactionEmote().getName();

        if (emote.equals("✅")) {
            Options options = SondaBot.getInstance().getOptions();
            Member member = options.getGuild().getMember(user);
            boolean approved = member.getRoles().stream().anyMatch(role -> role.getName().equals("Pilier de la Commu"));
            poll.end(user, approved ? options.getPollChannel() : options.getVerifyPollChannel(), approved);
            channel.getMessageById(event.getMessageId()).queue(message -> message.delete().queue());

        } else if (emote.equals("❎")) {
            poll.cancel(channel);
        }
    }
}
