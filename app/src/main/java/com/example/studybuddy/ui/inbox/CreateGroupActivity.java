package com.example.studybuddy.ui.inbox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.GroupAdapter;
import com.example.studybuddy.model.Group;
import com.example.studybuddy.model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {

    private ImageButton buttonBack;
    private Button buttonCreate;
    private EditText textNameGroup;
    private CircleImageView imagePhotoGroup;
    private Uri imageUri = null;
    private RecyclerView recyclerView;
    private List<User> users;
    private FirebaseUser userCreator;
    private DatabaseReference refDatabase;
    private DatabaseReference refStatus;
    private StorageReference refStorage;
    private ProgressDialog progressDialog;
    private List<String> mFollowers;
    private GroupAdapter groupAdapter;
    private ProgressBar progressBar;

    private List<String> members;
    private List<String> tokens;
    private String userToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        progressDialog = new ProgressDialog(this);

        users = new ArrayList<>();
        mFollowers = new ArrayList<>();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) { userToken = task.getResult(); }
                });

        userCreator = FirebaseAuth.getInstance().getCurrentUser();
        refDatabase = FirebaseDatabase.getInstance().getReference("groups");
        refStatus = FirebaseDatabase.getInstance().getReference("user_status").child(userCreator.getUid());
        refStorage = FirebaseStorage.getInstance().getReference();

        progressBar = (ProgressBar) findViewById(R.id.progressBarCreateGroup);

        buttonBack = (ImageButton) findViewById(R.id.imageButtonBackCreateGroup);
        buttonBack.setOnClickListener(view -> onBackPressed());

        buttonCreate = (Button) findViewById(R.id.buttonCreateGroup);
        buttonCreate.setOnClickListener(view -> onClickCreate());

        textNameGroup = (EditText) findViewById(R.id.editTextCreateGroupName);
        textNameGroup.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final String name = textNameGroup.getText().toString();
                if(!Check.validFirstName(name)) textNameGroup.setError("Unesite ime grupe");
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        imagePhotoGroup = (CircleImageView) findViewById(R.id.pagePhotoCreateGroup);
        imagePhotoGroup.setOnClickListener(view -> onClickImagePhoto());

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewCreateGroup);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showFollowers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refStatus.setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        refStatus.setValue("Offline");
    }
    private void onClickImagePhoto(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080,1080)
                .start();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                imagePhotoGroup.setImageURI(selectedImageUri);
                imageUri = selectedImageUri;
            }
        }
    }
    private void uploadImageGroup(String groupId){
        progressDialog.setTitle("Otpremanje grupne slike");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference ref = refStorage.child("group photos/"+groupId);
        UploadTask uploadTask = ref.putFile(imageUri);

        uploadTask.addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(this, "Greška: " + e, Toast.LENGTH_LONG).show();
            finish();
        }).addOnSuccessListener(taskSnapshot ->{
            progressDialog.dismiss();
            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnSuccessListener(t ->{
                String photoLink = t.toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("groupPhoto", photoLink);
                refDatabase.child(groupId).updateChildren(map);
            });
            finish();
        });
    }
    private void showFollowers(){
        DatabaseReference refFollowers = FirebaseDatabase.getInstance().getReference("followers");
        refFollowers.child(userCreator.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mFollowers.clear();
                        for(DataSnapshot dataSnapshot:snapshot.getChildren())
                            mFollowers.add(dataSnapshot.getKey());
                        readUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void readUsers() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User u = dataSnapshot.getValue(User.class);
                    assert u != null;
                    assert userCreator != null;
                    if (!userCreator.getUid().equals(u.getUserId()) && Check.isFollower(u.getUserId(), mFollowers)) { users.add(u); }
                }
                groupAdapter = new GroupAdapter(CreateGroupActivity.this, users);
                recyclerView.setAdapter(groupAdapter);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {progressBar.setVisibility(View.GONE);}
        });
    }
    private void onClickCreate(){
        if(userToken == null) return;

        List<User> checkedUsers = groupAdapter.getCheckedUsers();

        members = new ArrayList<>();
        tokens = new ArrayList<>();

        members.add(userCreator.getUid());
        tokens.add(userToken);

        for(User u : checkedUsers){
            members.add(u.getUserId());
            tokens.add(u.getToken());
        }

        final String name = textNameGroup.getText().toString();
        if(!Check.validFirstName(name)){
            Toast.makeText(this, "Unesite ime!", Toast.LENGTH_LONG).show();
            return;
        }
        if(!Check.networkConnect(this)){
            Toast.makeText(this, "Greška: nema mreže!", Toast.LENGTH_LONG).show();
            return;
        }
        final String search = name.toLowerCase();

        String groupId = refDatabase.push().getKey();
        Group group = new Group(groupId, name, userCreator.getUid(), "default", members, tokens, search);

        progressDialog.setTitle("Kreiranje grupe");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        refDatabase.child(groupId).setValue(group)
                .addOnFailureListener(e ->{
                    progressDialog.dismiss();
                    Toast.makeText(this, "Greška:" + e, Toast.LENGTH_LONG).show();

                }).addOnSuccessListener(aVoid->{
                    progressDialog.dismiss();

                    List<String> userId = group.getMembers();

                    for(String id : userId) {
                        DatabaseReference groupRef = FirebaseDatabase.getInstance().getReference("chats_list")
                                .child(id)
                                .child(groupId);

                        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) { if(!snapshot.exists()) { groupRef.child("id").setValue(groupId); } }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }


                    if(imageUri != null) { uploadImageGroup(groupId); }
                    else {
                        Toast.makeText(CreateGroupActivity.this, "Uspešno kreiranje grupe", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}