package com.finalproject.Objects;

import com.finalproject.interfaces.Writable;
import com.finalproject.logic.server.ConnectionMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Writable {

    private static final int OKAY = 10;
    private static final int FAIL = -1;
    private List<Question>  questions = new ArrayList<>();
    private String quizPassword;
    private String quizName;
    private static final int QUESTION = 0;
    private static final int RIGHT_ANSWER = 1;

    /**
     * Creating quiz object from an inputStream
     * @param inputStream the inputStream which sends the object
     * @throws IOException Throws IOException if the inputStream did not sent correctly the object
     */
    public Quiz(InputStream inputStream) throws IOException{
        /*
            Getting quiz name and password from the input stream
         */
        this.quizPassword = ConnectionMethods.getString(inputStream);
        this.quizName = ConnectionMethods.getString(inputStream);

        /*
            Getting question list size
            and running on for loop reading each question parameters
         */
        int questionListSize = ConnectionMethods.getInt(inputStream);
        for (int i = 0; i < questionListSize; i++) {
            /*
                Creating question object
             */
            Question question = new Question();
            for (int j = 0; j < 5; j++) {
                /*
                    Reading string from the server 5 strings, each time for different part of the question
                 */
                String inputString = ConnectionMethods.getString(inputStream);
                switch (j){
                    /*
                        j = 0
                     */
                    case QUESTION: question.setQustion(inputString);
                        break;
                    /*
                        j = 1;
                     */
                    case RIGHT_ANSWER: question.setRightAnswer(inputString);
                        break;
                    /*
                        j = 2, j=3, j=4, we set the index - 2 to start from 0 to 3
                     */
                    default: question.setWrongAnweser(j-2, inputString);
                        break;
                }
            }
            this.questions.add(question);
        }
    }

    /**
     * Creating quiz object from 2 strings password and name
     * @param quitPassword String that represents the game password
     * @param quizName String that represents the game name
     */
    public Quiz(String quitPassword, String quizName) {
        this.quizPassword = quitPassword;
        this.quizName = quizName;
    }

    /**
     * Send quiz object to outputStream
     * @param outputStream the outputStream for the quiz object to be sent to
     * @throws IOException if the OutputStream is not waiting for an inputStream
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        ConnectionMethods.sendString(quizPassword, outputStream);
        ConnectionMethods.sendString(quizName, outputStream);
        ConnectionMethods.sendInt(questions.size(), outputStream);
        for(Question question: questions){
            ConnectionMethods.sendString(question.qustion, outputStream);
            ConnectionMethods.sendString(question.rightAnswer, outputStream);
            for(String wAnwsers: question.wrongAnwesers){
                ConnectionMethods.sendString(wAnwsers, outputStream);
            }
        }
    }

    /**
     * Sends one question with 4 answers to OutputStream
     * @param outputStream the outputStream that will be receiving the question
     * @param qusNum the questing number to send
     * @param currentRightAnswer the right answer for the question
     * @throws IOException if the outputStream is not waiting for inputstream
     */
    public void sendQuestion(OutputStream outputStream, int qusNum, int currentRightAnswer) throws IOException{
        if(qusNum<questions.size()) {
            Question muQuestion = questions.get(qusNum);
            outputStream.write(OKAY);
            ConnectionMethods.sendString(muQuestion.qustion, outputStream);
            for (int i = 0; i < 4; i++) {
                /*
                    In order to send the right answer the currentRightAnswer position
                    Checking if its in the i position and if it is sending it
                    That way it will be send 1-4 place in the strings
                 */
                if(currentRightAnswer == i)
                    ConnectionMethods.sendString(muQuestion.getRightAnswer(), outputStream);
                if(i < 3)
                    ConnectionMethods.sendString(muQuestion.getWrongAnwesers()[i], outputStream);
            }
        }else
            /*
                If the question number is bigger than the total questions in the question list
                Send final int FAIL to the outpuStream
             */
            outputStream.write(FAIL);
    }

    /**
     * Adds question to the list
     * @param question the question object to add to the list
     */
    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    /**
     * Inner class Question that will be used for the quiz
     */
    public static class Question{
        private String qustion;
        private String[] wrongAnwesers = new String[3];
        private String rightAnswer;

        /*
            Getters and setters
         */
        public void setQustion(String qustion) {
            this.qustion = qustion;
        }

        public void setRightAnswer(String rightAnswer) {
            this.rightAnswer = rightAnswer;
        }

        private String[] getWrongAnwesers() {
            return wrongAnwesers;
        }

        private String getRightAnswer() {
            return rightAnswer;
        }

        public void setWrongAnweser(int index, String wrongAnwesers) {
            this.wrongAnwesers[index] = wrongAnwesers;
        }

        public void setWrongAnwesers(String[] answer) {
            this.wrongAnwesers = answer;
        }
    }

}
