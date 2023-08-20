package com.example.studybuddy.ui.home;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.studybuddy.R;
import com.example.studybuddy.model.Post;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class NewPostActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private Button buttonUpload;
    private ImageView photoPost;
    private EditText textPost;
    private VideoView videoPost;
    private EditText textDescription;
    private EditText textLocation;
    private EditText textHashtags;
    private Switch shareSwitch;
    private Switch commentsSwitch;
    private LinearLayout linearLayoutTextPost;
    private LinearLayout linearLayoutVideoPost;
    private Uri uri;
    private String type;
    private MediaPlayer mediaPlayer;
    private static final int VIDEO_REQUEST = 101;

    private FirebaseUser user;
    private DatabaseReference refDatabase;
    private StorageReference refStorage;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        refStorage = FirebaseStorage.getInstance().getReference();

        final String pageId = getIntent().getStringExtra("pageId");

        if(pageId != null) refDatabase = FirebaseDatabase.getInstance().getReference("pages_posts").child(pageId);
        else refDatabase = FirebaseDatabase.getInstance().getReference("posts");

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackNewPost);
        buttonBack.setOnClickListener(view -> onBackPressed());

        buttonUpload = (Button) findViewById(R.id.buttonNewPostUpload);
        buttonUpload.setOnClickListener(view -> onClickUpload());

        linearLayoutTextPost = (LinearLayout) findViewById(R.id.linearLayoutInputTextNewPost);
        linearLayoutVideoPost = (LinearLayout) findViewById(R.id.linearLayoutVideoView);

        photoPost = (ImageView) findViewById(R.id.imageViewUploadNewPost);
        photoPost.setOnClickListener(view -> choosePost());

        textPost = (EditText) findViewById(R.id.editTextNewPostText);
        videoPost = (VideoView) findViewById(R.id.videoViewNewPost);
        textLocation = (EditText) findViewById(R.id.editTextNewPostLocation);
        textHashtags = (EditText) findViewById(R.id.editTextNewPostHashtags);
        textDescription = (EditText) findViewById(R.id.editTextNewPostDescription);
        shareSwitch = (Switch) findViewById(R.id.switchShareNewPost);
        commentsSwitch = (Switch) findViewById(R.id.switchCommentsNewPost);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Otpremanje posta!");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
    }
    private void onClickUpload(){
        progressDialog.show();

        if(type == null){
            Toast.makeText(this, "Izaberi post!", Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            return;
        }
        final String description = textDescription.getText().toString().trim();
        final String location = textLocation.getText().toString().trim();

        List<String> hashtags = new ArrayList<>();
        String[] hashList = textHashtags.getText().toString().split("#");
        for(String hash:hashList){
            hash = hash.trim();
            hash = hash.toLowerCase();
            if(!hash.isEmpty()) hashtags.add(hash);
        }

        final boolean share = shareSwitch.isChecked();
        final boolean comments = commentsSwitch.isChecked();

        if(type.equals(Post.POST_TYPE_TEXT)){
            final String text = textPost.getText().toString().trim();
            if(text.isEmpty()){
                textPost.setError("Unesite tekst!");
                return;
            }
            final String postId = refDatabase.push().getKey();
            Post post = new Post(postId, user.getUid(), type, text, location, hashtags, description, share, comments, true);
            refDatabase.child(postId).setValue(post)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Toast.makeText(this, "Uspešno otpremanje!", Toast.LENGTH_SHORT).show();
                            finish();
                        }else Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    });
        }else {
            uploadImageOrVideo(location, description, hashtags, share, comments);
        }

    }
    private void  uploadImageOrVideo(String location, String description, List<String> hashtags, boolean share, boolean comments){
        StorageReference storage = refStorage.child("posts "+type+"/"+uri.getLastPathSegment());
        UploadTask uploadTask = storage.putFile(uri);

        uploadTask.addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnSuccessListener(uri->{
                final String postLink = uri.toString();
                final String postId = refDatabase.push().getKey();
                Post post = new Post(postId, user.getUid(),type, postLink, location, hashtags, description, share, comments, true);

                refDatabase.child(postId).setValue(post)
                        .addOnCompleteListener(task1 -> {
                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                Toast.makeText(this, "Uspešno otpremanje!", Toast.LENGTH_SHORT).show();
                                finish();
                            }else Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        });

            }).addOnFailureListener(e ->{
                progressDialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }
    private void choosePost(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewDialog = LayoutInflater.from(this).inflate(R.layout.alert_dialog_post, null);
        builder.setView(viewDialog);
        final AlertDialog dialog = builder.create();
        LinearLayout layoutImage = viewDialog.findViewById(R.id.linearLayout1);
        layoutImage.setOnClickListener(view -> chooseImage(dialog));

        LinearLayout layoutVideo = viewDialog.findViewById(R.id.linearLayout2);
        layoutVideo.setOnClickListener(view -> chooseVideo(dialog));

        LinearLayout layoutText = viewDialog.findViewById(R.id.linearLayout3);
        layoutText.setOnClickListener(view -> chooseText(dialog));

        Button back = viewDialog.findViewById(R.id.buttonAlertDialogCancel);
        back.setOnClickListener(view -> {dialog.dismiss();});

        dialog.show();
    }
    private void chooseImage(AlertDialog dialog){
        dialog.dismiss();
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }
    private void chooseVideo(AlertDialog dialog){
        dialog.dismiss();
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 120);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), VIDEO_REQUEST);
    }
    private void chooseText(AlertDialog dialog){
        dialog.dismiss();
        photoPost.setVisibility(View.GONE);
        linearLayoutTextPost.setVisibility(View.VISIBLE);
        type= Post.POST_TYPE_TEXT;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == ImagePicker.REQUEST_CODE) {
                uri = data.getData();
                photoPost.setImageURI(uri);
                photoPost.setEnabled(false);
                type = Post.POST_TYPE_IMAGE;
            } else if(requestCode == VIDEO_REQUEST) {
                uri = data.getData();

                photoPost.setVisibility(View.GONE);

                linearLayoutVideoPost.setVisibility(View.VISIBLE);

                videoPost.setVideoURI(uri);

                videoPost.setOnPreparedListener(mediaPlayer -> {
                    this.mediaPlayer = mediaPlayer;
                    mediaPlayer.start();
                    mediaPlayer.pause();
                });

                videoPost.setOnClickListener(view -> {
                    if(mediaPlayer.isPlaying()) { mediaPlayer.pause(); }
                    else { mediaPlayer.start(); }
                });

                videoPost.setOnCompletionListener(mediaPlayer -> mediaPlayer.start());

                type = Post.POST_TYPE_VIDEO;
            }
        }
    }
}