package com.example.studybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.security.Provider;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    private ImageButton buttonVerification;
    private EditText text1;
    private EditText text2;
    private EditText text3;
    private EditText text4;
    private EditText text5;
    private EditText text6;

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();

        String phone = getIntent().getStringExtra("PHONE");

        progressDialog = new ProgressDialog(this);

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

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();

                Toast.makeText(getApplicationContext(), "Greska!" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                progressDialog.dismiss();

                // Toast.makeText("Daa je kod poslat Stefiju!");

                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(false);

        Verification(phone);
    }

    private void Verification(String phoneNumber) {
        progressDialog.setTitle("Verifikacija u toku");
        progressDialog.setMessage("Molimo Vas sacekajte Stefija foo-a...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Verification done", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(getApplicationContext(), CreateProfileActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    } else {
                        String e = task.getException().toString();
                        Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void onButtonClick() {
        final String verificationCode = text1.getText().toString() +
                                       text2.getText().toString() +
                                       text3.getText().toString() +
                                       text4.getText().toString() +
                                       text5.getText().toString() +
                                       text6.getText().toString();


        if(verificationCode.length() == 6) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
            signInWithPhoneAuthCredential(credential);
        } else Toast.makeText(getApplicationContext(), "Unesite kod!", Toast.LENGTH_LONG).show();
    }
}