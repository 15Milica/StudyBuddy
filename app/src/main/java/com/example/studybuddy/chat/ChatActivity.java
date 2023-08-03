package com.example.studybuddy.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.studybuddy.R;

public class ChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private ImageButton buttonOptions;
    private ImageButton buttonCamera;
    private ImageButton buttonMic;
    private ImageButton buttonSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        buttonOptions = (ImageButton) findViewById(R.id.imageButtonOptions);
        buttonCamera = (ImageButton) findViewById(R.id.imageButtonSendPhotoChat);
        buttonMic = (ImageButton) findViewById(R.id.imageButtonSendSoundChat);
        buttonSend =(ImageButton) findViewById(R.id.imageButtonSendMessage);

        editTextMessage = (EditText) findViewById(R.id.editTextSendMessageChat);
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isEmpty())
                {
                    buttonCamera.setVisibility(View.GONE);
                    buttonMic.setVisibility(View.GONE);
                    buttonSend.setVisibility(View.VISIBLE);
                }else{
                    buttonCamera.setVisibility(View.VISIBLE);
                    buttonMic.setVisibility(View.VISIBLE);
                    buttonSend.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        buttonOptions.setOnClickListener(view -> onClickOptions());


    }
    private void onClickOptions() {
        buttonOptions.setEnabled(false);
        Intent intent = new Intent(this, ChatSettingsActivity.class);
        startActivity(intent);
    }
    private boolean isEmpty() {
        return editTextMessage.getText().toString().trim().length() == 0;
    }
    @Override
    protected void onResume() {
        super.onResume();
        buttonOptions.setEnabled(true);
    }
}