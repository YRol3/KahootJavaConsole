package com.finalproject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static final int PORT = 3001;
    public static List<Game> games = new ArrayList<>();
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while(true) {
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket);
                clientThread.setOnGameStartListener(new ClientThread.OnGameStartedListner() {
                    @Override
                    public void gameStarted(int gamePin,Quiz quiz) {
                        Game game = new Game(gamePin, quiz);
                        games.add(game);
                    }
                });
                clientThread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
