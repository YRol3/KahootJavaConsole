package com.finalproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Quiz implements Writable {

    public static final int OKAY = 10;
    public static final int FAIL = -1;
    List<Question>  questions = new ArrayList<>();
    String quitPassword;
    String quizName;
    private static final int QUESTION = 0;
    private static final int RIGHT_ANSWER = 1;
    private static final int WRONG_ANSWER_1 = 2;
    private static final int WRONG_ANSWER_2 = 3;
    private static final int WRONG_ANSWER_3 = 4;
    public Quiz(InputStream inputStream) throws IOException{
        int questionPasswordSize = inputStream.read();
        byte[] buffer = new byte[questionPasswordSize];
        int actuallyRead = inputStream.read(buffer);
        if(actuallyRead != questionPasswordSize)
            throw new IOException("Something went wrong");
        this.quitPassword = new String(buffer);
        int questionNameSize = inputStream.read();
        buffer = new byte[questionNameSize];
        actuallyRead = inputStream.read(buffer);
        if(actuallyRead != questionNameSize)
            throw new IOException("Something went wrong");
        this.quizName = new String(buffer);
        int questionArraySize = inputStream.read();
        String[] strings = new String[5];
        for (int i = 0; i < questionArraySize; i++) {
            Question question = new Question();
            for (int j = 0; j < strings.length; j++) {
                int stringSize = inputStream.read();
                buffer = new byte[stringSize];
                actuallyRead = inputStream.read(buffer);
                if(actuallyRead !=  stringSize)
                    throw new RuntimeException("Wrong InputStream");
                strings[j] = new String(buffer);
            }
            question.setQustion(strings[QUESTION]);
            question.setRightAnswer(strings[RIGHT_ANSWER]);
            question.wrongAnwesers[0] = strings[WRONG_ANSWER_1];
            question.wrongAnwesers[1] = strings[WRONG_ANSWER_2];
            question.wrongAnwesers[2] = strings[WRONG_ANSWER_3];
            this.questions.add(question);
        }
    }
    public Quiz(){}

    public Quiz(String quitPassword, String quizName) {
        this.quitPassword = quitPassword;
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
    public void addQuestion(Question question){
        questions.add(question);
    }
    @Override
    public void write(OutputStream outputStream) throws IOException {
        outputStream.write(quitPassword.getBytes().length);
        outputStream.write(quitPassword.getBytes());
        outputStream.write(quizName.getBytes().length);
        outputStream.write(quizName.getBytes());
        outputStream.write(questions.size());
        for(Question question: questions){
            outputStream.write(question.qustion.getBytes().length);
            outputStream.write(question.qustion.getBytes());
            outputStream.write(question.rightAnswer.getBytes().length);
            outputStream.write(question.rightAnswer.getBytes());
            for(String wAnwsers: question.wrongAnwesers){
                outputStream.write(wAnwsers.getBytes().length);
                outputStream.write(wAnwsers.getBytes());
            }
        }
    }

    public void write(OutputStream outputStream, int qusNum, int currentRightAnswer) throws IOException{
        if(qusNum<questions.size()) {
            outputStream.write(OKAY);
            outputStream.write(questions.get(qusNum).qustion.getBytes().length);
            outputStream.write(questions.get(qusNum).qustion.getBytes());
            for (int i = 0; i < 4; i++) {
                if(currentRightAnswer == i){
                    outputStream.write(questions.get(qusNum).getRightAnswer().getBytes().length);
                    outputStream.write(questions.get(qusNum).getRightAnswer().getBytes());
                }
                if(i < 3) {
                    outputStream.write(questions.get(qusNum).getWrongAnwesers()[i].getBytes().length);
                    outputStream.write(questions.get(qusNum).getWrongAnwesers()[i].getBytes());
                }
            }
        }else{
            outputStream.write(FAIL);
        }
    }
    public static class Question{
        private String qustion;
        private String[] wrongAnwesers = new String[3];
        private String rightAnswer;

        public void setQustion(String qustion) {
            this.qustion = qustion;
        }

        public void setRightAnswer(String rightAnswer) {
            this.rightAnswer = rightAnswer;
        }

        public String[] getWrongAnwesers() {
            return wrongAnwesers;
        }

        public String getRightAnswer() {
            return rightAnswer;
        }
    }

}
