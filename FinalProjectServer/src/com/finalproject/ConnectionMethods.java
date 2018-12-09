package com.finalproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ConnectionMethods {


    public static final int BEGIN_STATE = Integer.MAX_VALUE;
    public static final int RESULT_STATE = Integer.MAX_VALUE-1;
    public static final int END_STATE = Integer.MAX_VALUE-2;


    public static String getString(InputStream inputStream) throws IOException {
        int stringLenght = getInt(inputStream);
        byte[] buffer = readToBuffer(inputStream, stringLenght);
        return new String(buffer);
    }

    public static void sendString(String message, OutputStream outputStream) throws IOException {
        sendInt(message.getBytes().length, outputStream);
        outputStream.write(message.getBytes());
    }

    public static int getInt(InputStream inputStream) throws IOException {
       byte[] buffer = readToBuffer(inputStream, 4);
       return ByteBuffer.wrap(buffer).getInt();
    }

    public static void sendInt(int integer, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).putInt(integer);
        outputStream.write(buffer);
    }

    private static byte[] readToBuffer(InputStream inputStream, int bufferSize) throws IOException{
        byte[] buffer = new byte[bufferSize];
        int actuallyRead = inputStream.read(buffer);
        if(actuallyRead != bufferSize)
            throw new IOException("Inputstream received " + actuallyRead + " bytes, expected " +bufferSize);
        return buffer;
    }
}
