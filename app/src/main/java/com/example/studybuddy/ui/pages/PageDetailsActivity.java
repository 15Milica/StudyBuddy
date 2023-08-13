package com.example.studybuddy.ui.pages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Page;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Collections;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PageDetailsActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private Button buttonSave;
    private Button buttonLeave;
    private String pageId;
    private CircleImageView photoGroup;
    private EditText pageName;
    private EditText pageDescription;
    private TextView pageMembers;
    private TextView pageAdmins;
    private FirebaseUser user;
    private DatabaseReference refDatabase;
    private StorageReference refStorage;
    private ProgressBar progressBar;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_details);


        progressDialog = new ProgressDialog(this);
        imageUri = null;

        progressBar = (ProgressBar) findViewById(R.id.progressBarPageDetails);

        pageId = getIntent().getStringExtra("pageId");
        user = FirebaseAuth.getInstance().getCurrentUser();
        refDatabase = FirebaseDatabase.getInstance().getReference("pages").child(pageId);
        refStorage = FirebaseStorage.getInstance().getReference("page photos/"+pageId);

        buttonSave = (Button) findViewById(R.id.buttonPageDetailsSave);
        buttonSave.setOnClickListener(view -> onClickSave());

        buttonLeave = (Button) findViewById(R.id.buttonPageDetailsLeave);
        buttonLeave.setOnClickListener(view -> onClickLeave());

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackDetails);
        buttonBack.setOnClickListener(view -> onBackPressed());

        photoGroup = (CircleImageView) findViewById(R.id.pagePhotoDetails);
        photoGroup.setOnClickListener(view -> onClickPhoto());

        pageName = (EditText) findViewById(R.id.editTextPageDetailsName);
        pageName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!pageName.getText().toString().isEmpty()) buttonSave.setVisibility(View.VISIBLE);
                else buttonSave.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        pageDescription = (EditText) findViewById(R.id.editTextPageDetailsDescription);
        pageDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(pageDescription.getText().length() <= 10){
                    pageDescription.setText("Ukratko opišite grupu!");
                    buttonSave.setVisibility(View.GONE);
                }
                else buttonSave.setVisibility(View.VISIBLE);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        pageMembers = (TextView) findViewById(R.id.textViewPageDetailsMembers);
        pageAdmins = (TextView) findViewById(R.id.textViewPageDetailsAdmins);

        setDetails();
    }
    private void setDetails(){
        progressBar.setVisibility(View.VISIBLE);

        refDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Page page = snapshot.getValue(Page.class);

                pageName.setText(page.getPageName());
                pageDescription.setText(page.getPageDescription());

                if(page.getPagePhoto().equals("default")) photoGroup.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                else Glide.with(getApplicationContext()).load(page.getPagePhoto()).into(photoGroup);

                if(page.getMembers().get(user.getUid()).equals("member")){
                    buttonLeave.setVisibility(View.VISIBLE);
                    buttonSave.setVisibility(View.GONE);
                    pageName.setEnabled(false);
                    pageDescription.setEnabled(false);
                    photoGroup.setEnabled(false);
                } else {
                    buttonLeave.setVisibility(View.GONE);
                    buttonSave.setVisibility(View.GONE);
                    pageName.setEnabled(true);
                    pageDescription.setEnabled(true);
                    photoGroup.setEnabled(true);
                }

                int number_of_members = Collections.frequency(page.getMembers().values(), "member");
                int number_of_admins = Collections.frequency(page.getMembers().values(), "admin");

                pageMembers.setText(String.valueOf(number_of_members));
                pageAdmins.setText(String.valueOf(number_of_admins));

                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });

    }
    private void onClickSave(){
        if(!Check.networkConnect(getApplicationContext())) {
            Toast.makeText(this, "Greška: nema mreže!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String name = pageName.getText().toString();
        final String description = pageDescription.getText().toString();

        if(name.isEmpty()){
            pageName.setError("Unesite ispravno ima grupe!");
            return;
        }
        if(description.length() <= 10){
            pageDescription.setError("Ukratko opišite grupu!");
            return;
        }
        final String search = name;

        progressDialog.setTitle("Izmena podataka o grupi!");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        HashMap<String, Object> map = new HashMap<>();
        map.put("pageName", name);
        map.put("pageDescription", description);
        map.put("search", search);

        refDatabase.updateChildren(map)
                .addOnCompleteListener(task ->{
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        if(imageUri != null) updatePhoto();
                        else Toast.makeText(this, "Uspešno promenjeni podaci!", Toast.LENGTH_LONG).show();
                    }
                    else Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void onClickLeave(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.alert_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        Button buttonConfirm = view.findViewById(R.id.confirm_button);
        Button buttonCancel = view.findViewById(R.id.cancel_button);
        TextView text = view.findViewById(R.id.textViewAlertDialog);
        text.setText("Želite li da napustite grupu?");
        buttonConfirm.setText("Napusti");
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refDatabase.child("members").child(user.getUid()).removeValue()
                                .addOnCompleteListener(task-> {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Uspešno ste napustili grupu.", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK, new Intent());
                                        finish();
                                    } else { Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show(); }
                                });
                dialog.dismiss();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void onClickPhoto(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080,1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            Uri uri = data.getData();
            photoGroup.setImageURI(uri);
            imageUri = uri;
            buttonSave.setVisibility(View.VISIBLE);
        }
    }
    private void updatePhoto(){
        progressDialog.setTitle("Otpremanje slike");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        UploadTask uploadTask = refStorage.putFile(imageUri);
        uploadTask.addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }).addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnSuccessListener(uri ->{
                String imageLink = uri.toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("pagePhoto", imageLink);
                refDatabase.updateChildren(map);

                imageUri = null;
                Toast.makeText(this, "Uspešno promenjena podaci.", Toast.LENGTH_SHORT).show();
            });
        });
    }
}