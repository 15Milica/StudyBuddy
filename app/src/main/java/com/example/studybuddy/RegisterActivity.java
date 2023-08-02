package com.example.studybuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;

public class RegisterActivity extends AppCompatActivity {

    private CountryCodePicker codePicker;
    private EditText phone;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        codePicker = (CountryCodePicker) findViewById(R.id.countryCodePickerRegister);
        phone = (EditText) findViewById(R.id.editTextPhoneRegister);
        button = (Button) findViewById(R.id.buttonRegister);

        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text_phone = codePicker.getSelectedCountryCodeWithPlus() + phone.getText().toString();
                if (!isValidPhone(text_phone))
                    phone.setError("Unesite ispravan broj telefona");
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        button.setOnClickListener(view -> onButtonClick());
    }

    private void onButtonClick() {
        final String text_phone = codePicker.getSelectedCountryCodeWithPlus() + phone.getText().toString();
        if(isValidPhone(text_phone)){
            button.setEnabled(false);
            Intent intent = new Intent(this, VerificationActivity.class);
            intent.putExtra("PHONE", text_phone);
            startActivity(intent);
        }
        else phone.setError("Unesite ispravan broj telefona");
    }

    private boolean isValidPhone(String phone) {
        if (phone.length() < 8 || phone.length() > 16) return false;
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        button.setEnabled(true);
    }
}