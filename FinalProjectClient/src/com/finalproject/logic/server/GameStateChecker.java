package com.finalproject.logic.server;

import com.finalproject.users.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameStateChecker extends Thread {
    private static final int CHECK_GAME_STATE = 45;
    private boolean isRunning;
    private int lastGameState;
    private onGameStateChangeListener listener;
    private User user;
    public GameStateChecker(User user, onGameStateChangeListener listener){
        isRunning=true;
        this.listener = listener;
        this.user = user;
    }
    @Override
    public void run() {
        while (isRunning) {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            OutputStream outputStream = connectionHandler.getOutputStream();
            InputStream inputStream = connectionHandler.getInputStream();
            try {
                outputStream.write(CHECK_GAME_STATE);
                ConnectionMethods.sendInt(user.getGamePin(), outputStream);
                int tempGameState = ConnectionMethods.getInt(inputStream);
                if(lastGameState != tempGameState) {
                    listener.onGameStateChanged(tempGameState);
                    lastGameState = tempGameState;
                }
            } catch (IOException e) {
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
