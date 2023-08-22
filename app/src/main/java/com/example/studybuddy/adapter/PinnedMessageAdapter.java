package com.example.studybuddy.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.Check;
import com.example.studybuddy.R;
import com.example.studybuddy.model.PinnedMessage;
import com.example.studybuddy.model.User;
import com.example.studybuddy.ui.profile.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PinnedMessageAdapter extends RecyclerView.Adapter<PinnedMessageAdapter.ViewHolder> {

    private Context context;
    private List<PinnedMessage> pinnedMessageList;
    private FirebaseUser firebaseUser;
    private Activity activity;

    public PinnedMessageAdapter(Context context, List<PinnedMessage> pinnedMessageList, Activity activity) {
        this.context = context;
        this.pinnedMessageList = pinnedMessageList;
        this.activity = activity;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public PinnedMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pinned_message_item, parent, false);
        return new PinnedMessageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PinnedMessageAdapter.ViewHolder holder, int position) {
        PinnedMessage pinnedMessage = pinnedMessageList.get(position);
        holder.pinned_msg_date.setText(pinnedMessage.getDate());
        holder.pinned_msg_time.setText(pinnedMessage.getTime());
        holder.pinned_msg_text.setText(pinnedMessage.getMessage());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(pinnedMessage.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getPhoto().equals("default"))
                    holder.pinned_msg_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                else Glide.with(context).load(user.getPhoto()).into(holder.pinned_msg_photo);
                if(firebaseUser.getUid().equals(user.getUserId())){
                    holder.pinned_msg_name.setText("Moja poruka");
                }else holder.pinned_msg_name.setText(user.getName());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        holder.pinned_msg_text.setOnLongClickListener(view -> {
            delete(pinnedMessage);
            return true;
        });
        holder.pinned_msg_photo.setOnClickListener(view -> onClickPhoto(pinnedMessage.getUserId()));
    }
    private void delete(PinnedMessage message){
        if(!Check.networkConnect(context)){
            Toast.makeText(context, "Greška: nema mreže!", Toast.LENGTH_SHORT).show();
        }else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pinned_messages");
            ref.child(firebaseUser.getUid()).child(message.getId()).removeValue();
        }
    }
    private void onClickPhoto(String userId){
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("userId", userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }
    @Override
    public int getItemCount() { return pinnedMessageList.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView pinned_msg_photo;
        public TextView pinned_msg_name;
        public TextView pinned_msg_date;
        public TextView pinned_msg_text;
        public TextView pinned_msg_time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pinned_msg_photo = itemView.findViewById(R.id.pinned_msg_photo);
            pinned_msg_name = itemView.findViewById(R.id.pinned_msg_name);
            pinned_msg_date = itemView.findViewById(R.id.pinned_msg_date);
            pinned_msg_text = itemView.findViewById(R.id.pinned_message);
            pinned_msg_time = itemView.findViewById(R.id.pinned_msg_time);
        }
    }
}
