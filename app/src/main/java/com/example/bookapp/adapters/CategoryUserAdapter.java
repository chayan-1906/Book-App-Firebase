package com.example.bookapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.R;
import com.example.bookapp.activities.PdfListUserActivity;
import com.example.bookapp.databinding.RowCategoryUserBinding;
import com.example.bookapp.filters.FilterCategoryUser;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class CategoryUserAdapter extends RecyclerView.Adapter<CategoryUserAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "CategoryUserAdapter";

    // view binding
    @SuppressLint("StaticFieldLeak")
    static RowCategoryUserBinding rowCategoryUserBinding;

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    public static ArrayList<ModelCategory> modelCategoryArrayList = null;
    public static ArrayList<ModelCategory> filterList = null;

    // instance of filter class
    private FilterCategoryUser filterCategoryUser;

    public CategoryUserAdapter(Context context, ArrayList<ModelCategory> modelCategoryArrayList) {
        CategoryUserAdapter.context = context;
        CategoryUserAdapter.modelCategoryArrayList = modelCategoryArrayList;
        CategoryUserAdapter.filterList = modelCategoryArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind row_category.xml
        rowCategoryUserBinding = RowCategoryUserBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( rowCategoryUserBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data
        ModelCategory modelCategory = modelCategoryArrayList.get ( position );
        String id = modelCategory.getId ( );
        String category = modelCategory.getCategory ( );
        // set data
        holder.textViewCategoryTitle.setText ( category );
        // handle item click, intent to PdfListUserActivity.class
        holder.itemView.setOnClickListener ( v -> {
            Intent intentPdfListUserActivity = new Intent ( context, PdfListUserActivity.class );
            intentPdfListUserActivity.putExtra ( "categoryId", id );
            intentPdfListUserActivity.putExtra ( "categoryTitle", category );
            context.startActivity ( intentPdfListUserActivity );
        } );

        animate ( holder );
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
        if (filterCategoryUser == null) {
            filterCategoryUser = new FilterCategoryUser ( filterList, this );
        }
        return filterCategoryUser;
    }

    /* View holder class to hold UI for row_category.xml */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        // ui views of row_category
        private final TextView textViewCategoryTitle;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            textViewCategoryTitle = rowCategoryUserBinding.textViewCategoryTitle;
        }
    }
}