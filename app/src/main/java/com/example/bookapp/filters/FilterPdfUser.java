package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.PdfUserAdapter;
import com.example.bookapp.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfUser extends Filter {

    // arraylist in which we want to search
    ArrayList<ModelPdf> filterList;
    PdfUserAdapter pdfUserAdapter;
    // adapter in which filter need to be implemented

    // constructor
    public FilterPdfUser(ArrayList<ModelPdf> filterList, PdfUserAdapter pdfUserAdapter) {
        this.filterList = filterList;
        this.pdfUserAdapter = pdfUserAdapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults ( );
        // value should not be null and empty
        if (constraint != null && constraint.length ( ) > 0) {
            // change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString ( ).toLowerCase ( );
            ArrayList<ModelPdf> filterModels = new ArrayList<> ( );
            for (int i = 0; i < filterList.size ( ); i++) {
                // validate
                if (filterList.get ( i ).getTitle ( ).toLowerCase ( ).contains ( constraint )) {
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
        PdfUserAdapter.pdfArrayList = (ArrayList<ModelPdf>) filterResults.values;
        pdfUserAdapter.notifyDataSetChanged ( );
    }
}
