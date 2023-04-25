package com.example.clickergame;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private int id;
    private String key;
    private long score;
    private Finals.State myState;
    private boolean visibility;

    public Player(String name, long score, int id) {
        this.name = name;
        this.score = score;
        this.id = id;
        this.myState = Finals.State.ACTIVE;
    }

    public Player() {}

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void increaseScore() {
        this.score++;
    }

    public void decreaseScore() {
        if (this.score > 0)
            this.score--;
    }

    public Player(Player p){
        this.name = p.name;
        this.score = p.score;
        this.id = p.id;
        this.myState = p.myState;
        this.key = p.key;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Finals.State getMyState() {
        return myState;
    }

    public void setMyState(Finals.State myState) {
        this.myState = myState;
    }

    public long compare(Player other) {
        return Long.compare(this.score, other.score);
    }

    public boolean isEqual(Player other) { return other.key.equals(this.key); }

    @Override
    public String toString() {
        return name;
    }
}
