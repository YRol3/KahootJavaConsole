package com.finalproject.logic;

import com.finalproject.Objects.UserAndScore;
import com.finalproject.Objects.Quiz;
import com.finalproject.User;
import com.finalproject.Writable;
import com.finalproject.logic.servercheck.UserJoinThread;
import com.finalproject.users.Admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static com.finalproject.logic.servercheck.AdminPlayerAnswer.getIntFromInputStream;

public class GameManager {

    private static Scanner scanner = new Scanner(System.in);
    /*
        private finals
     */
    public static final int PULL_CURRENT_QUESTION = 144;
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
    /*
        public finals
     */
    public static final int ADMIN_STOP_GAME = 159;
    public static final int ADMIN_START_GAME = 104;
    public static final int ADMIN_NEXT_QUESTION = 105;
    public  static final int CREATE_NEW_QUIZ = 100;





    public static boolean getGamePinFromServer(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(GAME_START);
            user.write(outputStream);
            byte[] bytes = new byte[4];
            int actuallyRead = inputStream.read(bytes);
            if (actuallyRead != 4)
                throw new IOException("Wrong input stream waiting for 4 bytes but recived : " + actuallyRead);
            user.setGamePin(ByteBuffer.wrap(bytes).getInt());
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
    public static boolean checkIfPasswordCorrect(String name, String password) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(CHECK_IF_NAME_AND_PASSWORD_CORRECT);
            outputStream.write(name.getBytes().length);
            outputStream.write(name.getBytes());
            outputStream.write(password.getBytes().length);
            outputStream.write(password.getBytes());
            switch (inputStream.read()) {
                case TRUE: {
                    return true;
                }
                case FALSE:
                    return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return false;
    }
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
    public static boolean checkIfRoomHasPlayers(UserJoinThread userJoinThread){
        if (userJoinThread != null) {
            if (userJoinThread.getTotalUsers() != 0)
                return true;
        }
        return false;
    }
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

    public static boolean CheckIfGameExists(String name){
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(CHECK_IF_GAME_EXISTS);
            outputStream.write(name.getBytes().length);
            outputStream.write(name.getBytes());
            switch (inputStream.read()) {
                case TRUE:
                    return true;
                case FALSE:
                    return false;
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

    public static Quiz createGame() {
        System.out.print("Game name: ");
        String name = scanner.nextLine();
        if(!CheckIfGameExists(name)){
            System.out.println("Great name");
        }else{
            System.out.println("Please choose different name, This name already exists");
            createGame();
            return null;
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

    public static List<UserAndScore> adminPullCurrentResults(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        List<UserAndScore> userScoreList = new ArrayList<>();
        try {
            outputStream.write(GET_ADMIN_QUESTION_RESULT);
            user.write(outputStream);
            int totalUsers = getIntFromInputStream(inputStream);
            for (int i = 0; i < totalUsers; i++) {
                int stringLenght = inputStream.read();
                byte[] buffer = new byte[stringLenght];
                int actuallyRead = inputStream.read(buffer);
                if (actuallyRead != stringLenght)
                    throw new IOException("Something went wrong with the inputstream");
                UserAndScore userScore = new UserAndScore();
                userScore.setUserName(new String(buffer));
                userScore.setScore(getIntFromInputStream(inputStream));
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
    public static String userGetHighestScorePlayer(User user){
        return adminPullCurrentResults(user).get(0).getUserName();
    }

    public static int pullCurrentResult(User user) {
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(PULL_CURRENT_RESULTS);
            user.write(outputStream);
            return inputStream.read();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
        return -1;
    }

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
            int qStringLenght = inputStream.read();
            byte[] buffer = new byte[qStringLenght];
            int actuallyRead = inputStream.read(buffer);
            if (qStringLenght != actuallyRead)
                throw new IOException("Something went wrong with the input stream");
            question.add(new String(buffer));
            for (int i = 0; i < 4; i++) {
                int answerLenght = inputStream.read();
                buffer = new byte[answerLenght];
                actuallyRead = inputStream.read(buffer);
                if (answerLenght != actuallyRead)
                    throw new IOException("Something went wrong with the inputstream");
               question.add(new String(buffer));
            }
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

    public static void sendAnswer(User user, String string) {
        if(Integer.parseInt(string) > 4 || Integer.parseInt(string) < 1)
            return;
        System.out.println();
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        try {
            outputStream.write(SEND_ANSWER_TO_QUESTION);
            user.write(outputStream);
            outputStream.write(Integer.parseInt(string) - 1);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
    }

}
