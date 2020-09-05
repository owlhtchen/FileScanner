package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.filescanner.constants.MyConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.example.filescanner.constants.MyConstants.IMPORT_IMAGE_CHOSEN;

public class TransformImageActivity extends AppCompatActivity {
    Bitmap rectImage;
    ImageView transformedImage;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform_image);
        rectImage = ImageBitmap.transformToRectImage();
        initElements();
        initListeners();
//        Log.v("absolute path storage", String.valueOf(getExternalFilesDir(null).getAbsolutePath()));
//        Log.v("path storage", String.valueOf(getExternalFilesDir(null).getPath()));
        Toast.makeText(this, "image cropped", Toast.LENGTH_SHORT).show();
    }

    void initElements() {
        transformedImage = findViewById(R.id.transformed_image);
        transformedImage.setImageBitmap(rectImage);
        save = findViewById(R.id.save);
    }

    void initListeners() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
//                finish();
            }
        });
    }

    void saveImage() {
        // save file in .../ImageDir/filename.jpeg
        File imageStorageDir = new File(getExternalFilesDir(null), MyConstants.FOLDER_NAME);
        if(!imageStorageDir.exists()) {
            if(!imageStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
        }
        String fileName = getCurrentTimeString() + ".jpeg";
        File file = new File(imageStorageDir.getPath(), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            rectImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(TransformImageActivity.this,
                    "Saved as " + fileName + " in folder " + imageStorageDir.getPath(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getCurrentTimeString() {
        String pattern = "yyyy_MM_dd_HH_mm_ss_SSSS";
        DateFormat df = new SimpleDateFormat(pattern);

        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        TimeZone tz = c.getTimeZone();
        df.setTimeZone(tz);

        return df.format(today);
    }
}