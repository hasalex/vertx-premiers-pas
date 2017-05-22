package fr.sewatech.vertx;

import java.util.concurrent.ThreadLocalRandom;

public class Hello {
    private long id;
    private String text;
    private String who;

    public Hello(String text, String who) {
        this(ThreadLocalRandom.current().nextLong(), text, who);
    }

    public Hello(long id, String text, String who) {
        this.id = ThreadLocalRandom.current().nextLong();
        this.text = text;
        this.who = who;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getWho() {
        return who;
    }

    @Override
    public String toString() {
        return id + "-" + who + ":" + text;
    }
}
