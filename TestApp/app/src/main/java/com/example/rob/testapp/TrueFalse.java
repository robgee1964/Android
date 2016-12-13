package com.example.rob.testapp;

/**
 * Created by Rob on 13/11/2016.
 */

public class TrueFalse extends Object {
    private int mQuestion;
    private boolean mTrueQuestion;

    public TrueFalse(int question, boolean trueQuestion) {
        mQuestion = question;
        mTrueQuestion = trueQuestion;
    }

    public void setQuestion(int question) {
        mQuestion = question;
    }

    public void setTrueQuestion(boolean trueQuestion) {
        mTrueQuestion = trueQuestion;
    }

    public int getQuestion() {

        return mQuestion;
    }

    public boolean isTrueQuestion() {
        return mTrueQuestion;
    }
}
