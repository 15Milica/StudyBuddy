package com.example.studybuddy.ui.pages;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Page;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPageActivity extends AppCompatActivity {

    private ImageButton imageButtonBack;
    private EditText textNameGroup;
    private EditText textDescriptionGroup;
    private CircleImageView photoGroup;
    private Button buttonCreate;
    private Uri imageUri;
    private FirebaseUser user;
    private DatabaseReference refDatabase;
    private StorageReference refStorage;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_page);
        imageUri = null;

        user = FirebaseAuth.getInstance().getCurrentUser();
        refStorage = FirebaseStorage.getInstance().getReference();
        refDatabase = FirebaseDatabase.getInstance().getReference("pages");

        imageButtonBack = (ImageButton) findViewById(R.id.imageButtonBackNewPage);
        imageButtonBack.setOnClickListener(view->onBackPressed());

        textNameGroup = (EditText) findViewById(R.id.editTextNewPageName);
        textDescriptionGroup = (EditText) findViewById(R.id.editTextNewPageDescription);

        buttonCreate = (Button) findViewById(R.id.buttonCreatePage);
        buttonCreate.setOnClickListener(view -> onClickCreate());

        photoGroup = (CircleImageView) findViewById(R.id.pagePhotoCreateGroup);
        photoGroup.setOnClickListener(view -> onClickImage());
    }
    private void onClickImage(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080,1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri selectedUri = data.getData();
            if(null != selectedUri){
                imageUri = selectedUri;
                photoGroup.setImageURI(imageUri);
            }
        }
    }
    private void uploadImage(final String pageId){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Otpremanje slike");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference ref = refStorage.child("page photos/" + pageId);
        UploadTask uploadTask = ref.putFile(imageUri);
        uploadTask.addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(this, "Greška: " + e, Toast.LENGTH_LONG).show();

        }).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

            task.addOnSuccessListener(uri->{
                String photoLink = uri.toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("pagePhoto", photoLink);
                refDatabase.child(pageId).updateChildren(map);

                imageUri = null;
                finish();
            });
        });
    }
    private void onClickCreate(){
        if(!Check.networkConnect(this)){
            Toast.makeText(this,"Greška: nema mreže!", Toast.LENGTH_LONG).show();
            return;
        }

        final String name = textNameGroup.getText().toString();
        final String description = textDescriptionGroup.getText().toString();

        if(name.isEmpty()){
            Toast.makeText(this, "Unesite ispravno ime grupe!", Toast.LENGTH_LONG).show();
            return;
        }
        if(description.length() <= 10){
            Toast.makeText(this, "Opišite namenu grupe u par rečenica.", Toast.LENGTH_LONG).show();
            return;
        }
        final String search = name.toLowerCase();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Kreiranje grupe!");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final String pageId = refDatabase.push().getKey();

        Map<String, String> members = new HashMap<>();
        members.put(user.getUid(), "admin");

        Page page = new Page(pageId,name, "default", description, search, members);

        refDatabase.child(pageId).setValue(page)
                .addOnCompleteListener(task ->{
                    if(task.isSuccessful()) {
                        if (imageUri != null) uploadImage(pageId);
                        else {
                            Toast.makeText(this, "Grupa je uspešno kreirana!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }else Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}