package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.CategoryUserAdapter;
import com.example.bookapp.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategoryUser extends Filter {

    // arraylist in which we want to search
    ArrayList<ModelCategory> filterList;
    CategoryUserAdapter categoryUserAdapter;
    // adapter in which filter need to be implemented

    // constructor
    public FilterCategoryUser(ArrayList<ModelCategory> filterList, CategoryUserAdapter categoryUserAdapter) {
        this.filterList = filterList;
        this.categoryUserAdapter = categoryUserAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults ( );
        // value should not be null and empty
        if (constraint != null && constraint.length ( ) > 0) {
            // change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString ( ).toLowerCase ( );
            ArrayList<ModelCategory> filterModels = new ArrayList<> ( );
            for (int i = 0; i < filterList.size ( ); i++) {
                // validate
                if (filterList.get ( i ).getCategory ( ).toLowerCase ( ).contains ( constraint )) {
                    // add to filtered list
                    filterModels.add ( filterList.get ( i ) );
                }
            }
            filterResults.count = filterModels.size ( );
            filterResults.values = filterModels;
        } else {
            filterResults.count = filterList.size ( );
            filterResults.values = filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        // apply filter changes
        CategoryUserAdapter.modelCategoryArrayList = (ArrayList<ModelCategory>) filterResults.values;
        categoryUserAdapter.notifyDataSetChanged ( );
    }
}
