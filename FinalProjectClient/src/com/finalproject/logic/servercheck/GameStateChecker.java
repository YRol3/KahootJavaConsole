package com.finalproject.logic.servercheck;

import com.finalproject.User;
import com.finalproject.logic.ConnectionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class GameStateChecker extends Thread {
    private final int CHECK_GAME_STATE = 45;
    boolean isRunning;
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
                byte[] buffer = new byte[4];
                ByteBuffer.wrap(buffer).putInt(user.getGamePin());
                outputStream.write(CHECK_GAME_STATE);
                outputStream.write(buffer);
                int tempGameState = inputStream.read();
                if (lastGameState != tempGameState) {
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



    public void killThread(){
        isRunning=false;
    }

    public interface onGameStateChangeListener{
        void onGameStateChanged(int gameState);
    }
}
