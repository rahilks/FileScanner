package com.rahil.filescanner.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rahil.filescanner.R;
import com.rahil.filescanner.data.FileData;

public class LargeFileAdapter extends RecyclerView.Adapter<LargeFileAdapter.ViewHolder> {

    private FileData[] mFileData;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mFileNameTextView;
        public TextView mFileSizeTextView;

        public ViewHolder(View v) {
            super(v);
            mFileNameTextView = (TextView) v.findViewById(R.id.file_name);
            mFileSizeTextView = (TextView) v.findViewById(R.id.file_size);
        }
    }

    public LargeFileAdapter(FileData[] mFileData) {
        this.mFileData = mFileData;
    }

    @Override
    public LargeFileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mFileNameTextView.setText("File Name-" + mFileData[position].getFileName());
        holder.mFileSizeTextView.setText("File Size-" + Long.toString(mFileData[position].getSize()) + " Bytes");
    }

    @Override
    public int getItemCount() {
        return mFileData.length;
    }
}

