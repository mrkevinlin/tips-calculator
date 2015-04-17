package com.scepticallistic.kevin.tipcalc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}

