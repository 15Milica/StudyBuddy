package com.example.studybuddy.ui.inbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.studybuddy.R;
import com.example.studybuddy.adapter.InviteAdapter;
import com.example.studybuddy.model.InviteItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InviteActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private ImageButton buttonBack;
    private List<InviteItem> invites;
    private EditText search;
    private RecyclerView recyclerView;

    private InviteAdapter inviteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        invites = new ArrayList<>();

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackInvite);
        buttonBack.setOnClickListener(view -> onBackPressed());

        search = (EditText) findViewById(R.id.editTextSearchInviteFriends);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchContacts(search.getText().toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewInvite);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        showContacts();
    }
    private void searchContacts(String contact){
        List<InviteItem> items = new ArrayList<>();

        for(InviteItem i : invites){
            if(i.getInviteName().toLowerCase().contains(contact))
                items.add(i);
        }

        inviteAdapter = new InviteAdapter(this, items);
        recyclerView.setAdapter(inviteAdapter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                showContacts();
        }
    }
    private void showContacts() {
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getContacts();
        }
    }
    @SuppressLint("Range")
    private void getContacts(){
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                    while (phones.moveToNext()) {
                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        InviteItem inviteItem = new InviteItem(name, phoneNumber);
                        invites.add(inviteItem);
                        break;
                    }
                    phones.close();
                }

            }
        }
        inviteAdapter = new InviteAdapter(this, invites);
        recyclerView.setAdapter(inviteAdapter);
    }
}