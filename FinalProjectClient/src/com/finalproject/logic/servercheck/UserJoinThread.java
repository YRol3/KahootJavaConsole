package com.finalproject.logic.servercheck;

import com.finalproject.logic.ConnectionHandler;
import com.finalproject.users.Admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.finalproject.logic.servercheck.AdminPlayerAnswer.getIntFromInputStream;


public class UserJoinThread extends Thread {
    private static final int GET_USERS_IN_GAME = 241;
    private static final int SEND_LAST_JOINED_USER = 100;
    private Admin user;
    private OnUserJoinListener listener;
    private boolean running = true;
    private int totalUsers;

    public UserJoinThread(Admin user, OnUserJoinListener listener){
        this.user = user;
        this.listener = listener;
    }
    @Override
    public void run() {

        while(running) {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            OutputStream outputStream = connectionHandler.getOutputStream();
            InputStream inputStream = connectionHandler.getInputStream();
            try {
                outputStream.write(GET_USERS_IN_GAME);
                user.write(outputStream);
                int tempTotal = getIntFromInputStream(inputStream);
                if (tempTotal != totalUsers) {
                    totalUsers = tempTotal;
                    outputStream.write(SEND_LAST_JOINED_USER);
                    int stringLenght = inputStream.read();
                    byte[] buffer = new byte[stringLenght];
                    int actuallyRead = inputStream.read(buffer);
                    if (actuallyRead != stringLenght)
                        throw new IOException("something went wrong with the input stream");
                    String user = new String(buffer);
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

    public int getTotalUsers() {
        return totalUsers;
    }

    public void killThread(){
        running = false;
    }
    public interface OnUserJoinListener{
        void onUserJoin(String string);
    }
}