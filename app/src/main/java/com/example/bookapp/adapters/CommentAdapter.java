package com.example.bookapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.databinding.RowCommentBinding;
import com.example.bookapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    // view binding
    @SuppressLint("StaticFieldLeak")
    private static RowCommentBinding rowCommentBinding;

    private final Context context;

    // firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private DatabaseReference databaseReferenceBooks;

    // arraylist to hold comments
    private ArrayList<ModelComment> commentArrayList;

    public CommentAdapter(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
        firebaseAuth = FirebaseAuth.getInstance ( );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the view
        rowCommentBinding = RowCommentBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( rowCommentBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        // Get data
        ModelComment modelComment = commentArrayList.get ( position );
        String timestamp = modelComment.getTimestamp ( );
        String comment = modelComment.getComment ( );
        String uid = modelComment.getUid ( );
        // format date
        String date = MyApplication.formatTimestamp ( timestamp );
        // set data
        holder.textViewCommentDate.setText ( date );
        holder.textViewComment.setText ( comment );
        loadUserDetails ( modelComment, holder );
        // handle click, show option to delete comment
        holder.itemView.setOnLongClickListener ( v -> {
            if (firebaseAuth.getCurrentUser ( ) != null && uid.equals ( firebaseAuth.getUid ( ) )) {
                deleteComment ( modelComment, holder );
            }
            return false;
        } );

        animate ( holder );
    }

    private void deleteComment(ModelComment modelComment, ViewHolder holder) {
        // show confirm dialog before deleting comment
        AlertDialog.Builder builder = new AlertDialog.Builder ( context );
        builder.setTitle ( "Delete Comment" )
                .setMessage ( "Are you sure you want to delete this comment?" )
                .setPositiveButton ( "Yes", (dialog, which) -> {
                    // delete this comment
                    databaseReferenceBooks = FirebaseDatabase.getInstance ( ).getReference ( "Books" );
                    databaseReferenceBooks.child ( modelComment.getBookId ( ) ).child ( "Comments" ).child ( modelComment.getId ( ) ).removeValue ( ).addOnSuccessListener ( new OnSuccessListener<Void> ( ) {
                        @Override
                        public void onSuccess(Void aVoid) {
                            FancyToast.makeText ( context, "Comment deleted successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( );
                        }
                    } ).addOnFailureListener ( e -> FancyToast.makeText ( context, "Failed to delete comment " + e, FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( ) );
                } )
                .setNegativeButton ( "No", (dialog, which) -> {
                    // cancel
                    dialog.dismiss ( );
                } );
        builder.show ( );
    }

    private void loadUserDetails(ModelComment modelComment, ViewHolder holder) {
        String uid = modelComment.getUid ( );
        databaseReferenceUsers = FirebaseDatabase.getInstance ( ).getReference ( "Users" );
        databaseReferenceUsers.child ( uid ).addListenerForSingleValueEvent ( new ValueEventListener ( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = "" + snapshot.child ( "name" ).getValue ( );
                String profileImage = "" + snapshot.child ( "profileImage" ).getValue ( );
                holder.textViewCommentUserName.setText ( name );
                try {
                    Picasso.get ( ).load ( profileImage ).placeholder ( R.drawable.profile_image ).into ( holder.circleImageViewCommentUser );
                } catch (Exception e) {
                    holder.circleImageViewCommentUser.setImageResource ( R.drawable.profile_image );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size ( );
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation ( context, R.anim.bounce_interpolator );
        viewHolder.itemView.setAnimation ( animAnticipateOvershoot );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views for row_comment.xml
        private final CircleImageView circleImageViewCommentUser;
        private final TextView textViewCommentUserName;
        private final TextView textViewCommentDate;
        private final TextView textViewComment;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            circleImageViewCommentUser = rowCommentBinding.circleImageViewCommentUser;
            textViewCommentUserName = rowCommentBinding.textViewCommentUserName;
            textViewCommentDate = rowCommentBinding.textViewCommentDate;
            textViewComment = rowCommentBinding.textViewComment;
        }
    }
}