package com.example.bookapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.activities.PdfListAdminActivity;
import com.example.bookapp.databinding.RowCategoryAdminBinding;
import com.example.bookapp.filters.FilterCategoryAdmin;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

public class CategoryAdminAdapter extends RecyclerView.Adapter<CategoryAdminAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "CategoryAdminAdapter";

    // view binding
    @SuppressLint("StaticFieldLeak")
    static RowCategoryAdminBinding rowCategoryAdminBinding;

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static ArrayList<ModelCategory> modelCategoryArrayList = null;
    public static ArrayList<ModelCategory> filterList = null;

    // instance of filter class
    private FilterCategoryAdmin filterCategoryAdmin;

    // firebase
    private DatabaseReference databaseReferenceCategories;

    public CategoryAdminAdapter(Context context, ArrayList<ModelCategory> modelCategoryArrayList) {
        CategoryAdminAdapter.context = context;
        CategoryAdminAdapter.modelCategoryArrayList = modelCategoryArrayList;
        CategoryAdminAdapter.filterList = modelCategoryArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_category.xml
        rowCategoryAdminBinding = RowCategoryAdminBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( rowCategoryAdminBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data
        ModelCategory modelCategory = modelCategoryArrayList.get ( position );
        String id = modelCategory.getId ( );
        String category = modelCategory.getCategory ( );
        // set data
        holder.textViewCategoryTitle.setText ( category );
        // handle click, delete category
        holder.imageButtonDelete.setOnClickListener ( v -> {
            // confirm delete dialog
            String categoryToBeDeleted = holder.textViewCategoryTitle.getText ( ).toString ( );
            AlertDialog.Builder builder = new AlertDialog.Builder ( context );
            builder.setTitle ( "Delete " + categoryToBeDeleted );
            builder.setMessage ( "Do you want to delete " + categoryToBeDeleted + " category?" );
            builder.setPositiveButton ( "Delete", (dialog, which) -> {
                // begin delete
                deleteCategory ( modelCategory, holder );
            } );
            builder.setNeutralButton ( "No", (dialog, which) -> dialog.dismiss ( ) );
            builder.show ( );
        } );

        // handle item click, intent to PdfListAdminActivity.class
        holder.itemView.setOnClickListener ( v -> {
            Intent intentPdfListActivity = new Intent ( context, PdfListAdminActivity.class );
            intentPdfListActivity.putExtra ( "categoryId", id );
            intentPdfListActivity.putExtra ( "categoryTitle", category );
            context.startActivity ( intentPdfListActivity );
        } );

        animate ( holder );
    }

    private void deleteCategory(ModelCategory modelCategory, ViewHolder holder) {
        // get id of category to be deleted
        String id = modelCategory.getId ( );
        // Database Root -> Categories -> categoryId
        databaseReferenceCategories = FirebaseDatabase.getInstance ( ).getReference ( "Categories" );
        databaseReferenceCategories.child ( id ).removeValue ( ).addOnSuccessListener ( unused -> FancyToast.makeText ( context, modelCategory.getCategory ( ) + " deleted successfully", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false ).show ( ) ).addOnFailureListener ( e -> FancyToast.makeText ( context, e.getMessage ( ), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false ).show ( ) );
    }

    @Override
    public int getItemCount() {
        return modelCategoryArrayList.size ( );
    }

    public void animate(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation ( context, R.anim.bounce_interpolator );
        viewHolder.itemView.setAnimation ( animAnticipateOvershoot );
    }

    @Override
    public Filter getFilter() {
        if (filterCategoryAdmin == null) {
            filterCategoryAdmin = new FilterCategoryAdmin ( filterList, this );
        }
        return filterCategoryAdmin;
    }

    /* View holder class to hold UI for row_category.xml */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // ui views of row_category
        private final TextView textViewCategoryTitle;
        private final ImageButton imageButtonDelete;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            textViewCategoryTitle = rowCategoryAdminBinding.textViewCategoryTitle;
            imageButtonDelete = rowCategoryAdminBinding.imageButtonDelete;
        }
    }
}