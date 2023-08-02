package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class VerificationActivity extends AppCompatActivity {

    private ImageButton buttonVerification;
    private EditText text1;
    private EditText text2;
    private EditText text3;
    private EditText text4;
    private EditText text5;
    private EditText text6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);


        buttonVerification = (ImageButton) findViewById(R.id.imageButtonVerification);

        text1 = (EditText) findViewById(R.id.editTextNumberVerification1);
        text2 = (EditText) findViewById(R.id.editTextNumberVerification2);
        text3 = (EditText) findViewById(R.id.editTextNumberVerification3);
        text4 = (EditText) findViewById(R.id.editTextNumberVerification4);
        text5 = (EditText) findViewById(R.id.editTextNumberVerification5);
        text6 = (EditText) findViewById(R.id.editTextNumberVerification6);


        text1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (text1.length() == 1) text2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        text2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (text2.length() == 1) text3.requestFocus();
                if (text2.length() == 0) text1.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        text3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (text3.length() == 1) text4.requestFocus();
                if (text3.length() == 0) text2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        text4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (text4.length() == 1) text5.requestFocus();
                if (text4.length() == 0) text3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        text5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (text5.length() == 1) text6.requestFocus();
                if (text5.length() == 0) text4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        text6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (text6.length() == 0) text5.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        buttonVerification.setOnClickListener(view -> onButtonClick());
    }

    private void onButtonClick()
    {
        final String textVerification = text1.getText().toString() +
                                       text2.getText().toString() +
                                       text3.getText().toString() +
                                       text4.getText().toString() +
                                       text5.getText().toString() +
                                       text6.getText().toString();
        if(textVerification.length() == 6){
            Intent intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
        }else
            Toast.makeText(getApplicationContext(), "Unesite kod!", Toast.LENGTH_LONG).show();
    }
}