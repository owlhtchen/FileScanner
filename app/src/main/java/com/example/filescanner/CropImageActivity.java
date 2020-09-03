package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CropImageActivity extends AppCompatActivity {
    Uri importImageUri;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        imageView = findViewById(R.id.imported_image);
        loadImage();
        imageView.setImageBitmap(ImageBitmap.getOriginal());
    }

    void loadImage() {
        importImageUri = (Uri) getIntent().getExtras().get("IMPORT_IMAGE_URL");
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(importImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap original = BitmapFactory.decodeStream(inputStream);
        ImageBitmap.setOriginal(original);
        try {
            ImageBitmap.rotateImageUpright();
            ImageBitmap.resizeImage(800);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}