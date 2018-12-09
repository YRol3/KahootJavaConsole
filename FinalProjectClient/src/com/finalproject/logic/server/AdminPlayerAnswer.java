package com.finalproject.logic.server;

import com.finalproject.Objects.users.Admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

public class AdminPlayerAnswer extends Thread {

    private static final int GET_ADMIN_RESULTS = 209;
    private Admin user;
    private boolean running;
    private int totalPlayers;
    private int totalAnswers;
    private OnServerResponseListener listener;
    private boolean oneTimeSend;

    /**
     * AdminPlayerAnswer object for getting how much players in game answered the question
     * @param user the admin object that holds game name game password game pin code
     * @param listener the listener that will handle when anther players sends his answer
     */
    public AdminPlayerAnswer(Admin user, OnServerResponseListener listener){
        this.user = user;
        this.running = true;
        this.listener = listener;
    }

    /**
     * Checks how much players in the game
     * How much answers submitted so far
     * And if the last time checked total answer is different from the new check
     * calling the listener to handling the new answer
     */
    @Override
    public void run() {
        while(running) {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            OutputStream outputStream = connectionHandler.getOutputStream();
            InputStream inputStream = connectionHandler.getInputStream();
            /*
                Sets boolean flag to check whether or not new answer was submitted
             */
            boolean flag = false;
            try {
                outputStream.write(GET_ADMIN_RESULTS);
                user.write(outputStream);
                int tempTotalPlayers, tempTotalAnswers;
                tempTotalPlayers = ConnectionMethods.getInt(inputStream);
                tempTotalAnswers = ConnectionMethods.getInt(inputStream);
                synchronized (this) {
                    if (tempTotalAnswers != totalAnswers || tempTotalPlayers != totalPlayers || oneTimeSend) {
                        oneTimeSend = false;
                        totalAnswers = tempTotalAnswers;
                        totalPlayers = tempTotalPlayers;
                        flag = true;
                    }
                }
                sleep(100);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                connectionHandler.closeConnection();
                if(flag){
                    listener.OnServerResponse(totalPlayers, totalAnswers);
                }
            }
        }
    }

    /**
     * Sends the listener method one time
     */
    public void wakeUpListener(){
        oneTimeSend = true;
    }

    /**
     * setting the loop variable to false to kill the thread
     */
    public void killThread(){
        running=false;
    }

    public interface OnServerResponseListener{
        void OnServerResponse(int totalPlayers, int totalAnswers);
    }
}
