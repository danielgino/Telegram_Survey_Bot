package org.example;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Question {

    private String question;
    private List<String> options;
    private int questionNumber;
private static  int counter=1;



    public Question() {
        this.options = new LinkedList<>();
        this.questionNumber=counter++;

    }


    public String getQuestion() {
        return question;
    }

    public void addQuestion(String question){
        this.question = question;
    }
    public void addOption(String option) {
            this.options.add(option);
    }

    public List<String> getOptions() {
        return options;
    }
    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.question).append('\n');
        for (int i = 0; i <options.size() ; i++) {
            sb.append(i+1).append(". ").append(options.get(i)).append('\n');
        }

        return sb.toString();
    }
}
