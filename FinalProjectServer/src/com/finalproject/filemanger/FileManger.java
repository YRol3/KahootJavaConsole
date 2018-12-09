package com.finalproject.filemanger;

import java.io.*;

public class FileManger {
    /**
     * Changing this boolean to true or false for the correct file path to run the server
     * and find the Quizzes folder path
     */
    private static final boolean RUNNING_FROM_INTELIJ = true;

    /**
     * gets the quiz file path
     * @param quizName quiz file name
     * @return File which holds the correct file path
     */
    public static File getQuizPath(String quizName){

        StringBuilder filePath = new StringBuilder();
        if(RUNNING_FROM_INTELIJ){
            filePath.append("out")
                    .append(File.separator)
                    .append("production")
                    .append(File.separator)
                    .append("FinalProject");
        }else{
            filePath.append(".");
        }
        filePath.append(File.separator);
        filePath.append("Quizess");
        filePath.append(File.separator);
        filePath.append(quizName);
        filePath.append(".txt");
        return new File(filePath.toString());
    }


}
