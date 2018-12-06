package com.finalproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class User implements Writable {
    private String userName;
    private int gamePin;
    private boolean isAdmin;

    public User(){

    }
    public User(InputStream inputStream)throws IOException{
        int stringLength = inputStream.read();
        byte[] buffer = new byte[stringLength];
        int actuallyRead = inputStream.read(buffer);
        if(actuallyRead!=stringLength)
            throw new IOException("Something went wrong with the stream");
        userName = new String(buffer);
        buffer = new byte[4];
        actuallyRead = inputStream.read(buffer);
        if(actuallyRead!=4)
            throw new IOException("Something went wrong with the stream");
        gamePin = ByteBuffer.wrap(buffer).getInt();
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
        outputStream.write(userName.getBytes().length);
        outputStream.write(userName.getBytes());
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).putInt(gamePin);
        outputStream.write(buffer);
        outputStream.write(isAdmin? 1:0);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User){
            if(((User) obj).gamePin == gamePin && ((User) obj).userName.equals(userName)){
                return true;
            }
        }
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
