package fr.nolan.poll;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Poll {

    private static ArrayList<Poll> polls = new ArrayList<>();

    public static Poll getPollByUser(User user) {
        Optional<Poll> first = polls.stream().filter(poll -> user.getId().equals(poll.getMember())).findFirst();
        return first.orElse(null);
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

    public List<String> getEmotes() {
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
