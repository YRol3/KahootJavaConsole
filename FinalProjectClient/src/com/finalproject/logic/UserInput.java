package com.finalproject.logic;

import java.util.Scanner;

public class UserInput extends Thread {
    Scanner scanner;
    OnInputRecivedListener listener;
    int from, to;

    /**
     * Creates UserInput object, that will check for input from minimum to maximum, And listener
     * to handle the input
     * @param from the minimum value the user can choose
     * @param to the maximum value the user can choose
     * @param listener the listener that will handle the choose
     */
    public UserInput(int from, int to, OnInputRecivedListener listener){
        this.scanner = new Scanner(System.in);
        this.listener = listener;
        this.from = from;
        this.to = to;
    }

    /**
     * Gets user input on background thread
     * Once input received and notifies the listener
     */
    @Override
    public void run() {
        boolean flag=false;
        String input;
        do{
            if(flag)System.out.println("Enter a number between 1-4");
            input = scanner.nextLine();
            flag=true;
        }while(!isNumberAndInRange(input));
        listener.onInputRecived(input);

    }

    /**
     * Checks if the string is a number, and if in range of minimum and maximum
     * @param string the string that holds the number
     * @return true if number, false if not
     */
    private boolean isNumberAndInRange(String string){
        int num;
        if(isNumber(string)) {
            num = Integer.parseInt(string);
            if(num >= from && num <= to)
                return true;
        }
        return false;
    }

    /**
     * Checks if the string is a number
     * @param string the string that holds the number
     * @return true if is a number, false if not a number
     */
    public static boolean isNumber(String string){
        try{
            Integer.parseInt(string);
        }
        catch (NumberFormatException e){
            return false;
        }
        return true;
    }
    public interface OnInputRecivedListener{
        void onInputRecived(String string);
    }

}
