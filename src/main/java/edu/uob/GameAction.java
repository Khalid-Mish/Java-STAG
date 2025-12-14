package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class GameAction {
    private String trigger;
    private List<String> subjects;
    private List<String> consumed;
    private List<String> produced;
    private String narration;

    public GameAction() {
        this.subjects = new ArrayList<>();
        this.consumed = new ArrayList<>();
        this.produced = new ArrayList<>();
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public void setConsumed(List<String> consumed) {
        this.consumed = consumed;
    }

    public void setProduced(List<String> produced) {
        this.produced = produced;
    }

    public void addConsumer(String consumer) {
        this.consumed.add(consumer);
    }

    public void addProduce(String producer) {
        this.produced.add(producer);
    }

    public void addSubject(String subject) {
        this.subjects.add(subject);
    }

    public String getTrigger() {
        return trigger;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<String> getConsumed() {
        return consumed;
    }

    public List<String> getProduced() {
        return produced;
    }

    public String getNarration() {
        return narration;
    }

    public GameAction copy() {
        GameAction copy = new GameAction();
        copy.setNarration(narration);
        copy.setConsumed(consumed);
        copy.setProduced(produced);
        copy.setSubjects(subjects);
        return copy;
    }

    @Override
    public String toString() {
        return "GameAction{" +
                "\ntrigger='" + trigger + '\'' +
                "\nsubjects=" + subjects +
                "\nconsumed=" + consumed +
                "\nproduced=" + produced +
                "\nnarration='" + narration + '\'' +
                "\n}";
    }
}
