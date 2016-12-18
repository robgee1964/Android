package com.robg.android.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by robg on 17/12/16.
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
