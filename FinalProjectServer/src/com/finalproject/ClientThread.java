package com.finalproject;

import java.io.*;
import java.net.Socket;
import java.util.Random;

import static com.finalproject.ConnectionMethods.*;
import static com.finalproject.FileManger.getQuizPath;


public class ClientThread extends Thread {



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


    Socket socket;
    OutputStream outputStream;
    InputStream inputStream;
    OnGameStartedListner listner;

    public ClientThread(Socket socket){
        this.socket = socket;
    }
    public void setOnGameStartListener(OnGameStartedListner listner){
        this.listner = listner;
    }
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
    private Game getGame(int gamePin){
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == gamePin) return Main.games.get(i);
        }
        return null;
    }
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

    private void getUsersInGame() throws IOException {
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            ConnectionMethods.sendInt(game.getUsers().size(), outputStream);
            if(inputStream.read() == SEND_LAST_JOINED_USER)
                ConnectionMethods.sendString(game.getLastUser().getUserName(), outputStream);
        }
    }

    private void getAdminResults() throws IOException {
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            ConnectionMethods.sendInt(game.getUsers().size(), outputStream);
            ConnectionMethods.sendInt(game.getTotalAnswers(), outputStream);
        }
    }

    private void pullCurrentResults() throws IOException{
        User user = new User(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            ConnectionMethods.sendInt(game.getUsers().get(user), outputStream);
        }
    }

    private void sendAnswerToQuestion() throws IOException{
        User user = new User(inputStream);
        int userAnswer = ConnectionMethods.getInt(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            game.userAnswer(user,userAnswer);
            game.setTotalAnswers(game.getTotalAnswers() + 1);
        }
    }

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

    private void adminNextQuestion() throws IOException{
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            game.nextGameState(user);
            if(game.getGameState() != RESULT_STATE && game.getGameState() != BEGIN_STATE){
                if(game.getGameState() >= game.getQuiz().questions.size()){
                    game.endGame();
                    return;
                }
                Random rand = new Random();
                game.setCurrentRightAnswer(rand.nextInt(4));
                game.setTotalAnswers(0);
            }
        }
    }

    private void adminStartGame() throws IOException{
        Admin user = new Admin(inputStream);
        Game game = getGame(user.getGamePin());
        if(game != null){
            if (game.getGamePin() == user.getGamePin())
                game.setGameState(user, BEGIN_STATE);
        }
    }

    private void checkGameState() throws IOException{
        int x = ConnectionMethods.getInt(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            if (Main.games.get(i).getGamePin() == x) {
                ConnectionMethods.sendInt(Main.games.get(i).getGameState(), outputStream);
                break;
            }
        }
    }

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

    private void startGame() throws IOException {
        Admin user = new Admin(inputStream);
        if(checkIfFileNPasswordMatch(user.getGameName(),user.getGamePassword())){
            Random rand = new Random();
            int gamePin = rand.nextInt(1000000);
            ConnectionMethods.sendInt(gamePin, outputStream);
            Quiz quiz = loadQuiz(user.getGameName());
            listner.gameStarted(gamePin, quiz);
            System.out.println("[SERVER] "
                    + user.getUserName()
                    + " has just created a game room with game pin of: "
                    + gamePin
                    + " and the room is running the quiz "
                    + user.getGameName());
        }
    }

    private void checkNameNpassword() throws IOException{
        String[] user = new String[2];
        user[0] = ConnectionMethods.getString(inputStream);
        user[1] = ConnectionMethods.getString(inputStream);
        if(checkIfFileNPasswordMatch(user[0], user[1]))
            outputStream.write(TRUE);
        else
            outputStream.write(FALSE);
    }
    private boolean checkIfFileNPasswordMatch(String fileName, String password){
        return loadQuiz(fileName).quizPassword.equals(password);
    }
    public static Quiz loadQuiz(String quizName){
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
    private void checkIfExists() throws IOException{
        String fileName = ConnectionMethods.getString(inputStream);
        File file = getQuizPath(fileName);
        outputStream.write(file.exists()? TRUE: FALSE);
    }

    private void createGame() {
        OutputStream ops = null;
        try {
            Quiz quiz = new Quiz(inputStream);
            File file = getQuizPath(quiz.quizName);
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
    interface OnGameStartedListner{
        void gameStarted(int gamePin, Quiz quiz);
    }
}
