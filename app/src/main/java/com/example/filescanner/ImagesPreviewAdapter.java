package com.example.filescanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagesPreviewAdapter extends RecyclerView.Adapter<ImagesPreviewAdapter.ViewHolder> {

    List<File> mValues;
    Context mContext;

    public ImagesPreviewAdapter(Context context, List<File> values) {
        mValues = values;
        mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // This is your ViewHolder class that helps to populate data to the view
        public TextView textView;
        public ImageView imageView;
        public RelativeLayout relativeLayout;
        File file;

        public ViewHolder(View v) {

            super(v);

            v.setOnClickListener(this);
            imageView = (ImageView) v.findViewById(R.id.preview_thumbnail);
            textView = (TextView) v.findViewById(R.id.preview_name);
        }

        public void setData(File file) {
            this.file = file;

            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            imageView.setImageBitmap(bitmap);
//            imageView.setImageResource(R.drawable.gray_circle);
            textView.setText(file.getName());
        }


        @Override
        public void onClick(View view) {
            Toast.makeText(mContext.getApplicationContext(), file.getName() + " is clicked", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public ImagesPreviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // This method creates views for the RecyclerView by inflating the layout
        // Into the viewHolders which helps to display the items in the RecyclerView
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_preview, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // This method is called when binding the data to the views being created in RecyclerView
        viewHolder.setData(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }
}