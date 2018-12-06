package com.finalproject.Objects;

public class UserAndScore implements Comparable<UserAndScore> {
    private String userName;
    private int score;

    @Override
    public int compareTo(UserAndScore other) {
        if (score < other.score) return 1;
        else if (score > other.score) return -1;
        else return 0;
    }
    public void setScore(int score) {
        this.score = score;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }
}