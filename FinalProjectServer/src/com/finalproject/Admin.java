package com.finalproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Admin extends User{
    private String gameName;
    private String gamePassword;

    public Admin(InputStream inputStream) throws IOException {
        super(inputStream);
        if(super.isAdmin()) {
            int stringLength = inputStream.read();
            byte[] buffer = new byte[stringLength];
            int actuallyRead = inputStream.read(buffer);
            if (actuallyRead != stringLength)
                throw new IOException("Something went wrong with the stream");
            gameName = new String(buffer);
            stringLength = inputStream.read();
            buffer = new byte[stringLength];
            actuallyRead = inputStream.read(buffer);
            if (actuallyRead != stringLength)
                throw new IOException("Something went wrong with the stream");
            gamePassword = new String(buffer);
        }
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        super.setAdmin(true);
        super.write(outputStream);
        outputStream.write(gameName.getBytes().length);
        outputStream.write(gameName.getBytes());
        outputStream.write(gamePassword.getBytes().length);
        outputStream.write(gamePassword.getBytes());
    }

    public String getGameName() {
        return gameName;
    }
    public String getGamePassword() {
        return gamePassword;
    }
}
