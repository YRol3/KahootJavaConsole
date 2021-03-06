package com.finalproject.logic.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionHandler {
    public static final int PORT = 3001;
    public static final String HOST = "127.0.0.1";
    Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    /**
     * Creates connection object with outputStream and inputStream
     */
    public ConnectionHandler(){
        try {
            Socket socket = new Socket(HOST, PORT);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if there is a connection to a server
     * @return true if server is online, false if offline
     */
    public static boolean checkConnection() {
        Socket socket = null;
        try {
            socket = new Socket(HOST, PORT);
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            return false;
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Closes the socket, outputStream, inputStream
     */
    public void closeConnection(){
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
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println();
            }
        }
    }

    /*
        Getters of the class
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
