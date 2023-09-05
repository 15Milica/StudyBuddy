package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;

import com.example.studybuddy.model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class CreateProfileActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    private EditText textFirstName;
    private EditText textLastName;
    private EditText textBirthday;
    private EditText textDescription;
    private CircleImageView imageButton;

    private Uri imageUri;
    private Button button;

    private ProgressDialog progressDialog;
    private String userID;
    private String numberPhone;
    private DatabaseReference refDatabase;
    private StorageReference refStorage;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        progressDialog = new ProgressDialog(this);
        sessionManager = new SessionManager(getApplicationContext());

        refDatabase = FirebaseDatabase.getInstance().getReference();
        refStorage = FirebaseStorage.getInstance().getReference();

        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        numberPhone = user.getPhoneNumber();

        button = (Button) findViewById(R.id.buttonCreateProfile);
        imageButton =(CircleImageView) findViewById(R.id.profilePhotoCreateProfile);
        textFirstName = (EditText) findViewById(R.id.editTextProfileFirstName);
        textLastName = (EditText) findViewById(R.id.editTextProfileLastName);
        textBirthday = (EditText) findViewById(R.id.editTextDateProfile);
        textDescription = (EditText) findViewById(R.id.editTextMultiLineProfile);

        textFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final String first_name = textFirstName.getText().toString();
                if(!Check.validFirstName(first_name)) textFirstName.setError("Unesite ime!");
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        textLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final String last_name = textLastName.getText().toString();
                if(!Check.validLastName(last_name)) textLastName.setError("Unesite prezime!");
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        imageButton.setOnClickListener(view -> onClickImage());
        textBirthday.setOnClickListener(view -> onClickBirthday());

        button.setOnClickListener(view -> onClickContinue());

    }
    private void onClickContinue() {
        final String first_name = textFirstName.getText().toString();
        final String last_name = textLastName.getText().toString();
        final String birthday = textBirthday.getText().toString();
        final String description = textDescription.getText().toString();

        boolean k = true;
        if(!Check.validFirstName(first_name)){
            k = false;
            textFirstName.setError("Unesite ime!");
        }
        if(!Check.validLastName(last_name)){
            k = false;
            textLastName.setError("Unesite prezime!");
        }
        if(!Check.validDate(birthday)){
            k = false;
            textBirthday.setError("Unesite datum rođenja!");
        }
        if(description.length() <= 10){
            k = false;
            textDescription.setError("Recite nešto više o sebi!");
        }
        if(!k) return;
        final String search =  first_name.toLowerCase() + " " + last_name.toLowerCase();

        if(Check.networkConnect(getApplicationContext())) {createProfile(first_name, last_name, birthday, description, search);}
        else Toast.makeText(getApplicationContext(),"Greška: nema mreže!", Toast.LENGTH_SHORT).show();

    }

    private void uploadImage(){
        progressDialog.setTitle("Otpremanje profilne slike");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference userRef = refStorage.child("profile photos/" + userID);
        UploadTask uploadTask = userRef.putFile(imageUri);

        uploadTask.addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Greška: " + e, Toast.LENGTH_LONG).show();
            startMainActivity();
        }).addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();

            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnSuccessListener(uri -> {
                String photoLink = uri.toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("photo", photoLink);
                refDatabase.child("users").child(userID).updateChildren(map);
            });

            startMainActivity();
        });
    }
    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }
    private void createProfile(String first_name, String last_name, String birthaday, String description, String search) {
        progressDialog.setTitle("Kreiranje profila");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        User u = new User(userID, first_name, last_name, birthaday, description, numberPhone, search);
        refDatabase.child("users").child(userID).setValue(u)
                .addOnFailureListener(e ->{
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Greška: "+ e.getMessage(),Toast.LENGTH_LONG).show();
                })
                .addOnSuccessListener(aVoid ->{
                    progressDialog.dismiss();
                    sessionManager.createSession();
                    if(imageUri != null) uploadImage();
                    else startMainActivity();
                });

    }

    private void onClickBirthday(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                DatePickerDialog.THEME_HOLO_LIGHT,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        final String date = (month + 1) + "/" + dayOfMonth + "/" + year;
        textBirthday.setText(date);
    }

    private void onClickImage(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                imageButton.setImageURI(selectedImageUri);
                imageUri = selectedImageUri;
            }
        }
    }

}