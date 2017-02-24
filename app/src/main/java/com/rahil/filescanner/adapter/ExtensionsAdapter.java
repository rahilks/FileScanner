package com.rahil.filescanner.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rahil.filescanner.R;

public class ExtensionsAdapter extends RecyclerView.Adapter<ExtensionsAdapter.ViewHolder> {

    private String[] mExtensionsData;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mExtensionDetailsTextView;

        public ViewHolder(View v) {
            super(v);
            mExtensionDetailsTextView = (TextView) v.findViewById(R.id.extension_detail_text);

        }
    }

    public ExtensionsAdapter(String[] mExtensionsData) {
        this.mExtensionsData = mExtensionsData;
    }

    @Override
    public ExtensionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.extensions_recycler_view_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mExtensionDetailsTextView.setText(mExtensionsData[position]);
    }

    @Override
    public int getItemCount() {
        return mExtensionsData.length;
    }
}

