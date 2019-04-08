package fr.nolan.sondabot.modules;

import fr.nolan.sondabot.SondaBot;
import fr.nolan.sondabot.emojis.EmojiUtils;
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

        TextChannel channel = event.getChannel();
        if (!channel.getId().equals(SondaBot.getInstance().getOptions().getVerifyPollChannel().getId()))
            return;

        MessageReaction messageReaction = event.getReaction();
        channel.getMessageById(messageReaction.getMessageId()).queue(message -> {
            if (event.getMember().getUser().isBot())
                return;

            List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds.size() < 1)
                return;
            MessageEmbed messageEmbed = embeds.get(0);


            Member member = event.getMember();
            if (!member.hasPermission(Permission.ADMINISTRATOR) && !member.getUser().getAsTag().equals("Nolan#6423"))
                return;

            String userId = messageEmbed.getFooter().getText();
            Member user = SondaBot.getInstance().getOptions().getGuild().getMemberById(userId);

            MessageReaction.ReactionEmote reactionEmote = messageReaction.getReactionEmote();

            if (!reactionEmote.getName().equals("✅") && !reactionEmote.getName().equals("❎"))
                return;

            final boolean accepted;

            if (reactionEmote.getName().equals("✅")) {
                EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);
                embedBuilder.setFooter(user.getUser().getName(), messageEmbed.getFooter().getIconUrl());
                embedBuilder.setAuthor(null);

                SondaBot.getInstance().getOptions().getPollChannel().sendMessage(embedBuilder.build()).queue(send -> {
                    MessageEmbed embed = embeds.get(0);
                    String[] choices = embed.getDescription().split("\n");

                    for (String choice : choices) {
                        String[] cuts = choice.split(" ");
                        if (EmojiUtils.containsEmoji(cuts[0])) {
                            send.addReaction(cuts[0]).queue();
                        }
                    }
                });
                accepted = true;
            } else
                accepted = false;

            message.clearReactions().queue();
            user.getUser().openPrivateChannel().queue(privateChannel -> {
                ZonedDateTime creationTime = message.getCreationTime().toZonedDateTime();
                String time = creationTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                privateChannel.sendMessageFormat("**Votre sondage posté le** %s **a été %s**", time, (accepted ? "accepté" : "refusé")).queue();
                SondaBot.getLogger().info("[Accept : " + member.getUser().getAsTag() + "] > " + user.getUser().getAsTag());
            });

        });
    }

}
