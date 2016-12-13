package com.bignerdranch.android.geoquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class CheatActivity extends AppCompatActivity {

    private static final String EXTRA_ANSWER_IS_TRUE =
            "com.bignerdranch.android.geoquiz.answer_is_true";
    public static final String EXTRA_ANSWER_SHOWN =
            "com.bignerdranch.android.geoquiz.answer_shown";

    private static final String TAG = "CheatActivity";
    private static final String ANSWER_SHOWN_INDEX = "answer_shown";

    private boolean mAnswerShown;
    private boolean answerIsTrue;
    private TextView mAnswerText;
    private TextView mAPIText;
    private Button mShowAnswer;

    public static Intent newIntent(Context packageContext, boolean answerIsTrue) {
        Intent i = new Intent(packageContext, CheatActivity.class);
        i.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
        return i;
    }

    private void setExtraAnswerShownResult(boolean isAnswerShown) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown);
        setResult(RESULT_OK, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAnswerShown = savedInstanceState.getBoolean(ANSWER_SHOWN_INDEX, false);
            Log.i(TAG, ("InstanceState.get " + mAnswerShown));
        }

        setContentView(R.layout.activity_cheat);



        answerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);

        mAnswerText = (TextView) findViewById(R.id.txtview_question);
        mShowAnswer = (Button) findViewById(R.id.btn_showAnswer);
        mAPIText = (TextView) findViewById(R.id.txtViewAPIlevel);

        setExtraAnswerShownResult(mAnswerShown);
        if (mAnswerShown) {
            setAnswerText(answerIsTrue);
        }

        mAPIText.setText("API level " + Build.VERSION.SDK_INT);

        mShowAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnswerText(answerIsTrue);
                mAnswerShown = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    int cx = mShowAnswer.getWidth() / 2;
                    int cy = mShowAnswer.getHeight() / 2;
                    float radius = mShowAnswer.getWidth();
                    Animator anim = ViewAnimationUtils
                            .createCircularReveal(mShowAnswer, cx, cy, radius, 0);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mAnswerText.setVisibility(View.VISIBLE);
                            mShowAnswer.setVisibility(View.INVISIBLE);
                        }
                    });
                    anim.start();
                } else {
                    mAnswerText.setVisibility(View.VISIBLE);
                    mShowAnswer.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void setAnswerText(boolean answerIsTrue)
    {
        if (answerIsTrue) {
            mAnswerText.setText(R.string.true_button);
        }
        else {
            mAnswerText.setText(R.string.false_button);
        }
        setExtraAnswerShownResult(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putBoolean(ANSWER_SHOWN_INDEX, mAnswerShown);
    }
}
