package com.finalproject.objects;

import com.finalproject.logic.ConnectionMethods;
import com.finalproject.interfaces.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class User implements Writable {
    private String userName;
    private int gamePin;
    private boolean isAdmin;

    /**
     * Creating user object from one string
     * @param userName String of the user name
     */
    public User(String userName){
        this.userName = userName;
    }

    /**
     * Creating user object from the input stream
     * @param inputStream the input stream that sending the user object
     * @throws IOException if the input stream is not sending correctly the user object
     * will throw IOException
     */
    public User(InputStream inputStream)throws IOException{
        userName = ConnectionMethods.getString(inputStream);
        gamePin = ConnectionMethods.getInt(inputStream);
        isAdmin = inputStream.read()==1;
    }

    /**
     * @return int of the game pin code
     */
    public int getGamePin() {
        return gamePin;
    }

    /**
     * setting the game pin code
     * @param gamePin int of the game pin code
     */
    public void setGamePin(int gamePin) {
        this.gamePin = gamePin;
    }

    /**
     * @return string of the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return boolean if the user is an admin or not
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    /**
     * setting the user to be an admin
     * @param admin boolean true/false if the user is an admin
     */
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    /**
     * setting the user name
     * @param userName string for the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Sending user object from outputStream
     * @param outputStream the outputStream that will be receiving the user object
     * @throws IOException if the outputStream is not waiting for the stream
     * Will throw IOException
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        ConnectionMethods.sendString(userName, outputStream);
        ConnectionMethods.sendInt(gamePin, outputStream);
        outputStream.write(isAdmin? 1:0);
    }

    /**
     * Checks if the object is the same object
     * @param obj the other user
     * @return true if equals to the user false if not
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj == this) return true;
        if(obj instanceof User)
            return ((User) obj).gamePin == gamePin && ((User) obj).userName.equals(userName);
        return false;
    }

    /**
     * hashing the user for quick hash map interaction
     * @return int of hashed userName
     */
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
