package com.bignerdranch.android.geoquiz;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class QuizActivity extends AppCompatActivity {

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String CHEAT_INDEX = "cheat";

    private TrueFalse[] mQuestionBank = new TrueFalse[]{
            new TrueFalse(R.string.question_oceans, true),
            new TrueFalse(R.string.question_mideast, false),
            new TrueFalse(R.string.question_africa, false),
            new TrueFalse(R.string.question_americas, true),
            new TrueFalse(R.string.question_asia, true)
    };

    private int mCurrentIndex = 0;
    private boolean mIsCheater;

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getQuestion();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isTrueQuestion();

        int toastMsgId;

        if (mIsCheater || mQuestionBank[mCurrentIndex].isCheated()){
            toastMsgId = R.string.judgement_toast;
            mQuestionBank[mCurrentIndex].setCheated(true);
            if (answerIsTrue == userPressedTrue) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
            }
        }
        else if (answerIsTrue == userPressedTrue) {
            toastMsgId = R.string.correct_toast;
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            updateQuestion();
        } else {
            toastMsgId = R.string.incorrect_toast;
        }
        Toast.makeText(this, toastMsgId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        mIsCheater = data.getBooleanExtra(CheatActivity.EXTRA_ANSWER_SHOWN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean(CHEAT_INDEX, false);
        }
        setContentView(R.layout.activity_quiz);
        Log.d(TAG, "OnCreate() called");

        mFalseButton = (Button) findViewById(R.id.btn_false);
        mTrueButton = (Button) findViewById(R.id.btn_true);
        mNextButton = (ImageButton) findViewById(R.id.btn_next);
        mPrevButton = (ImageButton) findViewById(R.id.btn_prev);
        mQuestionTextView = (TextView) findViewById(R.id.txtview_question);
        mCheatButton = (Button) findViewById(R.id.btn_cheat);

        updateQuestion();

        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(false);
            }
        });

        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(true);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsCheater = false;
                if (mCurrentIndex > 0) {
                    mCurrentIndex = (mCurrentIndex - 1);
                }
                else {
                    mCurrentIndex = mQuestionBank.length - 1;
                }
                updateQuestion();
            }
        });

        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start cheat activity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isTrueQuestion();
                Intent i = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(i, 0);
            }
        });
     }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called" );
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called" );
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called" );
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called" );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called" );
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBoolean(CHEAT_INDEX, mIsCheater);
    }

}
