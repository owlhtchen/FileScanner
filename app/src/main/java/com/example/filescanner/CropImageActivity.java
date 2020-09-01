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
        try {
            imageBitmap = rotateImageUpright(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(imageBitmap);
    }

    public static Bitmap rotateImageUpright(Bitmap imageBitmap) throws IOException {
        // https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html
//        ExifInterface ei = new ExifInterface(inputStream);
//        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        int rotateAngle = 0;
//        switch(orientation) {
//            case ExifInterface.ORIENTATION_ROTATE_180:
//                rotateAngle = 180;
//                break;
//
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                rotateAngle = 270;
//                break;
//            case ExifInterface.ORIENTATION_ROTATE_90:
//            case ExifInterface.ORIENTATION_NORMAL:
//            default:
//                rotateAngle = 90;
//        }
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        if(width > height) {
            rotateAngle = 90;
        }
        Log.v("width", Integer.valueOf(imageBitmap.getWidth()).toString());
        Log.v("height", Integer.valueOf(imageBitmap.getHeight()).toString());
        Log.v("rotate angle", (Integer.valueOf(rotateAngle)).toString());
        rotatedBitmap = rotateImage(imageBitmap, rotateAngle);
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
}