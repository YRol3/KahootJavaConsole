package com.finalproject.logic;

import com.finalproject.Objects.UserAndScore;
import com.finalproject.Objects.Quiz;
import com.finalproject.logic.server.ConnectionHandler;
import com.finalproject.logic.server.ConnectionMethods;
import com.finalproject.Objects.users.User;
import com.finalproject.Objects.users.Admin;
import com.finalproject.interfaces.Writable;
import com.finalproject.logic.server.UserJoinThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


public class GameManager {

    private static Scanner scanner = new Scanner(System.in);
    /**
     * private finals
     */
    private static final int PULL_CURRENT_QUESTION = 144;
    private static final int OKAY = 10;
    private static final int CHECK_IF_NAME_AND_PASSWORD_CORRECT = 98;
    private static final int GAME_START = 97;
    private static final int CREATE_NEW_GAME = 100;
    private static final int TRUE = 0;
    private static final int FALSE = 1;
    private static final int CHECK_IF_GAME_EXISTS = 99;
    private static final int GET_ADMIN_QUESTION_RESULT = 135;
    private static final int PULL_CURRENT_RESULTS = 143;
    private static final int SEND_ANSWER_TO_QUESTION = 133;
    /**
     * public finals
     */
    public static final int ADMIN_STOP_GAME = 159;
    public static final int ADMIN_START_GAME = 104;
    public static final int ADMIN_NEXT_QUESTION = 105;
    public  static final int CREATE_NEW_QUIZ = 100;


    /**
     * gets the game pin from the server with user object
     * and stores the game pin code inside the user
     * @param user can be user/admin object
     * @return true if successfully retrieved the game pin, false if failed
     */
    public static boolean getGamePinFromServer(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(GAME_START);
            user.write(outputStream);
            user.setGamePin(ConnectionMethods.getInt(inputStream));
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return false;
    }

    /**
     * Checks if password and name matches the server quiz file
     * @param name the quiz name
     * @param password the quiz password
     * @return true if match false if don't match
     */
    public static boolean checkIfPasswordCorrect(String name, String password) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(CHECK_IF_NAME_AND_PASSWORD_CORRECT);
            ConnectionMethods.sendString(name, outputStream);
            ConnectionMethods.sendString(password, outputStream);
            switch (inputStream.read()) {
                case TRUE: return true;
                case FALSE: return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return false;
    }

    /**
     * Send quiz object to the server
     * @param quiz the quiz object to send to the server
     */
    public static void writeQuizToServer(Quiz quiz){
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        try {
            outputStream.write(CREATE_NEW_GAME);
            quiz.write(outputStream);
        }catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            connectionHandler.closeConnection();
        }
    }

    /**
     * checks if room has any players
     * @param userJoinThread the user Join thread that checks for user connections
     * @return true if has atlease one please, false if less
     */
    public static boolean checkIfRoomHasPlayers(UserJoinThread userJoinThread){
        if (userJoinThread != null) {
            if (userJoinThread.getTotalUsers() != 0)
                return true;
        }
        return false;
    }

    /**
     * Sends command to a server with admin object
     * @param admin the admin object that sends the command
     * @param command the command to send
     */
    public static void adminServerCommand(Admin admin, int command){
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        try {
            outputStream.write(command);
            admin.write(outputStream);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
    }

    /**
     * Sends writeable object to the server
     * @param writable the object that implemented Writeable
     * @param command the command to send
     */
    public static void sendToServer(Writable writable, int command){
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        try {
            outputStream.write(command);
            writable.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            connectionHandler.closeConnection();
        }
    }

    /**
     * Checks if game exists in the server
     * @param name the game name
     * @return true if exists, false if not
     */
    public static boolean CheckIfGameExists(String name){
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(CHECK_IF_GAME_EXISTS);
            ConnectionMethods.sendString(name, outputStream);
            switch (inputStream.read()) {
                case TRUE: return true;
                case FALSE: return false;
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return false;
    }

    /**
     * Creating Quiz with scanner, Once created returns Quiz object
     * @return the quiz object that created
     */
    public static Quiz createGame() {
        System.out.print("Game name: ");
        String name = scanner.nextLine();
        if(!CheckIfGameExists(name)){
            System.out.println("Great name");
        }else{
            System.out.println("Please choose different name, This name already exists");
            return createGame();
        }
        System.out.println("I love it!, please choose an administrator password");
        System.out.print("Admin password: ");
        String password = scanner.nextLine();
        Quiz quiz = new Quiz(password, name);
        int i = 1;
        do{
            Quiz.Question question = new Quiz.Question();
            System.out.println("Question number " + i + " please enter the question below:");
            question.setQustion(scanner.nextLine());

            System.out.println("Please enter the correct answer below:");
            question.setRightAnswer(scanner.nextLine());

            String[] answer = new String[3];
            for (int j = 0; j < 3; j++) {
                System.out.println("Please enter wrong answer number " + (j+1) + ": ");
                answer[j] = scanner.nextLine();
            }
            question.setWrongAnwesers(answer);
            quiz.addQuestion(question);
            i++;
            System.out.println("To add more questions type 'continue' to finish type anything else");
        }while(scanner.nextLine().equals("continue"));
        return quiz;
    }

    /**
     * Pulls the results for all players in the game with user object
     * @param user object that holds the game pin code
     * @return List of UserAndScore
     */
    public static List<UserAndScore> adminPullCurrentResults(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        List<UserAndScore> userScoreList = new ArrayList<>();
        try {
            outputStream.write(GET_ADMIN_QUESTION_RESULT);
            user.write(outputStream);
            int totalUsers = ConnectionMethods.getInt(inputStream);
            for (int i = 0; i < totalUsers; i++) {
                UserAndScore userScore = new UserAndScore();
                userScore.setUserName(ConnectionMethods.getString(inputStream));
                userScore.setScore(ConnectionMethods.getInt(inputStream));
                userScoreList.add(userScore);
            }
            Collections.sort(userScoreList);
           return userScoreList;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return null;
    }

    /**
     * Gets the highest score player from the game
     * @param user the user object that holds the game pin code
     * @return String of the user name with the highest score, null if failed
     */
    public static String userGetHighestScorePlayer(User user){
        List<UserAndScore> userAndScoreList = adminPullCurrentResults(user);
        if(userAndScoreList != null)
            return userAndScoreList.get(0).getUserName();
        else
            return null;
    }

    /**
     * Pulls the current question from the game
     * @param user the user object that holds the game pin code
     * @return List of strings that holds the question and 4 answers
     */
    public static List<String> pullCurrentQuestion(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        List<String> question = new ArrayList<>();
        try {
            outputStream.write(PULL_CURRENT_QUESTION);
            user.write(outputStream);
            if(inputStream.read() != OKAY)
                return null;
            question.add(ConnectionMethods.getString(inputStream));
            for (int i = 0; i < 4; i++)
               question.add(ConnectionMethods.getString(inputStream));
            return question;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return null;
    }

    /**
     * Sending user answer to the server
     * @param user the object that holds game pin code and user name
     * @param string the answer 1-4 string
     */
    public static void sendAnswer(User user, String string) {
        if(Integer.parseInt(string) > 4 || Integer.parseInt(string) < 1)
            return;
        System.out.println();
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        try {
            outputStream.write(SEND_ANSWER_TO_QUESTION);
            user.write(outputStream);
            ConnectionMethods.sendInt(Integer.parseInt(string)-1, outputStream);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
    }

    /**
     * Pull current results for the user, Shows the user score
     * @param user the user object which holds the game pin code and the user name
     * @return the user score int
     */
    public static int pullCurrentResult(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(PULL_CURRENT_RESULTS);
            user.write(outputStream);
            return ConnectionMethods.getInt(inputStream);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return -1;
    }
}
