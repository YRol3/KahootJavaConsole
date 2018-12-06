package com.finalproject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

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
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                sendInt(Main.games.get(i).getUsers().size());
                for(User tempUser: Main.games.get(i).getUsers().keySet()){
                    outputStream.write(tempUser.getUserName().getBytes().length);
                    outputStream.write(tempUser.getUserName().getBytes());
                    sendInt(Main.games.get(i).getUsers().get(tempUser));
                }
            }
        }

    }

    private void adminStopGame() throws IOException{
        Admin user = new Admin(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                if(Main.games.get(i).isAdmin(user)){
                    Main.games.get(i).setGameState(user, 253);
                    System.out.println("[SERVER] " + user.getUserName() + " has closed the game room with game pin code of " + user.getGamePin());
                    break;
                }
            }
        }
    }

    private void getUsersInGame() throws IOException {
        Admin user = new Admin(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                sendInt(Main.games.get(i).getUsers().size());
                if(inputStream.read() == SEND_LAST_JOINED_USER){
                    outputStream.write(Main.games.get(i).getLastUser().getUserName().getBytes().length);
                    outputStream.write(Main.games.get(i).getLastUser().getUserName().getBytes());
                    break;
                }
            }
        }
    }

    private void getAdminResults() throws IOException {
        Admin user = new Admin(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                sendInt(Main.games.get(i).getUsers().size());
                sendInt(Main.games.get(i).getTotalAnswers());
                break;
            }
        }
    }

    private void sendInt(int num) throws IOException {
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).putInt(num);
        outputStream.write(buffer);
    }

    private void pullCurrentResults() throws IOException{
        User user = new User(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                outputStream.write(Main.games.get(i).getUsers().get(user));
                break;
            }
        }
    }

    private void sendAnswerToQuestion() throws IOException{
        User user = new User(inputStream);
        int userAnswer = inputStream.read();
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                Main.games.get(i).userAnswer(user,userAnswer);
                Main.games.get(i).setTotalAnswers(Main.games.get(i).getTotalAnswers() + 1);
                break;
            }
        }
    }

    private void pullCurrentQuestion() throws IOException{
        Admin user = new Admin(inputStream);
        Quiz quiz = new Quiz();
        int currentRightAnswer = 0;
        int currentQuestion = 0;
        for (int i = 0; i < Main.games.size(); i++) {
            if(Main.games.get(i).getGamePin() == user.getGamePin()){
                quiz = Main.games.get(i).getQuiz();
                currentQuestion = Main.games.get(i).getCurrentQuestion();
                currentRightAnswer =Main.games.get(i).getCurrentRightAnswer();
                break;
            }
        }
        synchronized (quiz){
            quiz.write(outputStream, currentQuestion, currentRightAnswer);
        }
    }

    private void adminNextQuestion() throws IOException{
        Admin user = new Admin(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            synchronized (Main.games.get(i)) {
                if (Main.games.get(i).getGamePin() == user.getGamePin()) {
                    Main.games.get(i).nextGameState(user);
                    if(Main.games.get(i).getGameState() != 254 && Main.games.get(i).getGameState() != 255){
                        if(Main.games.get(i).getGameState() >= Main.games.get(i).getQuiz().questions.size()){
                            Main.games.get(i).endGame();
                            return;
                        }
                        Random rand = new Random();
                        Main.games.get(i).setCurrentRightAnswer(rand.nextInt(4));
                        Main.games.get(i).setTotalAnswers(0);
                        break;
                    }
                }
            }
        }
    }

    private void adminStartGame() throws IOException{
        Admin user = new Admin(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            synchronized (Main.games.get(i)) {
                if (Main.games.get(i).getGamePin() == user.getGamePin()) {
                    Main.games.get(i).setCurrentQuestion(0);
                    Main.games.get(i).setGameState(user, 255);
                }
            }
        }
    }

    private void checkGameState() throws IOException{
        byte[] buffer = new byte[4];
        int actullyRead = inputStream.read(buffer);
        if(actullyRead != 4)
            throw new IOException("someting went wrong");
        int x = ByteBuffer.wrap(buffer).getInt();
        for (int i = 0; i < Main.games.size(); i++) {
            synchronized (Main.games.get(i)){
                if(Main.games.get(i).getGamePin() == x){
                    outputStream.write(Main.games.get(i).getGameState());
                    break;
                }
            }
        }
    }

    private void joinGame() throws IOException {
        User user = new User(inputStream);
        for (int i = 0; i < Main.games.size(); i++) {
            synchronized (Main.games.get(i)){
                if(Main.games.get(i).getGamePin() == user.getGamePin()){
                    if(Main.games.get(i).addUser(user))
                        outputStream.write(SUCCESSFULLY);
                    else
                        outputStream.write(-1);
                    break;
                }

            }
        }

    }

    private void startGame() throws IOException {
        Admin user = new Admin(inputStream);
        if(checkIfFileNPasswordMatch(user.getGameName(),user.getGamePassword())){
            Random rand = new Random();
            byte[] buffer = new byte[4];
            int gamePin = rand.nextInt(1000000);
            ByteBuffer.wrap(buffer).putInt(gamePin);
            outputStream.write(buffer);
            Quiz quiz = getQuiz(user.getGameName());
            listner.gameStarted(gamePin, quiz);
            System.out.println("[SERVER] "
                    + user.getUserName()
                    + " has just created a game room with game pin of: "
                    + gamePin
                    + " and the room is running the quiz "
                    + user.getGameName());
        }
    }
    private Quiz getQuiz(String quizName){
        File file = new File("Quizess" +File.separator+ quizName + ".txt");
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
    private String[] getNameNPasswordFromstream() throws IOException{
        String[] strings = new String[2];
        int stringLenght = inputStream.read();
        byte[] buffer = new byte[stringLenght];
        int actullyRead = inputStream.read(buffer);
        if(actullyRead != stringLenght)
            throw new IOException("Something went wrong");
        strings[0] = new String(buffer);
        stringLenght = inputStream.read();
        buffer = new byte[stringLenght];
        actullyRead = inputStream.read(buffer);
        if(actullyRead != stringLenght)
            throw new IOException("Something went wrong");
        strings[1] = new String(buffer);
        return  strings;
    }
    private void checkNameNpassword() throws IOException{

        String[] user = getNameNPasswordFromstream();
        if(checkIfFileNPasswordMatch(user[0], user[1]))
            outputStream.write(TRUE);
        else
            outputStream.write(FALSE);
    }
    private boolean checkIfFileNPasswordMatch(String fileName, String password){
        Quiz quiz = getQuiz(fileName);
        return quiz.quitPassword.equals(password);
    }

    private void checkIfExists() throws IOException{
        int stringLenght = inputStream.read();
        byte[] buffer = new byte[stringLenght];
        int actullyRead = inputStream.read(buffer);
        if(actullyRead != stringLenght)
            throw new IOException("Something went wrong");
        String fileName = new String(buffer);
        File file = new File("Quizess" +File.separator+ fileName + ".txt");
        if(file.exists())
            outputStream.write(TRUE);
        else
            outputStream.write(FALSE);

    }

    private void createGame() {

        OutputStream ops = null;
        try {
            Quiz quiz = new Quiz(inputStream);

            File file = new File("Quizess" +File.separator+ quiz.quizName + ".txt");
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
