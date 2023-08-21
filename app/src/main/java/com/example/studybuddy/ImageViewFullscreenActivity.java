package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageViewFullscreenActivity extends AppCompatActivity {
    private ImageButton buttonBack;
    private ImageView imageView;
    private String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_fullscreen);
        Intent intent = getIntent();
        imageUrl = intent.getStringExtra("imageUrl");

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackFullScreen);
        buttonBack.setOnClickListener(view -> finish());

        imageView = (ImageView) findViewById(R.id.imageViewFullscreen);
        Glide.with(this).load(imageUrl).into(imageView);
    }
}