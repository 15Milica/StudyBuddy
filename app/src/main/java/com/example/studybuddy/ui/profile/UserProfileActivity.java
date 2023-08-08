package com.example.studybuddy.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.studybuddy.R;
import com.example.studybuddy.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textFirstName;
    private TextView textLastName;
    private TextView textBirthday;
    private TextView textDescription;
    private CircleImageView imagePhoto;
    private TextView textTitle;
    private ImageButton buttonSettings;
    private ImageButton imageButtonBack;

    private DatabaseReference refDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        refDatabase = FirebaseDatabase.getInstance().getReference();

        userId = getIntent().getStringExtra("userId");

        textTitle = (TextView) findViewById(R.id.textSearch);
        textFirstName = (TextView) findViewById(R.id.textViewUserProfileFirstName);
        textLastName  = (TextView) findViewById(R.id.textViewUserProfileLastName);
        textBirthday = (TextView) findViewById(R.id.textViewDateUserProfile);
        textDescription = (TextView) findViewById(R.id.textViewMultiLineUserProfile);
        imagePhoto = (CircleImageView) findViewById(R.id.imageViewUserProfilePhoto);

        buttonSettings = (ImageButton) findViewById(R.id.imageButtonOptionsUserProfile);
        imageButtonBack =(ImageButton) findViewById(R.id.imageButtonBackUserProfile);

        imageButtonBack.setOnClickListener(view -> onBackPressed());

        setDetailsUserProfile();
    }
    private void setDetailsUserProfile(){
        refDatabase.child("users").child(userId).get()
                .addOnCompleteListener(task ->{
                    if(task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        User u = snapshot.getValue(User.class);

                        textTitle.setText(u.getSearch());
                        textFirstName.setText(u.getFirstName());
                        textLastName.setText(u.getLastName());
                        textBirthday.setText(u.getBirthday());
                        textDescription.setText(u.getDescription());

                        String photoLink = u.getPhoto();
                        if(!photoLink.equals("default")){
                            Glide.with(getApplicationContext())
                                    .load(photoLink)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .dontAnimate()
                                    .into(imagePhoto);
                        }else imagePhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                    }
                });
    }
}