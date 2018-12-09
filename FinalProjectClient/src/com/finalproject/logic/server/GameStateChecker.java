package com.finalproject.logic.server;

import com.finalproject.Objects.users.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameStateChecker extends Thread {
    private static final int CHECK_GAME_STATE = 45;
    private boolean isRunning;
    private int lastGameState;
    private onGameStateChangeListener listener;
    private User user;

    /**
     * Creating game state object with user and listener
     * @param user the user that will be checking the game state
     * @param listener the listener which will be handling the game state changes
     */
    public GameStateChecker(User user, onGameStateChangeListener listener){
        isRunning=true;
        this.listener = listener;
        this.user = user;
    }

    /**
     * Requesting in the background the game state of the user game
     */
    @Override
    public void run() {
        while (isRunning) {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            OutputStream outputStream = connectionHandler.getOutputStream();
            InputStream inputStream = connectionHandler.getInputStream();
            try {
                /*
                    Send the server request for game state
                 */
                outputStream.write(CHECK_GAME_STATE);
                ConnectionMethods.sendInt(user.getGamePin(), outputStream);
                /*
                    Receive from the server game state
                 */
                int tempGameState = ConnectionMethods.getInt(inputStream);
                if(lastGameState != tempGameState) {
                    /*
                        If game state changed from the last listener call,
                        Notify the listener
                     */
                    listener.onGameStateChanged(tempGameState);
                    lastGameState = tempGameState;
                }
            } catch (IOException e) {
                /*
                    Server socket is closed for any reason
                 */
                System.out.println("Dear user we apologize from the bottom of our heart");
                System.out.println("But the server is down at the moment, Try again later");
                System.exit(0);
            } finally {
                connectionHandler.closeConnection();
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface onGameStateChangeListener{
        void onGameStateChanged(int gameState);
    }
}
