package com.example.studybuddy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.model.InviteItem;

import java.util.List;

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.ViewHolder> {
    private Context context;
    private List<InviteItem> items;

    public InviteAdapter(Context context, List<InviteItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public InviteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.invite_item, parent, false);
        return new InviteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteAdapter.ViewHolder holder, int position) {
        InviteItem item = items.get(position);

        holder.invite_name.setText(item.getInviteName());
        holder.invite_phone.setText(item.getInvitePhone());
        holder.invite_image.setText(Character.toString(item.getInviteName().charAt(0)));
        holder.invite_button.setOnClickListener(view -> onButtonInvite());
    }

    private void onButtonInvite() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "Stefan_FOO" + "App link");
        context.startActivity(Intent.createChooser(i, "Posalji link"));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView invite_name;
        public TextView invite_phone;
        public Button invite_button;

        public TextView invite_image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            invite_name = itemView.findViewById(R.id.invite_name);
            invite_phone = itemView.findViewById(R.id.invite_phone);
            invite_button = itemView.findViewById(R.id.invite_button);
            invite_image = itemView.findViewById(R.id.invite_image);
        }
    }
}
