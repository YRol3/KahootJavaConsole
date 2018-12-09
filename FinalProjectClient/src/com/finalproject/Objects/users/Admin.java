package com.finalproject.Objects.users;

import com.finalproject.interfaces.Writable;
import com.finalproject.logic.server.ConnectionMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Admin extends User implements Writable {
    private String gameName;
    private String gamePassword;

    /**
     * Creating admin object from input stream
     * @param inputStream the stream which will send the admin object
     * @throws IOException if the stream is not sending correctly the admin object will through
     * IOException
     */
    public Admin(InputStream inputStream) throws IOException {
        super(inputStream);
        if(super.isAdmin()) {
            gameName = ConnectionMethods.getString(inputStream);
            gamePassword = ConnectionMethods.getString(inputStream);
        }
    }
    /**
     * Creating admin object from 3 strings
     * @param gamePassword String for the game password
     * @param gameName String for the game name
     * @param username String for the user name
     */
    public Admin(String gamePassword,String gameName, String username){
        super(username);
        super.setAdmin(true);
        this.gamePassword = gamePassword;
        this.gameName = gameName;
        super.setUserName(username);
    }

    /**
     * send the Admin object through the outputStream
     * @param outputStream the outputStream that will be receiving the object
     * @throws IOException if the outputStream is not waiting for the stream will throw IOException
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        super.setAdmin(true);
        super.write(outputStream);
        ConnectionMethods.sendString(gameName, outputStream);
        ConnectionMethods.sendString(gamePassword, outputStream);
    }

    /**
     * @return String of game name from the Admin object
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * @return String of game password from the admin object
     */
    public String getGamePassword(){
        return gamePassword;
    }
}
