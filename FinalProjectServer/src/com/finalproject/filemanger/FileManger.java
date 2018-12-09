package com.finalproject.filemanger;

import java.io.*;

public class FileManger {
    private static final boolean RUNNING_FROM_INTELIJ = true;

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
