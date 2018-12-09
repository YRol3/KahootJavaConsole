package com.finalproject.objects;

import com.finalproject.logic.ConnectionMethods;
import com.finalproject.interfaces.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Writable {

    public static final int OKAY = 10;
    public static final int FAIL = -1;
    List<Question>  questions = new ArrayList<>();
    String quizPassword;
    String quizName;
    private static final int QUESTION = 0;
    private static final int RIGHT_ANSWER = 1;
    public Quiz(InputStream inputStream) throws IOException{
        this.quizPassword = ConnectionMethods.getString(inputStream);
        this.quizName = ConnectionMethods.getString(inputStream);
        int questionArraySize = ConnectionMethods.getInt(inputStream);
        for (int i = 0; i < questionArraySize; i++) {
            Question question = new Question();
            for (int j = 0; j < 5; j++) {
                String inputString = ConnectionMethods.getString(inputStream);
                switch (j){
                    case QUESTION: question.setQustion(inputString);
                        break;
                    case RIGHT_ANSWER: question.setRightAnswer(inputString);
                        break;
                    default: question.setWrongAnweser(j-2, inputString);
                        break;
                }
            }
            this.questions.add(question);
        }
    }

    public String getQuizName() {
        return quizName;
    }

    public String getQuizPassword() {
        return quizPassword;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Quiz(){}

    public Quiz(String quitPassword, String quizName) {
        this.quizPassword = quitPassword;
        this.quizName = quizName;
    }
    /*
            Writing & reading order:
            a. password bytes length
            b. password bytes
            c. quiz name bytes length
            d. quiz name bytes
            1. questions list size
            2. question bytes length
            3. question bytes
            4. right answer bytes length
            5. right answer bytes
            6. wrong answer bytes length
            7. wrong answer bytes
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

    public void sendQuestion(OutputStream outputStream, int qusNum, int currentRightAnswer) throws IOException{
        if(qusNum<questions.size()) {
            Question muQuestion = questions.get(qusNum);
            outputStream.write(OKAY);
            ConnectionMethods.sendString(muQuestion.qustion, outputStream);
            for (int i = 0; i < 4; i++) {
                if(currentRightAnswer == i)
                    ConnectionMethods.sendString(muQuestion.getRightAnswer(), outputStream);
                if(i < 3)
                    ConnectionMethods.sendString(muQuestion.getWrongAnwesers()[i], outputStream);
            }
        }else
            outputStream.write(FAIL);
    }
    public static class Question{
        private String qustion;
        private String[] wrongAnwesers = new String[3];
        private String rightAnswer;

        private void setQustion(String qustion) {
            this.qustion = qustion;
        }

        private void setRightAnswer(String rightAnswer) {
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
    }

}
