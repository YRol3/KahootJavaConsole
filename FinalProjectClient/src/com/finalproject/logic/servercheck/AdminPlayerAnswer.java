package com.finalproject.logic.servercheck;

import com.finalproject.logic.ConnectionHandler;
import com.finalproject.users.Admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class AdminPlayerAnswer extends Thread {
    private static final int GET_ADMIN_RESULTS = 209;
    private Admin user;
    private boolean running;
    private int totalPlayers;
    private int totalAnswers;
    private OnServerResponseListener listener;
    private boolean oneTimeSend;

    public AdminPlayerAnswer(Admin user, OnServerResponseListener listener){
        this.user = user;
        this.running = true;
        this.listener = listener;
    }

    public void reset() {
        totalAnswers = 0;
        totalPlayers = 0;
    }

    @Override
    public void run() {
        while(running) {
            ConnectionHandler connectionHandler = new ConnectionHandler();
            OutputStream outputStream = connectionHandler.getOutputStream();
            InputStream inputStream = connectionHandler.getInputStream();
            boolean flag = false;
            try {
                outputStream.write(GET_ADMIN_RESULTS);
                user.write(outputStream);
                int tempTotalPlayers, tempTotalAnswers;
                tempTotalPlayers = getIntFromInputStream(inputStream);
                tempTotalAnswers = getIntFromInputStream(inputStream);
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
                    listener.OnServerResponse(totalPlayers, totalAnswers);
                }
            }
        }
    }

    public static int getIntFromInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[4];
        int actuallyRead = inputStream.read(buffer);
        if(actuallyRead!=4)
            throw new IOException("Something went wrong with the input stream");
        return ByteBuffer.wrap(buffer).getInt();
    }
    public void wakeUpListener(){
        oneTimeSend = true;
    }
    public void killThread(){
        running=false;
    }

    public interface OnServerResponseListener{
        void OnServerResponse(int totalPlayers, int totalAnswers);
    }
}
