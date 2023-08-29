package com.example.studybuddy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.model.Comment;
import com.example.studybuddy.model.Page;
import com.example.studybuddy.model.Post;
import com.example.studybuddy.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private List<Comment> comments;
    private String postId;
    private FirebaseUser firebaseUser;
    private String pageId;

    public CommentAdapter(Context context, List<Comment> comments, String postId, String pageId) {
        this.context = context;
        this.comments = comments;
        this.postId = postId;
        this.pageId = pageId;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.user_text_comment.setText(comment.getText());

        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        refUsers.child(comment.getUser()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                holder.user_name_comment.setText(user.getName());
                if(user.getPhoto().equals("default")){
                    holder.user_photo_comment.setImageResource(R.drawable.ic_create_profile_vectors_photo);
                }else Glide.with(context).load(user.getPhoto()).into(holder.user_photo_comment);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        DatabaseReference refLikes = FirebaseDatabase.getInstance().getReference("likes");
        refLikes.child(comment.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> users = new ArrayList<>();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    users.add(dataSnapshot.getValue(String.class));
                }
                holder.text_like_comment.setText(String.valueOf(users.size()));

                if(users.contains(firebaseUser.getUid())){
                    holder.text_like_comment.setTextColor(ContextCompat.getColor(context, R.color.primary_color));
                    holder.button_like_comment.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like_primary));
                    holder.button_like_comment.setActivated(true);
                } else {
                    holder.text_like_comment.setTextColor(ContextCompat.getColor(context, R.color.text_color));
                    holder.button_like_comment.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_vector_like));
                    holder.button_like_comment.setActivated(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.button_like_comment.setOnClickListener(view -> {
            if(holder.button_like_comment.isActivated()){
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(comment.getId()).child(firebaseUser.getUid()).removeValue();
            }else {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("likes");
                ref.child(comment.getId()).child(firebaseUser.getUid()).setValue(firebaseUser.getUid());
            }
        });

        DatabaseReference refComment = FirebaseDatabase.getInstance().getReference("comments");
        refComment.child(postId).child(comment.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Comment comment = snapshot.getValue(Comment.class);
                if(comment != null){
                    if(comment.getUser().equals(firebaseUser.getUid())){
                       deleteComment(holder, comment.getId());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        if(pageId != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("pages_posts");
            ref.child(pageId).child(postId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Post post = snapshot.getValue(Post.class);
                    if (post.getUser().equals(firebaseUser.getUid())) {
                        deleteComment(holder, comment.getId());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }else {
            DatabaseReference refPosts = FirebaseDatabase.getInstance().getReference("posts");
            refPosts.child(postId).get().addOnSuccessListener(dataSnapshot -> {
                Post post = dataSnapshot.getValue(Post.class);
                final String userId = post.getUser();
                if(firebaseUser.getUid().equals(userId)){
                    deleteComment(holder, comment.getId());
                }
            });
        }
    }
    private void deleteComment(ViewHolder holder, String commentId){
        holder.cardViewComment.setOnLongClickListener(v->{
            holder.layoutDelete.setVisibility(View.VISIBLE);
            holder.layout.setVisibility(View.GONE);
            holder.button_delete.setOnClickListener(view -> {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("comments");
                ref.child(postId).child(commentId).removeValue();
                DatabaseReference refLikes = FirebaseDatabase.getInstance().getReference("likes");
                refLikes.child(commentId).removeValue();
            });
            holder.back.setOnClickListener(view -> {
                holder.layoutDelete.setVisibility(View.GONE);
                holder.layout.setVisibility(View.VISIBLE);
            });
            return true;
        });
    }
    @Override
    public int getItemCount() { return comments.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView cardViewComment;
        public CircleImageView user_photo_comment;
        public TextView user_name_comment;
        public TextView user_text_comment;
        public Button button_like_comment;
        public TextView text_like_comment;
        public Button button_delete;
        public TextView back;

        public ConstraintLayout layout;
        public ConstraintLayout layoutDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_photo_comment = itemView.findViewById(R.id.comment_user_photo);
            user_name_comment = itemView.findViewById(R.id.comment_user_name);
            user_text_comment = itemView.findViewById(R.id.comment_text);
            button_like_comment = itemView.findViewById(R.id.button_comment_like);
            text_like_comment = itemView.findViewById(R.id.comment_text_likes);
            cardViewComment = itemView.findViewById(R.id.cardViewComment);
            button_delete = itemView.findViewById(R.id.button_delete_comment);
            layout = itemView.findViewById(R.id.constraintLayoutComment);
            layoutDelete = itemView.findViewById(R.id.constraintLayoutCommentDelete);
            back = itemView.findViewById(R.id.text_back);
        }
    }
}
