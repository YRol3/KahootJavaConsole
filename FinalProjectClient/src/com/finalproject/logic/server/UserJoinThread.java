package com.finalproject.logic.server;

import com.finalproject.Objects.users.Admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class UserJoinThread extends Thread {
    private static final int GET_USERS_IN_GAME = 241;
    private static final int SEND_LAST_JOINED_USER = 100;
    private Admin user;
    private OnUserJoinListener listener;
    private boolean running = true;
    private int totalUsers;

    /**
     * Creating user join thread which will be listening for new incoming users to the game
     * @param user the admin which will be listening for the incoming users
     * @param listener the listener that will handle the new users join event
     */
    public UserJoinThread(Admin user, OnUserJoinListener listener){
        this.user = user;
        this.listener = listener;
    }

    /**
     * Checks with the server total players, If the total is different
     * from the last checked total, Its asked for the last joined player
     * and notifies the listener
     */
    @Override
    public void run() {

        while(running) {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            OutputStream outputStream = connectionHandler.getOutputStream();
            InputStream inputStream = connectionHandler.getInputStream();
            try {
                /*
                    Sends the server get user in game command and the admin object
                    receives total players in the game
                 */
                outputStream.write(GET_USERS_IN_GAME);
                user.write(outputStream);
                int tempTotal = ConnectionMethods.getInt(inputStream);
                if (tempTotal != totalUsers) {
                    /*
                        The total players in game is different from the last checked total
                        Requesting from the server the last joined user name
                        and notifies the listener
                     */
                    totalUsers = tempTotal;
                    outputStream.write(SEND_LAST_JOINED_USER);
                    String user = ConnectionMethods.getString(inputStream);
                    listener.onUserJoin(user);
                }
                sleep(100);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                connectionHandler.closeConnection();
            }
        }
    }

    /**
     * @return int of the total users in the game
     */
    public int getTotalUsers() {
        return totalUsers;
    }

    /**
     * Calling this method will set the loop variable to false and therefore
     * stop the loop
     */
    public void killThread(){
        running = false;
    }
    public interface OnUserJoinListener{
        void onUserJoin(String string);
    }
}