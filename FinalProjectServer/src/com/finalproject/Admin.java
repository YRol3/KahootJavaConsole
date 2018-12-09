package com.finalproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Admin extends User implements Writable {
    private String gameName;
    private String gamePassword;

    public Admin(InputStream inputStream) throws IOException {
        super(inputStream);
        if(super.isAdmin()) {
            gameName = ConnectionMethods.getString(inputStream);
            gamePassword = ConnectionMethods.getString(inputStream);
        }
    }
    public Admin(String gamePassword,String gameName, String username){
        this.gamePassword = gamePassword;
        this.gameName = gameName;
        super.setUserName(username);
    }
    @Override
    public void write(OutputStream outputStream) throws IOException {
        super.setAdmin(true);
        super.write(outputStream);
        ConnectionMethods.sendString(gameName, outputStream);
        ConnectionMethods.sendString(gamePassword, outputStream);
    }

    public String getGameName() {
        return gameName;
    }
    public String getGamePassword(){
        return gamePassword;
    }
}
