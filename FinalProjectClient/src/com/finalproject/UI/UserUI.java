package com.finalproject.UI;

import com.finalproject.Objects.Quiz;
import com.finalproject.Objects.UserAndScore;
import com.finalproject.logic.server.ConnectionHandler;
import com.finalproject.logic.UserInput;
import com.finalproject.logic.server.AdminPlayerAnswer;
import com.finalproject.logic.server.GameStateChecker;
import com.finalproject.logic.server.UserJoinThread;
import com.finalproject.Objects.users.Admin;
import com.finalproject.Objects.users.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.finalproject.logic.ConsoleFunctions.cleanScreen;
import static com.finalproject.logic.GameManager.*;

import java.util.*;

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
    private int gameState=-1;
    private AdminPlayerAnswer adminPlayerAnswer;
    private UserJoinThread userJoinThread;
    private User user;

    /**
     *
     *    <p>Starts the dialog for the player</p>
     *    <p>a. Asks for user name at first</p>
     *    <p>b. If already asked for name will just show actions 1 to 4</p>
     *        <p>1. for joining the game</p>
     *        <p>2. for creating quiz for a game</p>
     *        <p>3. for starting a game</p>
     *        <p>4. for exiting the program</p>
     *
     */
    public void startDialog(){
        if(!ConnectionHandler.checkConnection()){
            /*
                Server is offline
                Therefore displays a message to the user
             */
            System.out.println("Dear user we apologize from the bottom of our heart");
            System.out.println("But the server is down at the moment, Try again later");
            return;
        }
        if(firstTimeRunning){
            /*
                First time running, Getting the user name,
                Before asking for actions.
             */
            System.out.println("Hey please enter your user name");
            System.out.println("This user name will be used in the game later on");
            System.out.print("Your user name: ");
            user = new User(scanner.nextLine());
            System.out.println("Thank you " + user.getUserName());
            firstTimeRunning = false;
        }
        /*
            Displaying to the user Actions
         */
        System.out.println("1. Join game");
        System.out.println("2. Create new game");
        System.out.println("3. Start game");
        System.out.println("4. Exit the program");
        switch (scanner.nextLine()){
            case "1": joinGame();
                break;
            case "2": {
                    /*
                        Create quiz with create game, If successfully created will receive Quiz object
                        if failed to create will receive null
                     */
                    Quiz quiz;
                    if((quiz = createGame()) != null)
                        sendToServer(quiz, CREATE_NEW_QUIZ);
                        cleanScreen();
                        System.out.println("Successfully added quiz to the server " +
                                "\n To start the game choose start game at the menu " +
                                "\n Make sure you type the name and password correct");
                        startDialog();
                    }
                break;
            case "3": startGame();
                break;
            case "4": {
                /*
                    Exiting the program,
                    Cleaning the screen,
                    Sending goodbye message,
                    Closing the program
                 */
                cleanScreen();
                System.out.println("Thank you for choosing our program");
                System.out.println("Have a lovely day :)");
                System.exit(0);
            }
            /*
                Any other thing the user types instead of 1-4
                Send him to the start dialog again
             */
            default:startDialog();
                break;
        }
    }

    /**
     *  Starts the game
     *  <p>It takes from the user two inputs</p>
     *  <p>1. Game name (If exists)</p>
     *  <p>2. Game password</p>
     *  if both correct its starts the game
     */
    private void  startGame() {
        System.out.println("Please enter the game name exactly as you wrote it when created");
        System.out.print("Game name: ");
        String name = scanner.nextLine();
        if(CheckIfGameExists(name)) {
            /*
                If game name exists we ask for the password
             */
            System.out.println("Please enter the administrator password for the game");
            System.out.print("Your password: ");
            String password = scanner.nextLine();
            if (checkIfPasswordCorrect(name, password)) {
                /*
                    If game name and game password matches
                    The quiz file on the server we start the game
                    and creating the object Admin on the same pointer
                 */
                System.out.println("Game is starting please wait..");
                user = new Admin(password, name, user.getUserName());
                getGamePinFromServer(user);// Getting the game pin but not yet displaying it to the user
                /*
                    Once the Thread detects game state change, It triggers the adminHandleGameStates(int gameState)
                    Used method reference instead of lambda expression as there is only one function we call with
                    the same parameter we received from the interface
                 */
                Thread gameStateChecker = new GameStateChecker(user, this::adminHandleGameStates);
                gameStateChecker.start();
                adminGameRoomTextDialog();
                adminGameRoomDialogInput();
                return;
            }else
                System.out.println("Something went wrong, name/password is wrong");
        }else
            System.out.println("This game don't exists!");
        /*
            If game doesn't exists or password is not matching the name
            We send the user back to the start dialog
         */
        startDialog();
    }

    /**
     * Handling all the different game states for the admin UI
     * @param gameState is the current state of the game, We use this parameter to define either this is
     *                  End state, Begin state, Results state or any other value and this will represent us the question state
     */
    private void adminHandleGameStates(int gameState){
        //We set our object gameState to be the same
        this.gameState = gameState;
        if (gameState != END_STATE) {
            //Not end state
            if (gameState == RESULT_STATE) {
                /*
                    Result state
                    We pull results
                    set the admin Text Dialog and input
                 */
                cleanScreen();
                pullResultText(false);
                adminGameRoomTextDialog();
                adminGameRoomDialogInput();
            } else if (gameState == BEGIN_STATE) {
                /*
                    Begin_State
                    We are waiting for players to connect the game
                    Start userJoinThread
                    Each players that joins we display to the Admin UI
                    And we show the admin text dialog, and wait for user input
                 */
                cleanScreen();
                System.out.println("----Waiting for players----");
                System.out.println("Game Pin: " + user.getGamePin());
                System.out.println("Send the game pin to your players, That way they can join your room");
                System.out.println("----Waiting for players----");
                if (userJoinThread == null) {
                    /*
                        Once the Thread detects user join to the game, It triggers the onUserJoin(string userName)
                        Used method reference instead of lambda expression as there is only one function we call with
                        the same parameter we received from the interface
                    */
                    userJoinThread = new UserJoinThread((Admin)user, UserUI::printUserJoinMessage);
                    userJoinThread.start();
                }
                adminGameRoomTextDialog();
                adminGameRoomDialogInput();
            } else {
                /*
                    If none of the above state, Than question state
                    We kill the user join thread if alive and running
                    We Create a thread that will check how much answers submitted the total users
                    and to make sure its looks smoothly on the user screen
                    We print each time the empty lines of cleanScren() and the admin text dialog
                 */
                if(userJoinThread.isAlive())
                    userJoinThread.killThread();
                if(adminPlayerAnswer == null) {
                    adminPlayerAnswer = new AdminPlayerAnswer((Admin)user, (totalPlayers, totalAnswers) -> {
                        cleanScreen();
                        displayQuestion(user);
                        System.out.println(totalAnswers + " Answers out of " + totalPlayers);
                        adminGameRoomTextDialog();
                    });
                    adminPlayerAnswer.start();
                }
                /*
                    As the adminPlayerAnswer thread reacts only once a player submit an answer,
                    We don't display the admin the dialog + question that the users needs to see
                    So we call wakeUpListener, that will call the listener for one time
                 */
                adminPlayerAnswer.wakeUpListener();
                /*
                    Wait for user input
                 */
                adminGameRoomDialogInput();
            }


        } else {
            /*
                We are in the end state, We check if adminPlayerAnswers is running, to kill it
                We pull end results
                We close the program (We could as well send him back to startDialog()
             */
            if(adminPlayerAnswer != null && adminPlayerAnswer.isAlive()) adminPlayerAnswer.killThread();
            pullResultText(true);
            System.exit(0);
        }
    }

    /**
     * Pulls game results with a table score, Can return the winner aswell
     * @param end Set true if the end of the game, Will return the winner,
     *            Set false if anything else will return the table score
     */
    private void pullGameResults(boolean end){
        /*
            Gets list of Users and Scores sorted from the highest score to the lowest
         */
        List<UserAndScore> userAndScores = adminPullCurrentResults(user);
        if(userAndScores != null) {
            String message;
            if (end) {
                /*
                    If end is true, Displaying the winner,
                    If the user is the winner displays the "you are the winner"
                    If not displays the the winner name
                 */
                if(userAndScores.get(0).getUserName().equals(user.getUserName()))
                    message = "You are the winner of the quiz, Congratulations!!";
                else
                    message = String.format("%s is the winner of the quiz, Congratulations!!", userAndScores.get(0).getUserName());
                System.out.println(message);
            }
            /*
                Displays the User and the Score of the top 5 players
             */
            System.out.println("---Table Score---");
            for (int i = 0; i < userAndScores.size(); i++) {
                if(i>=5)break;
                message = String.format("[%d] %s with score of: %d", i, userAndScores.get(i).getUserName(), userAndScores.get(i).getScore());
                System.out.println(message);
            }
        }
    }

    /**
     * Displays text for admin dialog
     * Two states, if game dialog == -1 (Which means game opened but not started)
     * Will display one message, If not -1 will display different message
     */
    private void adminGameRoomTextDialog(){
        if (this.gameState == -1){
            cleanScreen();
            System.out.println("----Game Room----");
            System.out.println("Successfully created and started the game room");
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

    /**
     * Waiting for admin scanner input,
     * Has different stages
     * if user try's to change game state but game state is BEGIN_STATE and there is no players in the game yet
     * You need at lease one player to start the game
     * if game state is question or result state will switch between states untill quiz ends
     * will switch to end state
     */
    private void adminGameRoomDialogInput() {
        String choose;
        boolean firstMessage=true;
        do{
            if(this.gameState == BEGIN_STATE && !checkIfRoomHasPlayers(userJoinThread) && !firstMessage){
                System.out.println("You need at lease one player to start the game");
            }
            firstMessage = false;
            choose = scanner.nextLine();
        }
        while(!UserInput.isNumber(choose) || (this.gameState == BEGIN_STATE && !checkIfRoomHasPlayers(userJoinThread) && !choose.contains("2") ));

        switch(Integer.parseInt(choose)){
            /*
                if game not started, Starting the game
                if game started, next question, changing between states (result/question state and end state)
             */
            case 1: {
                if (this.gameState == -1)
                    adminServerCommand((Admin)user, ADMIN_START_GAME);
                else
                    adminServerCommand((Admin)user, ADMIN_NEXT_QUESTION);
                break;
            }
            case 2:
                /*
                    requesting stopping the game
                 */
                adminServerCommand((Admin)user, ADMIN_STOP_GAME);
                System.out.println("Closed the server room, Have a good day.");
                System.exit(0);
                break;
            default:
                /*
                    if choose anything else (not 1 or 2)
                    shows the dialog again
                 */
                adminGameRoomDialogInput();
                break;
        }

    }

    /**
     *  Shows the user join game dialog
     *  Requesting for game pin code
     *  if game exists and in the begin state, Will join the user to the game
     */
    private void joinGame() {
        String num;
        do {
            System.out.println("Enter game pin code");
            System.out.println("Make sure you type the number right");
            num = scanner.nextLine();
        }while(!UserInput.isNumber(num));
        /*
            Setting the user game pin to the number entered
         */
        user.setGamePin(Integer.parseInt(num));
        ConnectionHandler connectionHandler = new ConnectionHandler();
        OutputStream outputStream = connectionHandler.getOutputStream();
        InputStream inputStream = connectionHandler.getInputStream();
        try {
            outputStream.write(JOIN_GAME);
            user.write(outputStream);
            if (inputStream.read() == SUCCESSFULLY) {
                /*
                    if received from the server successfully joined
                    starting game thread to check game states
                    using method reference to go to handleGameState of the user with the received game state
                 */
                Thread gameStateChecker = new GameStateChecker(user, this::handleGameState);
                gameStateChecker.start();
                System.out.println("Successfully joined the game!");
            } else {
                /*
                    if game doesn't exists, or not in BEGIN_STATE, send to the start dialog again
                 */
                System.out.println("the game pin does not exists / the game is not accepting players");
                startDialog();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connectionHandler.closeConnection();
        }
    }

    /**
     * Handles the game states for the regular user
     * Begin state will display "Waiting for admins to start the game"
     * Results state will display the user result and highest player in the table score
     * Question state will display the current question and 4 answers to the question
     * @param gameState
     */
    private void handleGameState(int gameState) {
        this.gameState = gameState;
        switch (gameState){
            case BEGIN_STATE:
                /*
                    Begin state waiting for more players to join and administrator to switch game state
                 */
                cleanScreen();
                System.out.println("Waiting for administrator to start the game");
                break;
            case RESULT_STATE: {
                /*
                    Results state, pulling user score,
                    if user score is not -1, successfully pulled user score from the server
                    if user score is the same as before the results, user had wrong answer
                    if user score is higher than before the results, user had right answer
                    if user waitingForAnswer Variable is true that means user didn't answered the
                    question, Will get the too late message
                 */
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
                    /*
                        Setting user score to the score received
                     */
                    userScore = tempUserScore;
                    System.out.println(String.format("Your score is: %d", userScore));
                }
                /*
                    Getting the highest score player from the table score and displaying,
                    If the user name is the user as the user who pulled the results
                    Will get "You are the leading player" message if not will get the name of the highest
                    score player
                 */
                String userName = userGetHighestScorePlayer(user);
                if(user.getUserName().equals(userName)){
                    System.out.println("You are the leading player in the quiz so far, Keep on going like this");
                }else{
                    System.out.println(userName + " is currently the leading player in the quiz, try to keep up");
                }
            }
                break;
            case END_STATE: {
                /*
                    End state, pulling end results, same as the results the admin is pulling
                    on each state, Just with the variable end set to true to display the winner
                 */
                pullResultText(true);
                System.exit(0);
                break;
            }
            default:
                /*
                    defualt state is everything else than END_STATE BEGIN_STATE RESULT_STATE
                    Which means its question state
                    pulling the current question with answers
                    starting question input method(Which is starting thread for input)
                 */
                displayQuestion(user);
                questionInput();
                break;

        }
    }

    /**
     * Displaying end results or results if end set to true or false
     * pulling the game results with table score and winner if set to true
     * @param end pulling end results if set to true
     */
    private void pullResultText(boolean end) {
        cleanScreen();
        System.out.println(end? "-----END RESULTS-----" : "-----RESULTS-----" );
        pullGameResults(end);
    }


    /**
     * If user did not submitted answer yet(waitingForAnswer is true), Starting a thread
     * Waiting for user input, Once input received calling sendAnswer method
     * and setting waitingForAnswer to false
     */
    private void questionInput(){
        if (!waitingForAnswer) {
            /*
                starting thread, with minimum input which is 1, maximum input which is 4
             */
            Thread userInput = new UserInput(1, 4, string -> {
                if(waitingForAnswer) {
                    /*
                        Sending answer to the server and displaying a message
                        and setting waitingForAnswer to false
                     */
                    sendAnswer(user, string);
                    System.out.println("Your answer received, Lets wait for the results :)");
                    waitingForAnswer = false;
                }
            });
            userInput.start();
        }
        /*
            Setting waitingForAnswer to true
            and displaying a message "your answer: "
         */
        System.out.print("Your answer: ");
        waitingForAnswer = true;
    }

    /**
     * Displaying current question for the user
     * @param user the user which the question will displayed for
     */
    private static void displayQuestion(User user) {
        /*
            Pulling List of strings of the current question
         */
        List<String> question = pullCurrentQuestion(user);
        if(question != null) {
            /*
                If successfully pulled questions
                display them for the user
             */
            System.out.println();
            System.out.println("-------Question-------");
            System.out.println(question.get(0));
            for (int i = 1; i < question.size(); i++) {
                System.out.println((i) + ". " + question.get(i));
            }
        }
    }

    /**
     * Printing the user has joined the game message
     * @param userName holds the string for the user name
     */
    private static void printUserJoinMessage(String userName){
        System.out.println(userName + " has joined the game");
    }
}
