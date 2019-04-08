package fr.nolan.sondabot.poll;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.requests.RestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Poll {

    private static final String[] EMOTES = new String[]{"\u0030\u20E3", "\u0031\u20E3", "\u0032\u20E3", "\u0033\u20E3", "\u0034\u20E3",
            "\u0035\u20E3", "\u0036\u20E3", "\u0037\u20E3", "\u0038\u20E3", "\u0039\u20E3", "\uD83C\uDDE6",
            "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC",
            "\uD83C\uDDED", "\uD83C\uDDEE", "\uD83C\uDDEF"};

    private static final ArrayList<Poll> polls = new ArrayList<>();

    public static Optional<Poll> getPollByUser(User user) {
        return polls.stream().filter(poll -> user.getId().equals(poll.getMember())).findFirst();
    }

    public static void addPoll(Poll poll) {
        polls.add(poll);
    }

    public static void delete(Poll poll) {
        polls.remove(poll);
    }

    private final String member;
    private String question;
    private final List<String> answers;
    private String message;
    private Color color;
    private boolean end;
    private final List<String> emotes;

    public Poll(Member member) {
        this.member = member.getUser().getId();
        this.answers = new ArrayList<>();
        this.color = Color.GRAY;
        this.end = false;
        this.emotes = new ArrayList<>();
    }

    public void end(User user, MessageChannel channel, boolean approved) {
        setEnd(true);
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(getColor())
                .setTitle(getQuestion() != null && getQuestion().length() > 255 ? null : getQuestion())
                .setAuthor(user.getName(), user.getAvatarUrl())
                .setFooter(user.getId(), user.getAvatarUrl());

        builder.setDescription(getDescription());
        channel.sendMessage(builder.build()).queue(message -> {
            if (approved) {
                int reactions = 0;

                for (String emote : getEmotes()) {
                    message.addReaction(emote).queue();
                    reactions++;
                }

                for (; reactions < getAnswers().size(); reactions++) {
                    message.addReaction(EMOTES[reactions]).queue();
                }

            } else {
                message.addReaction("\u2705").queue();
                message.addReaction("\u274E").queue();
            }
        });
    }

    public void cancel(MessageChannel channel) {
        getMessage(channel).queue(message -> message.delete().queue());
        Poll.delete(this);
        channel.sendMessage("**Annulation**").queue();
    }

    public String getDescription() {
        StringBuilder descriptionBuilder = new StringBuilder();
        if (getQuestion() != null && getQuestion().length() > 255) {
            descriptionBuilder.append("**")
                    .append(getQuestion())
                    .append("**")
                    .append("\n\n");

        }
        for (int i = 0; i < getAnswers().size(); i++) {
            descriptionBuilder.append(getEmotes().size() > i ? getEmotes().get(i) : EMOTES[i])
                    .append(" ")
                    .append(getAnswers().get(i))
                    .append("\n\n");
        }
        return descriptionBuilder.toString();
    }

    private String getMember() {
        return member;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void addAnswer(String answer) {
        answers.add(answer);
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public void setMessage(Message message) {
        this.message = message.getId();
    }

    public RestAction<Message> getMessage(MessageChannel channel) {
        return channel.getMessageById(message);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private List<String> getEmotes() {
        return emotes;
    }

    public void addEmotes(String emote) {
        emotes.add(emote);
    }

    @Override
    public String toString() {
        return String.format("Member: %s, Question: %s, Answers: %s, Emotes: %s, isEnd: %s", getMember(), getQuestion(), Arrays.toString(getAnswers().toArray()), Arrays.toString(getEmotes().toArray()), isEnd());
    }
}
