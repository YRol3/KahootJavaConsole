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

    /**
     * Creates game object from game pin code and Quiz object
     * @param gamePin the game pin code
     * @param quiz the Quiz object of the game
     */
    public Game(int gamePin, Quiz quiz){
        Users = new HashMap<>();
        this.gamePin = gamePin;
        this.quiz = quiz;
    }

    /**
     * gets the user hash map
     * @return HashMash of Users and Integer that holds scores
     */
    public HashMap<User, Integer> getUsers() {
        return Users;
    }

    /**
     * set the next game state, if the same game state and question, Will go to the results stage,
     * if different will go to question state and reset the game max score bonus for the players
     * @param user the admin object which calls the next game state
     */
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


    /**
     * Sets the game state
     * @param user the admin object which calls the method
     * @param gameState the game state to set
     */
    public void setGameState(Admin user, int gameState){
        if(isAdmin(user)){
            this.gameState = gameState;
        }
    }

    /**
     * checking if the user is an admin of the current game
     * @param user the admin object which you checking for
     * @return true if is an admin, false if not
     */
    public boolean isAdmin(Admin user){
        return user.getGameName().equals(quiz.getQuizName()) && user.getGamePassword().equals(quiz.getQuizPassword());
    }

    /**
     * adds user to the game
     * @param user the user object we are adding to the game
     * @return true if successfully added false if not (if game state is not accepting players)
     */
    public boolean addUser(User user) {
        if(gameState == BEGIN_STATE){
            Users.put(user,0);
            lastUser = user;
            System.out.println("[Games "+gamePin+"] successfully added user " + user.getUserName());
            return true;
        }
        return false;
    }

    /**
     * gets user answer and checks if it correct,
     * Updating the user score accordingly
     * @param user the user submitted the answer
     * @param answer the answer the user submitted
     * @return user score if the answer is correct, 0 if not correct, or not part of the game
     */
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

    /**
     * setting the game state to END_STATE to finish the game for all
     */
    public void endGame() {
        gameState = END_STATE;
    }

   /*
        Regular getters and setters
    */

    public User getLastUser() {
        return lastUser;
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
    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }
}
