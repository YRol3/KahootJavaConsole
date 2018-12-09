package com.finalproject.logic;

import com.finalproject.Main;
import com.finalproject.objects.Admin;
import com.finalproject.objects.Quiz;
import com.finalproject.objects.User;

import java.io.*;
import java.net.Socket;
import java.util.Random;

import static com.finalproject.logic.ConnectionMethods.*;
import static com.finalproject.filemanger.FileManger.getQuizPath;


public class ClientThread extends Thread {


    /**
     * private finals
     */
    private static final int CREATE_NEW_GAME = 100;
    private static final int GET_ADMIN_RESULTS = 209;
    private static final int CHECK_IF_GAME_EXISTS = 99;
    private static final int CHECK_IF_NAME_AND_PASSWORD_CORRECT = 98;
    private static final int TRUE = 0;
    private static final int FALSE = 1;
    private static final int START_GAME = 97;
    private static final int JOIN_GAME = 22;
    private static final int SUCCESSFULLY = 150;
    private static final int ADMIN_START_GAME = 104;
    private static final int ADMIN_NEXT_QUESTION = 105;
    private static final int PULL_CURRENT_QUESTION = 144;
    private static final int SEND_ANSWER_TO_QUESTION = 133;
    private static final int PULL_CURRENT_RESULTS = 143;
    private static final int GET_USERS_IN_GAME = 241;
    private static final int SEND_LAST_JOINED_USER = 100;
    private static final int ADMIN_STOP_GAME = 159;
    private static final int GET_ADMIN_QUESTION_RESULT = 135;
    private static final int CHECK_GAME_STATE = 45;

    /**
     * private object variables
     */
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private OnGameStartedListener listener;

    /**
     * Creates a client thread, with socket and listener to handle new games
     * @param socket the socket that connected to the server
     * @param listener the listener that will handle the new game creating
     */
    public ClientThread(Socket socket, OnGameStartedListener listener){
        this.socket = socket;
        this.listener = listener;
    }

    /**
     *  Handling the client request and sending to the method
     */
    @Override
    public void run() {
        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            switch (inputStream.read()){
                case CREATE_NEW_GAME: createGame();
                    break;
                case CHECK_IF_GAME_EXISTS: checkIfExists();
                    break;
                case CHECK_IF_NAME_AND_PASSWORD_CORRECT: checkNameNpassword();
                    break;
                case START_GAME: startGame();
                    break;
                case JOIN_GAME: joinGame();
                    break;
                case CHECK_GAME_STATE: checkGameState();
                    break;
                case ADMIN_START_GAME: adminStartGame();
                    break;
                case ADMIN_NEXT_QUESTION: adminNextQuestion();
                    break;
                case PULL_CURRENT_QUESTION: pullCurrentQuestion();
                    break;
                case SEND_ANSWER_TO_QUESTION: sendAnswerToQuestion();
                    break;
                case PULL_CURRENT_RESULTS: pullCurrentResults();
                    break;
                case GET_ADMIN_RESULTS: getAdminResults();
                    break;
                case GET_USERS_IN_GAME: getUsersInGame();
                    break;
                case ADMIN_STOP_GAME: adminStopGame();
                    break;
                case GET_ADMIN_QUESTION_RESULT: getAdminQuestionResult();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * gets the game object from the Main games list
     * @param gamePin game pin code to search for
     * @return the game object, null if don't exists
     */
    private Game getGame(int gamePin){
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == gamePin) return Main.games.get(i);
        }
        return null;
    }
    /**
     * sends the client the question results
     * @throws IOException if the inputStream is not waiting for a stream
     */
    private void getAdminQuestionResult() throws IOException {
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null) {
            ConnectionMethods.sendInt(game.getUsers().size(), outputStream);
            for (User tempUser : game.getUsers().keySet()) {
                ConnectionMethods.sendString(tempUser.getUserName(), outputStream);
                ConnectionMethods.sendInt(game.getUsers().get(tempUser), outputStream);
            }
        }
    }

    /**
     * changes the game state to END_STATE
     * @throws IOException if the client didn't send the Admin correctly
     */
    private void adminStopGame() throws IOException{
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            if(game.isAdmin(user)){
                game.setGameState(user, END_STATE);
                System.out.println("[SERVER] " + user.getUserName() + " has closed the game room with game pin code of " + user.getGamePin());
            }
        }
    }

    /**
     * sends to the client the user in the server
     * if recvived SEND_LAST_JOINED_USER than sends the last joined user to the client
     * @throws IOException if inputStream or OutputStream was closed or was not waiting for stream
     */
    private void getUsersInGame() throws IOException {
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            ConnectionMethods.sendInt(game.getUsers().size(), outputStream);
            if(inputStream.read() == SEND_LAST_JOINED_USER)
                ConnectionMethods.sendString(game.getLastUser().getUserName(), outputStream);
        }
    }

    /**
     * sends total users and total answers to the client
     * @throws IOException if the inputStream is not waiting for a stream
     */
    private void getAdminResults() throws IOException {
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            ConnectionMethods.sendInt(game.getUsers().size(), outputStream);
            ConnectionMethods.sendInt(game.getTotalAnswers(), outputStream);
        }
    }

    /**
     * sends the user score
     * @throws IOException if the inputStream is not waiting for a stream
     */
    private void pullCurrentResults() throws IOException{
        User user = new User(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            ConnectionMethods.sendInt(game.getUsers().get(user), outputStream);
        }
    }

    /**
     * gets the answer from the client, Updates his score accordingly
     * @throws IOException if the user was not sent correctly
     */
    private void sendAnswerToQuestion() throws IOException{
        User user = new User(inputStream);
        int userAnswer = ConnectionMethods.getInt(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            game.userAnswer(user,userAnswer);
            game.setTotalAnswers(game.getTotalAnswers() + 1);
        }
    }

    /**
     * getting the current question for the client,
     * and sending it back to the client
     * @throws IOException if the inputStream is not waiting for a stream or user sent incorrectly
     */
    private void pullCurrentQuestion() throws IOException{
        Admin user = new Admin(inputStream);
        Quiz quiz;
        int currentRightAnswer;
        int currentQuestion;
        Game game = getGame(user.getGamePin());
        if(game != null){
            quiz = game.getQuiz();
            currentQuestion = game.getCurrentQuestion();
            currentRightAnswer = game.getCurrentRightAnswer();
            quiz.sendQuestion(outputStream, currentQuestion, currentRightAnswer);
        }
    }

    /**
     * changing to next game state from client admin
     * if last question and requesting next question, endGame method called
     * @throws IOException if the user was not set correctly
     */
    private void adminNextQuestion() throws IOException{
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            game.nextGameState(user);
            if(game.getGameState() != RESULT_STATE && game.getGameState() != BEGIN_STATE){
                if(game.getGameState() >= game.getQuiz().getQuestions().size()){
                    game.endGame();
                    return;
                }
                Random rand = new Random();
                game.setCurrentRightAnswer(rand.nextInt(4));
                game.setTotalAnswers(0);
            }
        }
    }

    /**
     * getting admin user from the client and
     * setting the game state to BEGIN_STATE, and letting users to join the game
     * @throws IOException if the admin object was not sent correctly
     */
    private void adminStartGame() throws IOException{
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            if (game.getGamePin() == user.getGamePin())
                game.setGameState(user, BEGIN_STATE);
        }
    }

    /**
     * gettting game pin from the user, and sending back the game state
     * @throws IOException if the inputStream is not waiting for a stream
     */
    private void checkGameState() throws IOException{
        int x = ConnectionMethods.getInt(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            if (Main.games.get(i).getGamePin() == x) {
                ConnectionMethods.sendInt(Main.games.get(i).getGameState(), outputStream);
                break;
            }
        }
    }

    /**
     * adding user to a game, Getting user from the stream, if added
     * sending back SUCCESSFULLY, if failed sending back -1
     * @throws IOException if the inputStream is not waiting for a stream, or use was not sent correctly
     */
    private void joinGame() throws IOException {
        User user = new User(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            if(game.addUser(user))
                outputStream.write(SUCCESSFULLY);
            else
                outputStream.write(-1);
        }
    }

    /**
     * Starting a new game from quiz file
     * if password and name matches the files
     * sending back the game pin code
     * @throws IOException if the inputStream is not waiting for a stream or admin was not set correctly
     * and don't hold the right quiz name and password
     */
    private void startGame() throws IOException {
        Admin user = new Admin(inputStream);
        if(checkIfFileNPasswordMatch(user.getGameName(),user.getGamePassword())){
            Random rand = new Random();
            int gamePin = rand.nextInt(1000000);
            ConnectionMethods.sendInt(gamePin, outputStream);
            Quiz quiz = loadQuiz(user.getGameName());
            listener.gameStarted(gamePin, quiz);
            System.out.println("[SERVER] "
                    + user.getUserName()
                    + " has just created a game room with game pin of: "
                    + gamePin
                    + " and the room is running the quiz "
                    + user.getGameName());
        }
    }

    /**
     * gets two strings from the client,
     * if game and password match, sends TRUE, if don't match
     * sends FALSE
     * @throws IOException if the inputStream is not waiting for a stream or strings not sent correctly
     */
    private void checkNameNpassword() throws IOException{
        String[] user = new String[2];
        user[0] = ConnectionMethods.getString(inputStream);
        user[1] = ConnectionMethods.getString(inputStream);
        if(checkIfFileNPasswordMatch(user[0], user[1]))
            outputStream.write(TRUE);
        else
            outputStream.write(FALSE);
    }

    /**
     * checks if name and password matches the quiz file
     * @param fileName String of the quiz name
     * @param password String of the quiz password
     * @return true if correct password and name, false if not
     */
    private boolean checkIfFileNPasswordMatch(String fileName, String password){
        return loadQuiz(fileName).getQuizPassword().equals(password);
    }

    /**
     * Loading quiz from string that holds the quiz name
     * @param quizName a string of the quiz name
     * @return Object of the quiz or null if don't exists
     */
    private static Quiz loadQuiz(String quizName){
        File file = getQuizPath(quizName);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return new Quiz(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;

    }

    /**
     * gets string from the client, Checks if the quiz exists, and sends back to the client the answer
     * TRUE if exists FALSE if not
     * @throws IOException if the inputStream is not waiting for a stream or the string not sent correctly
     */
    private void checkIfExists() throws IOException{
        String fileName = ConnectionMethods.getString(inputStream);
        File file = getQuizPath(fileName);
        outputStream.write(file.exists()? TRUE: FALSE);
    }

    /**
     * gets quiz object from inputStream and safes it to a file
     */
    private void createGame() {
        OutputStream ops = null;
        try {
            Quiz quiz = new Quiz(inputStream);
            File file = getQuizPath(quiz.getQuizName());
            ops = new FileOutputStream(file);
            quiz.write(ops);
            System.out.println("[Server] Saved new quiz to the server hard disk memory " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (ops != null) {
                try {
                    ops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public interface OnGameStartedListener {
        void gameStarted(int gamePin, Quiz quiz);
    }
}
