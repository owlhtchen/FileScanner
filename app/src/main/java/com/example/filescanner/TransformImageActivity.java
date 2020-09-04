package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import static com.example.filescanner.constants.MyConstants.IMPORT_IMAGE_CHOSEN;

public class TransformImageActivity extends AppCompatActivity {
    Bitmap rectImage;
    ImageView transformedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform_image);
        rectImage = ImageBitmap.transformToRectImage();
        initElements();
        initListeners();
        Toast.makeText(this, "image cropped", Toast.LENGTH_SHORT).show();
    }

    void initElements() {
        transformedImage = findViewById(R.id.transformed_image);
        transformedImage.setImageBitmap(rectImage);
    }

    void initListeners() {
    }
}