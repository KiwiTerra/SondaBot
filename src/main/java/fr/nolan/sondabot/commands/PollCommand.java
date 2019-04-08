package fr.nolan.sondabot.commands;

import fr.nolan.sondabot.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class PollCommand extends SondaCommand {

    public PollCommand() {
        super("poll", ChannelType.TEXT);
    }

    @Override
    public void execute(User user, Message message, Optional<Poll> poll, String... args) {
        Member member = message.getGuild().getMember(user);
        MessageChannel channel = message.getChannel();

        if (!(member.getRoles().stream().anyMatch(role -> role.getName().equals("Sondeur")) ||
                member.hasPermission(Permission.ADMINISTRATOR))) {

            channel.sendMessage(member.getAsMention() + " >> Tu n'as pas la permission")
                    .queue(noPermMessage -> noPermMessage.delete().queueAfter(5, TimeUnit.SECONDS));
            return;
        }

        String text = !channel.getName().equals("commandes") ?
                member.getAsMention() + " >> Vous devez être dans le salon <#551998189071368192> pour faire ceci !" :
                member.getAsMention() + " >> Regardez vos messages privés";

        channel.sendMessage(text).queue(warningMessage -> {
            warningMessage.delete().queueAfter(5, TimeUnit.SECONDS);
            message.delete().queueAfter(5, TimeUnit.SECONDS);
        });

        if (!channel.getName().equals("commandes"))
            return;


        poll.ifPresent(Poll::delete);
        Poll newPoll = new Poll(member);
        Poll.addPoll(newPoll);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(user.getName(), user.getAvatarUrl());
        embedBuilder.setFooter(user.getId(), user.getAvatarUrl());
        embedBuilder.setColor(newPoll.getColor());

        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(embedBuilder.build()).queue(newPoll::setMessage);
            privateChannel.sendMessage("**Changer la couleur:** ``.color <RED | CYAN | BLUE | ORANGE | BLACK | GREEN | MAGENTA | YELLOW>`` \n"
                    + "**Changer la question:** ``.ask <question>`` \n"
                    + "**Ajouter un choix:** ``.add <choix>`` \n"
                    + "**Ajouter une réaction:** ``.react :emote:`` \n"
                    + "**Terminer le sondage:** ``.end`` (2 choix minimums) \n"
                    + "**Annuler le sondage:** ``.cancel``").queue();
        });
    }
}
