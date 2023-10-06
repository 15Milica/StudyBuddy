package com.example.studybuddy.ui.profile;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.adapter.ProfileStorylineAdapter;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private EditText textFirstName;
    private EditText textLastName;
    private EditText textBirthday;
    private EditText textDescription;
    private CircleImageView updatePhoto;
    private ImageView buttonSave;
    private RecyclerView recyclerView;
    private List<Post> posts;
    private Uri imageUri;
    private FirebaseUser user;
    private DatabaseReference refDatabase;
    private StorageReference refStorage;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    private ProfileStorylineAdapter profileStorylineAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        posts = new ArrayList<>();

        progressDialog = new ProgressDialog(getContext());
        progressBar = (ProgressBar) root.findViewById(R.id.progressBarProfile);

        refDatabase = FirebaseDatabase.getInstance().getReference();
        refStorage = FirebaseStorage.getInstance().getReference();

        user = FirebaseAuth.getInstance().getCurrentUser();

        updatePhoto = (CircleImageView) root.findViewById(R.id.updatePhoto);
        textFirstName = (EditText) root.findViewById(R.id.updateProfileFirstName);
        textLastName = (EditText) root.findViewById(R.id.updateProfileLastName);
        textBirthday = (EditText) root.findViewById(R.id.updateDateProfile);
        textDescription = (EditText) root.findViewById(R.id.updateMultiLineProfile);

        buttonSave = (ImageView) root.findViewById(R.id.imageViewUpdateProfileSave);

        textFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final String first_name = textFirstName.getText().toString();
                if(!Check.validFirstName(first_name)) {
                    textFirstName.setError("Unesite ime!");
                    buttonSave.setVisibility(View.GONE);
                }else
                    buttonSave.setVisibility(View.VISIBLE);
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
                if(!Check.validLastName(last_name)) {
                    textLastName.setError("Unesite prezime!");
                    buttonSave.setVisibility(View.GONE);
                }else
                    buttonSave.setVisibility(View.VISIBLE);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        textDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textDescription.getText().toString().length() <= 10) {
                    buttonSave.setVisibility(View.GONE);
                }else buttonSave.setVisibility(View.VISIBLE);
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        setUserDetails();
        textBirthday.setOnClickListener(view -> onClickBirthday());
        updatePhoto.setOnClickListener(view -> onClickUpdateImage());

        buttonSave.setOnClickListener(view -> onClickButtonSave());

        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerViewProfileStoryline);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        setStoryline();

        return root;
    }
    private void setUserDetails(){
        progressBar.setVisibility(View.VISIBLE);
        Check.enable(textFirstName, textLastName, textBirthday, textDescription, updatePhoto, false);

        refDatabase.child("users").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        User u = snapshot.getValue(User.class);

                        textFirstName.setText(u.getFirstName());
                        textLastName.setText(u.getLastName());
                        textBirthday.setText(u.getBirthday());
                        textDescription.setText(u.getDescription());

                        final String photoLink = u.getPhoto();

                        if(!photoLink.equals("default")) {
                            if(getActivity() != null) Glide.with(getActivity()).load(photoLink).into(updatePhoto);
                        }
                        else { updatePhoto.setImageResource(R.drawable.ic_create_profile_vectors_photo); }

                        Check.enable(textFirstName, textLastName, textBirthday, textDescription, updatePhoto, true);
                        buttonSave.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void onClickBirthday(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                DatePickerDialog.THEME_HOLO_LIGHT,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        final String date = (month + 1) + "/" + dayOfMonth + "/" + year;
        textBirthday.setText(date);
        buttonSave.setVisibility(View.VISIBLE);
    }

    private void onClickUpdateImage(){
        ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                updatePhoto.setImageURI(selectedImageUri);
                imageUri = selectedImageUri;
                buttonSave.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onClickButtonSave(){

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

        final String search = first_name.toLowerCase() + " " + last_name.toLowerCase();
        if(Check.networkConnect(getContext())) updateUserData(first_name, last_name, birthday, description, search);
        else
            Toast.makeText(getContext(), "Greška: nema mreže!", Toast.LENGTH_LONG).show();
    }
    private void updateUserData(String first_name, String last_name, String birthday, String description, String search){
        progressDialog.setTitle("Ažuriranje podataka!");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        HashMap<String, Object> map = new HashMap<>();

        map.put("firstName", first_name);
        map.put("lastName", last_name);
        map.put("birthday", birthday);
        map.put("description", description);
        map.put("search", search);

        refDatabase.child("users").child(user.getUid()).updateChildren(map)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if(!task.isSuccessful()) {
                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        if(imageUri != null) uploadImage();
                        buttonSave.setVisibility(View.GONE);
                    }
                });
    }
    private void uploadImage(){
        progressDialog.setTitle("Promena slike");
        progressDialog.setMessage("Sačekajte...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference userRef = refStorage.child("profile photos/"+user.getUid());
        UploadTask uploadTask = userRef.putFile(imageUri);

        uploadTask.addOnFailureListener(e ->{
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Greška: " + e, Toast.LENGTH_LONG).show();

        }).addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();

            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
            task.addOnSuccessListener(uri ->{
                String photoLink = uri.toString();
                HashMap<String, Object> map = new HashMap<>();
                map.put("photo", photoLink);
                refDatabase.child("users").child(user.getUid()).updateChildren(map);
                imageUri = null;
            });
        });
    }
    private void setStoryline(){
        Query query = FirebaseDatabase.getInstance().getReference("posts").orderByChild("user")
                .startAt(user.getUid())
                .endAt(user.getUid()+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    posts.add(post);
                }

                Collections.reverse(posts);
                profileStorylineAdapter = new ProfileStorylineAdapter(getContext(), getActivity(), posts);
                recyclerView.setAdapter(profileStorylineAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}