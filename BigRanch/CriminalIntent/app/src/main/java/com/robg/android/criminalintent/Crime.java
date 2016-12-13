package com.robg.android.criminalintent;

import java.util.UUID;

/**
 * Created by robg on 10/12/16.
 */

public class Crime {
    private UUID mID;
    private String mTitle;

    public Crime() {
        this.mID = UUID.randomUUID();
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
}
