package com.example.studybuddy.ui.pages;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telecom.Call;
import android.widget.TextView;

import com.example.studybuddy.R;

import java.time.Instant;

public class PageActivity extends AppCompatActivity {

    private Activity mActivity;
    private TextView textViewBack;
    private TextView textViewDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        mActivity = this;

        textViewBack = (TextView) findViewById(R.id.textViewBackPage);
        textViewBack.setOnClickListener(view -> onBackPressed());

        textViewDetails = (TextView) findViewById(R.id.textViewDetailsPage);
        textViewDetails.setOnClickListener(view -> onDetails());
    }
    private void onDetails() {
        Intent intent = new Intent(mActivity, PageDetailsActivity.class);
        startActivity(intent);
    }
}