package com.scepticallistic.kevin.tipscalc;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView functionView = (TextView)findViewById(R.id.function_help);
        functionView.setText(Html.fromHtml(getString(R.string.functions_help_text)));

        TextView splitView = (TextView)findViewById(R.id.split_help);
        splitView.setText(Html.fromHtml(getString(R.string.split_help_text)));

        Button feedback = (Button) findViewById(R.id.feedback_button);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AboutActivity.this, "Nothing yet! Please leave a review. Sorry!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
