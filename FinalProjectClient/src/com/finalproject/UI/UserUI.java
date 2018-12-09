package com.finalproject.UI;

import com.finalproject.Objects.Quiz;
import com.finalproject.Objects.UserAndScore;
import com.finalproject.logic.server.ConnectionHandler;
import com.finalproject.logic.UserInput;
import com.finalproject.logic.server.AdminPlayerAnswer;
import com.finalproject.logic.server.GameStateChecker;
import com.finalproject.logic.server.UserJoinThread;
import com.finalproject.users.Admin;
import com.finalproject.users.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.finalproject.logic.ConsoleFunctions.cleanScreen;
import static com.finalproject.logic.GameManager.*;

import java.util.*;

import static com.finalproject.logic.server.AdminPlayerAnswer.*;
import static com.finalproject.logic.server.ConnectionMethods.BEGIN_STATE;
import static com.finalproject.logic.server.ConnectionMethods.END_STATE;
import static com.finalproject.logic.server.ConnectionMethods.RESULT_STATE;


public class UserUI {



    private boolean waitingForAnswer;
    private int userScore;
    private static final int JOIN_GAME = 22;
    private static final int SUCCESSFULLY = 150;
    private Scanner scanner = new Scanner(System.in);
    private  static boolean firstTimeRunning = true;
    private int gameStates=-1;
    private AdminPlayerAnswer adminPlayerAnswer;
    private UserJoinThread userJoinThread;
    private User user;

    public void startDialog(){
        if(!ConnectionHandler.checkConnection()){
            System.out.println("Dear user we apologize from the bottom of our heart");
            System.out.println("But the server is down at the moment, Try again later");
            return;
        }
        if(firstTimeRunning){
            System.out.println("Hey please enter your user namne");
            System.out.println("This user name will be used in the game later on");
            System.out.print("Your user name: ");
            user = new User();
            user.setUserName(scanner.nextLine());
            System.out.println("Thank you " + user.getUserName());
            firstTimeRunning = false;
        }
        System.out.println("1. Join game");
        System.out.println("2. Create new game");
        System.out.println("3. Start game");
        switch (scanner.nextLine().charAt(0)){
            case '1': joinGame();
                break;
            case '2': {
                    Quiz quiz;
                    if((quiz = createGame()) != null)
                        sendToServer(quiz, CREATE_NEW_QUIZ);
                    }
                break;
            case '3': startGame();
                break;
            default:startDialog();
                break;
        }
    }
    private void  startGame() {
        System.out.println("Please enter the game name exactly as you wrote it when created");
        System.out.print("Game name: ");
        String name = scanner.nextLine();
        if(CheckIfGameExists(name)) {
            System.out.println("Please enter the administrator password for the game");
            System.out.print("Your password: ");
            String password = scanner.nextLine();
            if (checkIfPasswordCorrect(name, password)) {
                System.out.println("Game is starting please wait..");
                user = new Admin(password, name, user.getUserName());
                getGamePinFromServer(user);
                Thread gameStateChecker = new GameStateChecker(user, new GameStateChecker.onGameStateChangeListener() {
                    @Override
                    public void onGameStateChanged(int gameState) {
                        gameStates = gameState;
                        if (gameState != END_STATE) {
                            if (gameState == RESULT_STATE) {
                                cleanScreen();
                                pullResultText(false);
                                adminGameRoomTextDialog();
                                adminGameRoomDialogInput();
                            } else if (gameState == BEGIN_STATE) {
                                cleanScreen();
                                System.out.println("----Waiting for players----");
                                System.out.println("Game Pin: " + user.getGamePin());
                                System.out.println("Send the game pin to your players, That way they can join your room");
                                System.out.println("----Waiting for players----");
                                if (userJoinThread == null) {
                                    userJoinThread = new UserJoinThread((Admin)user, new UserJoinThread.OnUserJoinListener() {
                                        @Override
                                        public void onUserJoin(String string) {
                                            System.out.println(string + " has joined the game");
                                        }
                                    });
                                    userJoinThread.start();
                                }
                                adminGameRoomTextDialog();
                                adminGameRoomDialogInput();
                            } else {
                                if(userJoinThread.isAlive())
                                    userJoinThread.killThread();
                                if(adminPlayerAnswer == null) {
                                    adminPlayerAnswer = new AdminPlayerAnswer((Admin)user, new OnServerResponseListener() {
                                        @Override
                                        public void OnServerResponse(int totalPlayers, int totalAnswers) {
                                            cleanScreen();
                                            displayQuestion(user);
                                            System.out.println(totalAnswers + " Answers out of " + totalPlayers);
                                            adminGameRoomTextDialog();
                                        }
                                    });
                                    adminPlayerAnswer.start();
                                }
                                adminPlayerAnswer.wakeUpListener();
                                adminGameRoomDialogInput();
                            }


                        } else {
                            if(adminPlayerAnswer != null && adminPlayerAnswer.isAlive()) adminPlayerAnswer.killThread();
                            pullResultText(true);
                            System.exit(0);
                        }
                    }
                });
                gameStateChecker.start();
                adminGameRoomTextDialog();
                adminGameRoomDialogInput();
                return;
            }else
                System.out.println("Something went wrong, name/password is wrong");
        }else
            System.out.println("This game don't exists!");
        startDialog();
    }

    private void adminPullResults(boolean end){
        List<UserAndScore> userAndScores = adminPullCurrentResults(user);
        if(userAndScores != null) {
            String message;
            if (end) {
                if(userAndScores.get(0).getUserName().equals(user.getUserName()))
                    message = "You are the winner of the quiz, Congratulations!!";
                else
                    message = String.format("%s is the winner of the quiz, Congratulations!!", userAndScores.get(0).getUserName());
                System.out.println(message);
            }
            System.out.println("---Table Score---");
            for (int i = 0; i < userAndScores.size(); i++) {
                if(i>=5)break;
                message = String.format("[%d] %s with score of: %d", i, userAndScores.get(i).getUserName(), userAndScores.get(i).getScore());
                System.out.println(message);
            }
        }
    }


    private void adminGameRoomTextDialog(){
        if (gameStates == -1){
            cleanScreen();
            System.out.println("----Game Room----");
            System.out.println("Succesfuly created and started the game room");
            System.out.println("Room state: locked");
            System.out.println("1. to open the room for connections");
            System.out.println("2. if you changed your mind and stop the game");
            System.out.println("----Game Room-----");
            return;
        }
        System.out.println("------");
        System.out.println("Please choose 1 or 2");
        System.out.println("1. Next game step");
        System.out.println("2. Stop Game");
        System.out.println("------");
    }

    private void adminGameRoomDialogInput() {
        String choose;
        boolean firstMessage=true;
        do{
            if(gameStates == BEGIN_STATE && !checkIfRoomHasPlayers(userJoinThread) && !firstMessage){
                System.out.println("You need at lease one player to start the game");
            }
            firstMessage = false;
            choose = scanner.nextLine();
        }
        while(!UserInput.isNumberStatic(choose) || (gameStates == BEGIN_STATE && !checkIfRoomHasPlayers(userJoinThread) && !choose.contains("2") ));

        switch(Integer.parseInt(choose)){
            case 1: {
                if (gameStates == -1)
                    adminServerCommand((Admin)user, ADMIN_START_GAME);
                else
                    adminServerCommand((Admin)user, ADMIN_NEXT_QUESTION);
                break;
            }
            case 2:
                adminServerCommand((Admin)user, ADMIN_STOP_GAME);
                System.out.println("Closed the server room, Have a good day.");
                System.exit(0);
                break;
            default:
                adminGameRoomDialogInput();
                break;
        }

    }






    private void joinGame() {
        String num;
        do {
            System.out.println("Enter game pin code");
            System.out.println("Make sure you type the number right");
            num = scanner.nextLine();
        }while(!UserInput.isNumberStatic(num));
        user.setGamePin(Integer.parseInt(num));
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(JOIN_GAME);
            user.write(outputStream);
            if (inputStream.read() == SUCCESSFULLY) {
                Thread gameStateChecker = new GameStateChecker(user, new GameStateChecker.onGameStateChangeListener() {
                    @Override
                    public void onGameStateChanged(int gameState) {
                        handleGameState(gameState);
                        gameStates = gameState;
                    }
                });
                gameStateChecker.start();
                System.out.println("Successfully joined the game!");
            } else {
                System.out.println("the game pin does not exists / the game is not accepting players");
                startDialog();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
    }

    private void handleGameState(int gameState) {
        switch (gameState){
            case BEGIN_STATE:
                cleanScreen();
                System.out.println("Waiting for admininstrator to start the game");
                break;
            case RESULT_STATE: {
                cleanScreen();
                int tempUserScore = pullCurrentResult(user);
                if(tempUserScore != -1){
                    if(!waitingForAnswer) {
                        if (tempUserScore != userScore)
                            System.out.println("----CORRECT ANSWER----");
                        else
                            System.out.println("----WRONG ANSWER----");
                    }else {
                        System.out.println("----TOO LATE----");
                        System.out.println("You missed your opportunity, try to be faster next time!");
                    }
                    userScore = tempUserScore;
                    System.out.println(String.format("Your score is: %d", userScore));
                }
                String userName = userGetHighestScorePlayer(user);
                if(user.getUserName().equals(userName)){
                    System.out.println("You are the leading player in the quiz so far, Keep on going like this");
                }else{
                    System.out.println(userName + " is currently the leading player in the quiz, try to keep up");
                }
            }
                break;
            case END_STATE: {
                pullResultText(true);
                System.exit(0);
                break;
            }
            default:
                displayQuestion(user);
                questionInput();
                break;

        }
    }
    private void pullResultText(boolean end) {
        cleanScreen();
        System.out.println(end? "-----END RESULTS-----" : "-----RESULTS-----" );
        adminPullResults(end);
    }



    private void questionInput(){
        if (!waitingForAnswer) {
            Thread userInput = new UserInput(1, 4, new UserInput.OnInputRecivedListener() {
                @Override
                public void onInputRecived(String string) {
                    if(waitingForAnswer) {
                        sendAnswer(user, string);
                        System.out.println("Your answer received, Lets wait for the results :)");
                        waitingForAnswer = false;
                    }
                }
            });
            userInput.start();
        }
        System.out.print("Your answer: ");
        waitingForAnswer = true;
    }

    private void displayQuestion(User user) {
        List<String> question = pullCurrentQuestion(user);
        if(question != null) {
            System.out.println();
            System.out.println("-------Question-------");
            System.out.println(question.get(0));
            for (int i = 1; i < question.size(); i++) {
                System.out.println((i) + ". " + question.get(i));
            }
        }
    }
}
