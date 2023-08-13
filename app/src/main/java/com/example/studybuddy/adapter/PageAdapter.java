package com.example.studybuddy.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Page;
import com.example.studybuddy.ui.pages.PageActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PageAdapter extends RecyclerView.Adapter<PageAdapter.ViewHolder> {

    private Context context;
    private List<Page> pages;
    private FirebaseUser user;
    public PageAdapter(Context context, List<Page> pages) {
        this.context = context;
        this.pages = pages;
    }

    @NonNull
    @Override
    public PageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.page_item, parent, false);
        return new PageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageAdapter.ViewHolder holder, int position) {
        Page p = pages.get(position);

        holder.page_name.setText(p.getPageName());
        holder.page_description.setText(p.getPageDescription());

        holder.page_members.setText(p.getMembers().size() + " članova");

        if(p.getPagePhoto().equals("default")) holder.page_photo.setImageResource(R.drawable.ic_create_profile_vectors_photo);
        else Glide.with(context).load(p.getPagePhoto()).into(holder.page_photo);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(p.getMembers().containsKey(user.getUid())){
            holder.member.setVisibility(View.VISIBLE);
            holder.join.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(view -> onClickGroup(p));
        } else {
            holder.member.setVisibility(View.GONE);
            holder.join.setVisibility(View.VISIBLE);
            holder.join.setOnClickListener(view -> onClickJoin(p));
            holder.itemView.setOnClickListener(view -> onClickGroup(p));
        }
    }
    private void onClickGroup(Page page){
        Intent intent = new Intent(context, PageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("page", page);
        context.startActivity(intent);
    }
    private void onClickJoin(Page page){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        Button buttonJoin = dialogView.findViewById(R.id.confirm_button);
        Button buttonCancel = dialogView.findViewById(R.id.cancel_button);
        TextView text = dialogView.findViewById(R.id.textViewAlertDialog);
        text.setText("Želite li da se pridružite grupi?");
        buttonJoin.setText("Pridruži se");

        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pages").child(page.getPageId());
                ref.child("members").child(user.getUid()).setValue("member")
                        .addOnCompleteListener(task ->{
                            if(task.isSuccessful()){

                                Toast.makeText(context, "Uspešno ste priključeni grupi!",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            dialog.dismiss();
                        });
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
    @Override
    public int getItemCount() { return pages.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView page_name;
        public TextView page_description;
        public TextView page_members;
        public CircleImageView page_photo;
        public Button join;
        public TextView member;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            page_members = itemView.findViewById(R.id.page_members);
            page_name = itemView.findViewById(R.id.page_name);
            page_description = itemView.findViewById(R.id.page_description);
            page_photo = itemView.findViewById(R.id.page_photo);
            join = itemView.findViewById(R.id.buttonJoin);
            member = itemView.findViewById(R.id.textMember);
        }
    }

}
