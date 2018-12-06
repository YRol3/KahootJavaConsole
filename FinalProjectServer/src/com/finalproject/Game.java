package com.finalproject;

import java.util.HashMap;

public class Game{
    private User lastUser;
    private int gameState=0;
    private int gameMaxScore;
    private int currentQuestion=-1;
    private int gamePin;
    private int totalAnswers;
    private int currentRightAnswer;
    private Quiz quiz;
    private HashMap<User, Integer> Users;
    public Game(int gamePin, Quiz quiz){
        Users = new HashMap<>();
        this.gamePin = gamePin;
        this.quiz = quiz;
    }

    public HashMap<User, Integer> getUsers() {
        return Users;
    }

    public void nextGameState(Admin user){
        if(isAdmin(user)){
            if (this.gameState == currentQuestion) {
                this.gameState = 254;
                currentQuestion++;
            } else {
                this.gameState = currentQuestion;
                gameMaxScore = Users.size();
                totalAnswers=0;
            }
        }
    }

    public void setCurrentQuestion(int currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public void printStateAndAnswer(){
        System.out.println(this.gameState + " " + currentQuestion);
    }
    public void setGameState(Admin user, int gameState){
        if(isAdmin(user)){
            this.gameState = gameState;
        }
    }
    public int getCurrentQuestion() {
        return currentQuestion;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setCurrentRightAnswer(int currentRightAnswer) {
        this.currentRightAnswer = currentRightAnswer;
    }

    public int getCurrentRightAnswer() {
        return currentRightAnswer;
    }

    public int getGameState() {
        return gameState;
    }

    public int getGamePin() {
        return gamePin;
    }

    public boolean isAdmin(Admin user){
        return user.getGameName().equals(quiz.quizName) && user.getGamePassword().equals(quiz.quitPassword);
    }



    public boolean addUser(User user) {
        if(gameState == 255){
            Users.put(user,0);
            lastUser = user;
            System.out.println("[Games "+gamePin+"] successfully added user " + user.getUserName());
            return true;
        }
        return false;
    }

    public User getLastUser() {
        return lastUser;
    }

    public int userAnswer(User user, int answer){
        if(Users.containsKey(user)){
            if(answer == currentRightAnswer) {
                int userScore = Users.get(user);
                userScore += gameMaxScore--;
                Users.put(user, userScore);
                return userScore;
            }
        }
        return 0;
    }

    public void endGame() {
        gameState = 253;
    }

    public String getHighestScorePlayer() {
        int tempHighScore = -1;
        User tempUser = new User();
        for(User user: Users.keySet()){
            for (int i = 0; i < Users.size(); i++) {
                if(Users.get(user) >= tempHighScore){
                    tempHighScore = Users.get(user);
                    tempUser = user;
                }
            }
        }
        return tempUser.getUserName();
    }
}
