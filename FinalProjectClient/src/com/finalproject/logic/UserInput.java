package com.finalproject.logic;

import java.util.Scanner;

public class UserInput extends Thread {
    Scanner scanner;
    OnInputRecivedListener listener;
    int from, to;
    public UserInput(int from, int to, OnInputRecivedListener listener){
        this.scanner = new Scanner(System.in);
        this.listener = listener;
        this.from = from;
        this.to = to;
    }
    @Override
    public void run() {
        boolean flag=false;
        String input;
        do{
            if(flag)System.out.println("Enter a number between 1-4");
            input = scanner.nextLine();
            flag=true;
        }while(!isNumber(input));
        listener.onInputRecived(input);

    }

    public boolean isNumber(String string){
        int num;
        try{
            num = Integer.parseInt(string);
        }
        catch (NumberFormatException e){
            return false;
        }
        catch (NullPointerException e){
            return false;
        }
        if(num >= from && num <= to){
            return true;
        }
        return false;
    }
    public static boolean isNumberStatic(String string){
        try{
            Integer.parseInt(string);
        }
        catch (NumberFormatException e){
            return false;
        }
        catch (NullPointerException e){
            return false;
        }
        return true;
    }
    public interface OnInputRecivedListener{
        void onInputRecived(String string);
    }

}
