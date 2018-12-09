package com.finalproject.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ConnectionMethods {

    /**
     * Finals that used for indicating the game states
     */
    public static final int BEGIN_STATE = Integer.MAX_VALUE;
    public static final int RESULT_STATE = Integer.MAX_VALUE-1;
    public static final int END_STATE = Integer.MAX_VALUE-2;

    /**
     *
     * @param inputStream the inputStream we want to receive the string from
     * @return String from the inputStream
     * @throws IOException if the inputStream is not sending the string with 4 bytes that represents an integer at the beginning
     * the IOException will call or if the string is not matching the integer that represents the length
     */
    public static String getString(InputStream inputStream) throws IOException {
        int stringLenght = getInt(inputStream);
        byte[] buffer = readToBuffer(inputStream, stringLenght);
        return new String(buffer);
    }

    /**
     *
     * @param message String with maximum length of Intger.MAX_VALUE to send through outputStream
     * @param outputStream the outputStream that we want to send the string through
     * @throws IOException If the outputStream is not receiving the stream may through IOException
     */
    public static void sendString(String message, OutputStream outputStream) throws IOException {
        sendInt(message.getBytes().length, outputStream);
        outputStream.write(message.getBytes());
    }

    /**
     *
     * @param inputStream the inputStream we want to receive the string from
     * @return int from the inputStream
     * @throws IOException if received less or more than 4 bytes
     */
    public static int getInt(InputStream inputStream) throws IOException {
        byte[] buffer = readToBuffer(inputStream, 4);
        return ByteBuffer.wrap(buffer).getInt();
    }

    /**
     *
     * @param integer the number we want to send
     * @param outputStream the outputStream that we want to send the int through
     * @throws IOException If the outputStream is not receiving the stream may through IOException
     */
    public static void sendInt(int integer, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[4];
        ByteBuffer.wrap(buffer).putInt(integer);
        outputStream.write(buffer);
    }

    /**
     *
     * @param inputStream the inputStream we want to receive the bytes from
     * @param bufferSize the size of the buffer byte[] array
     * @return byte[] Array that stores the bytes from the inputStream
     * @throws IOException if the buffer size does not match the actually received bytes, May through IOException
     */
    private static byte[] readToBuffer(InputStream inputStream, int bufferSize) throws IOException{
        byte[] buffer = new byte[bufferSize];
        int actuallyRead = inputStream.read(buffer);
        if(actuallyRead != bufferSize)
            throw new IOException("InputStream received " + actuallyRead + " bytes, expected " +bufferSize);
        return buffer;
    }
}
