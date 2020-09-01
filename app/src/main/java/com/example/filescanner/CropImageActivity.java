package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class CropImageActivity extends AppCompatActivity {
    Uri importImageUri;
    Bitmap imageBitmap;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        imageView = findViewById(R.id.imported_image);

        importImageUri = (Uri) getIntent().getExtras().get("IMPORT_IMAGE_URL");
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(importImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageBitmap = BitmapFactory.decodeStream(inputStream);
        imageView.setImageBitmap(imageBitmap);
    }


}