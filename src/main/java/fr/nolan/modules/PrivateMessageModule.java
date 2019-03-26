package fr.nolan.modules;

import fr.nolan.SondaBot;
import fr.nolan.config.Options;
import fr.nolan.poll.Poll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PrivateMessageModule extends SondaModule {

    PrivateMessageModule() {
        super("privatemessage_module");
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        SondaBot.getThreadPool().submit(() -> privateMessageReceived(event));
    }

    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        SondaBot.getThreadPool().submit(() -> privateMessageReactionAdd(event));
    }

    private void privateMessageReceived(PrivateMessageReceivedEvent event) {
        final User user = event.getAuthor();
        if (user.isBot())
            return;

        final MessageChannel channel = event.getChannel();
        if (channel.getType() != ChannelType.PRIVATE)
            return;

        Poll poll = Poll.getPollByUser(user);
        if (poll == null || poll.isEnd())
            return;

        final Message message = event.getMessage();
        final String content = message.getContentRaw();
        if (!content.startsWith("."))
            return;

        final String[] contentSplit = content.replaceFirst(".", "").split(" ");
        if (contentSplit.length == 0)
            return;

        final String command = contentSplit[0];

        switch (command) {
            case "ask":
                final String messageWithoutFormat = message.getContentStripped();
                final String[] argsWithoutFormat = messageWithoutFormat.split(" ");

                final StringBuilder stringBuilder = new StringBuilder();
                for (int i = 1; i < argsWithoutFormat.length; i++) {
                    stringBuilder.append(argsWithoutFormat[i]).append(" ");
                }
                final String question = stringBuilder.toString();
                poll.setQuestion(question);

                poll.getMessage(channel).queue(pollMessage -> {
                    final MessageEmbed embed = pollMessage.getEmbeds().get(0);
                    final EmbedBuilder builder = new EmbedBuilder(embed).setTitle(question.length() > 255 ? null : question);

                    builder.setDescription(updateDescription(poll));
                    pollMessage.editMessage(builder.build()).queue(poll::setMessage);
                });

                break;
            case "color":
                if (contentSplit.length >= 2) {
                    final Color color = getColorByName(contentSplit[1]);
                    if (color == null) {
                        channel.sendMessage("Couleur incorrecte ! Usage: .color <RED | CYAN | BLUE | ORANGE | BLACK | GREEN | MAGENTA | YELLOW>")
                                .queue(colorUsage -> colorUsage.delete().queueAfter(5, TimeUnit.SECONDS));
                        return;
                    }

                    poll.getMessage(channel).queue(pollMessage -> {
                        final MessageEmbed embed = pollMessage.getEmbeds().get(0);
                        final EmbedBuilder builder = new EmbedBuilder(embed);

                        builder.setColor(color);
                        poll.setColor(color);
                        pollMessage.editMessage(builder.build()).queue(poll::setMessage);
                    });

                } else
                    channel.sendMessage("Usage: .color <RED | CYAN | BLUE | ORANGE | BLACK | GREEN | MAGENTA | YELLOW>").queueAfter(5, TimeUnit.SECONDS);

                break;
            case "add":
                if (poll.getAnswers().size() >= 20) {
                    channel.sendMessage("La limite de réponses est atteinte !").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }
                final StringBuilder answerBuilder = new StringBuilder();
                for (int i = 1; i < contentSplit.length; i++) {
                    answerBuilder.append(contentSplit[i]).append(" ");
                }

                poll.addAnswer(answerBuilder.toString());

                poll.getMessage(channel).queue(pollMessage -> {
                    final MessageEmbed embed = pollMessage.getEmbeds().get(0);
                    final EmbedBuilder builder = new EmbedBuilder(embed);
                    builder.setDescription(updateDescription(poll));
                    pollMessage.editMessage(builder.build()).queue(poll::setMessage);
                });

                break;
            case "react":
                if (contentSplit.length < 2) {
                    channel.sendMessage("Usage: .react :emote:").queue(reactMessage -> reactMessage.delete().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }

                final String emoteString = contentSplit[1];

                Message pollMessage = poll.getMessage(channel).complete();
                int reactionBefore = pollMessage.getReactions().size();
                if (reactionBefore + 1 > poll.getAnswers().size()) {
                    channel.sendMessage("Les réactions sont déjà attribués aux choix que vous avez donné.").queue(reactMessage -> reactMessage.delete().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }

                pollMessage.addReaction(emoteString).complete();

                pollMessage = poll.getMessage(channel).complete();
                final List<MessageReaction> emotes = pollMessage.getReactions();
                final int reactionAfter = emotes.size();

                if (reactionAfter > reactionBefore) {
                    poll.addEmotes(emotes.get(reactionAfter - 1).getReactionEmote().getName());

                    MessageEmbed embed = pollMessage.getEmbeds().get(0);
                    EmbedBuilder builder = new EmbedBuilder(embed);
                    builder.setDescription(updateDescription(poll));
                    pollMessage.editMessage(builder.build()).queue(poll::setMessage);
                }

                break;
            case "end":
                if (poll.getAnswers().size() >= 2 && poll.getQuestion() != null && !poll.getQuestion().isEmpty()) {
                    end(poll, user, channel, false);
                    SondaBot.getLogger().info("[Send " + user.getAsTag() + "] " + poll.toString());
                }
                break;
            case "cancel":
                cancel(poll, channel);
                break;
        }
    }


    private void privateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        final User user = event.getUser();
        if (user.isBot())
            return;

        final Poll poll = Poll.getPollByUser(user);
        if (poll == null || !poll.isEnd())
            return;

        final PrivateChannel channel = event.getChannel();
        final String emote = event.getReactionEmote().getName();

        if (emote.equals("✅")) {
            final Options options = SondaBot.getInstance().getOptions();
            final Member member = options.getGuild().getMember(user);
            final boolean approved = member.getRoles().stream().anyMatch(role -> role.getName().equals("Pilier de la Commu"));
            end(poll, user, approved ? options.getPollChannel() : options.getVerifyPollChannel(), approved);
            channel.getMessageById(event.getMessageId()).queue(message -> message.delete().queue());

        } else if (emote.equals("❎")) {
            cancel(poll, channel);
        }
    }

    private String updateDescription(Poll poll) {
        final StringBuilder descriptionBuilder = new StringBuilder();
        if (poll.getQuestion() != null && poll.getQuestion().length() > 255) {
            descriptionBuilder.append("**")
                    .append(poll.getQuestion())
                    .append("**")
                    .append("\n\n");

        }
        for (int i = 0; i < poll.getAnswers().size(); i++) {
            descriptionBuilder.append(poll.getEmotes().size() > i ? poll.getEmotes().get(i) : emotes[i])
                    .append(" ")
                    .append(poll.getAnswers().get(i))
                    .append("\n\n");
        }
        return descriptionBuilder.toString();
    }

    private Color getColorByName(String name) {
        try {
            return (Color) Color.class.getField(name.toUpperCase()).get(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void cancel(Poll poll, MessageChannel channel) {
        poll.getMessage(channel).queue(message -> message.delete().queue());
        Poll.delete(poll);
        channel.sendMessage("**Annulation**").queue();
    }

    private void end(Poll poll, User user, MessageChannel channel, boolean approved) {
        poll.setEnd(true);
        final EmbedBuilder builder = new EmbedBuilder()
                .setColor(poll.getColor())
                .setTitle(poll.getQuestion() != null && poll.getQuestion().length() > 255 ? null : poll.getQuestion())
                .setAuthor(user.getName(), user.getAvatarUrl())
                .setFooter(user.getId(), user.getAvatarUrl());

        builder.setDescription(updateDescription(poll));
        channel.sendMessage(builder.build()).queue(message -> {
            if (approved) {
                int reactions = 0;
                if (poll.getEmotes().size() != 0) {
                    poll.getEmotes().forEach(emote -> message.addReaction(emote).queue());
                }

                for (; reactions < poll.getAnswers().size(); reactions++) {
                    message.addReaction(emotes[reactions]).queue();
                }

            } else {
                message.addReaction("\u2705").queue();
                message.addReaction("\u274E").queue();
            }
        });
    }

    private static final String[] emotes = new String[]{"\u0030\u20E3", "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3",
            "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3", "\uD83C\uDDE6",
            "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC",
            "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF"};
}
