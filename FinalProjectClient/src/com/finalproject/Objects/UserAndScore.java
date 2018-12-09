package com.finalproject.Objects;


/**
 *  Class that holds user name and score
 */
public class UserAndScore implements Comparable<UserAndScore> {
    private String userName;
    private int score;

    /**
     * compares scores for sorting
     * @param other is anther UserAndScore object to compare with
     * @return int 1 if score is lower than other -1 if higher than other, 0 if equals
     */
    @Override
    public int compareTo(UserAndScore other) {
        if (score < other.score) return 1;
        else if (score > other.score) return -1;
        else return 0;
    }
    /*
        Getters and setters for the class
     */
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