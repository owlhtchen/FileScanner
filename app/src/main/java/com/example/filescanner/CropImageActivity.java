package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CropImageActivity extends AppCompatActivity {
    Uri importImageUri;
    ImageView imageView;
    Button cropImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);
        initElements();
        initListeners();

        imageView = findViewById(R.id.imported_image);
        loadImage();
        imageView.setImageBitmap(ImageBitmap.getResizedImage());
    }

    void initElements() {
        cropImage = findViewById(R.id.crop_image);
    }

    void initListeners() {
        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DrawQuadrilateral drawQuadrilateral = findViewById(R.id.quadrilateral);
                List<org.opencv.core.Point> quadrilateralPoints = drawQuadrilateral.getContourOpenCVPoints();
                ImageBitmap.setQuadrilateralPoints(quadrilateralPoints);
                Intent intent = new Intent(getBaseContext(), TransformImageActivity.class);
                startActivity(intent);
            }
        });
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