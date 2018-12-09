package com.finalproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class User implements Writable {
    private String userName;
    private int gamePin;
    private boolean isAdmin;

    public User(){

    }
    public User(InputStream inputStream)throws IOException{
        userName = ConnectionMethods.getString(inputStream);
        gamePin = ConnectionMethods.getInt(inputStream);
        isAdmin = inputStream.read()==1;
    }
    public int getGamePin() {
        return gamePin;
    }

    public void setGamePin(int gamePin) {
        this.gamePin = gamePin;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        ConnectionMethods.sendString(userName, outputStream);
        ConnectionMethods.sendInt(gamePin, outputStream);
        outputStream.write(isAdmin? 1:0);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj instanceof User)
            return ((User) obj).gamePin == gamePin && ((User) obj).userName.equals(userName);
        return false;
    }

    @Override
    public int hashCode() {
        int primeNumber = 31;
        int results = 0 ;
        for (int i = 0; i < userName.length(); i++) {
            results ^= userName.charAt(i)*primeNumber;
        }
        return results;
    }
}
