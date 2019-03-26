package fr.nolan.modules;

import fr.nolan.SondaBot;
import fr.nolan.emojis.EmojiUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class GuildMessageModule extends SondaModule {

    GuildMessageModule() {
        super("guildmessage_module");
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        SondaBot.getThreadPool().submit(() -> guildMessageReactionAdd(event));
    }

    private void guildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        if (event.getMember().getUser().isBot())
            return;

        final TextChannel channel = event.getChannel();
        if (!channel.getId().equals(SondaBot.getInstance().getOptions().getVerifyPollChannel().getId()))
            return;

        final MessageReaction messageReaction = event.getReaction();
        channel.getMessageById(messageReaction.getMessageId()).queue(message -> {
            if (event.getMember().getUser().isBot())
                return;

            final List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds.size() < 1)
                return;
            final MessageEmbed messageEmbed = embeds.get(0);


            final Member member = event.getMember();
            if (!member.hasPermission(Permission.ADMINISTRATOR) && !member.getUser().getAsTag().equals("Nolan#6423"))
                return;

            final String userId = messageEmbed.getFooter().getText();
            final Member user = SondaBot.getInstance().getOptions().getGuild().getMemberById(userId);

            final MessageReaction.ReactionEmote reactionEmote = messageReaction.getReactionEmote();

            if (!reactionEmote.getName().equals("✅") && !reactionEmote.getName().equals("❎"))
                return;

            final boolean accepted;

            if (reactionEmote.getName().equals("✅")) {
                final EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);
                embedBuilder.setFooter(user.getUser().getName(), messageEmbed.getFooter().getIconUrl());
                embedBuilder.setAuthor(null);

                SondaBot.getInstance().getOptions().getPollChannel().sendMessage(embedBuilder.build()).queue(send -> {
                    MessageEmbed embed = embeds.get(0);
                    String[] choices = embed.getDescription().split("\n");
                    Arrays.stream(choices).map(choice -> choice.split(" "))
                            .filter(cuts -> EmojiUtils.containsEmoji(cuts[0]))
                            .forEach(cuts -> send.addReaction(cuts[0]).queue());
                });
                accepted = true;
            } else
                accepted = false;

            message.clearReactions().queue();
            user.getUser().openPrivateChannel().queue(privateChannel -> {
                final ZonedDateTime creationTime = message.getCreationTime().toZonedDateTime();
                final String time = creationTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                privateChannel.sendMessageFormat("**Votre sondage posté le** %s **a été %s**", time, (accepted ? "accepté" : "refusé")).queue();
                SondaBot.getLogger().info("[Accept : " + member.getUser().getAsTag() + "] > " + user.getUser().getAsTag());
            });

        });
    }

}
