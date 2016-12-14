package com.robg.android.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by robg on 10/12/16.
 */

public class Crime {
    private UUID mID;
    private String mTitle;
    private Date mdate;
    private boolean mSolved;

    public Crime() {
        this.mID = UUID.randomUUID();
        this.mdate = new Date();
    }

    public UUID getmID() {
        return mID;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }
}
