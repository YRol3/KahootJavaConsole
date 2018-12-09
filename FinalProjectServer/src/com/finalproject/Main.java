package com.finalproject;

import com.finalproject.logic.ClientThread;
import com.finalproject.logic.Game;
import com.finalproject.objects.Quiz;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static boolean running = true;
    private static final int PORT = 3001;
    public static List<Game> games = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("Successfully started the server");
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while(running) {
                Socket socket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(socket, (gamePin, quiz) -> games.add(new Game(gamePin, quiz)));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
