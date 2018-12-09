package com.finalproject.logic;

import com.finalproject.objects.Admin;
import com.finalproject.objects.Quiz;
import com.finalproject.objects.User;

import java.util.HashMap;

import static com.finalproject.logic.ConnectionMethods.BEGIN_STATE;
import static com.finalproject.logic.ConnectionMethods.END_STATE;
import static com.finalproject.logic.ConnectionMethods.RESULT_STATE;

public class Game{
    private User lastUser;
    private int gameState=0;
    private int gameMaxScoreBonus;
    private int currentQuestion;
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
                this.gameState = RESULT_STATE;
                currentQuestion++;
            } else {
                this.gameState = currentQuestion;
                gameMaxScoreBonus = Users.size();
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
        return user.getGameName().equals(quiz.getQuizName()) && user.getGamePassword().equals(quiz.getQuizPassword());
    }



    public boolean addUser(User user) {
        if(gameState == BEGIN_STATE){
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
                userScore += gameMaxScoreBonus--;
                Users.put(user, userScore);
                return userScore;
            }
        }
        return 0;
    }

    public void endGame() {
        gameState = END_STATE;
    }

}
