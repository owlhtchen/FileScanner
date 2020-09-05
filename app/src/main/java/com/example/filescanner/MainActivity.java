package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.filescanner.constants.MyConstants;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.filescanner.constants.MyConstants.IMPORT_IMAGE_CHOSEN;

public class MainActivity extends AppCompatActivity {

    Button importImage;
    RecyclerView imagesPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initElements();
        initListeners();
        testImportOpenCV();

    }

    void initElements() {
        importImage = findViewById(R.id.import_image);
        imagesPreview = findViewById(R.id.images_preview);
        File directory = new File(getExternalFilesDir(null), MyConstants.FOLDER_NAME);
        List<File> files = new ArrayList<>(Arrays.asList(directory.listFiles())); // pass this to adapter

        ImagesPreviewAdapter imagesPreviewAdapter = new ImagesPreviewAdapter(this, files);
        imagesPreview.setAdapter(imagesPreviewAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        imagesPreview.setLayoutManager(gridLayoutManager);
    }

    void initListeners() {
        importImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, IMPORT_IMAGE_CHOSEN);
            }
        });
    }

    void testImportOpenCV() {
        if(OpenCVLoader.initDebug()){
            Toast.makeText(this, "openCv successfully loaded", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "openCv cannot be loaded", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_IMAGE_CHOSEN && resultCode == RESULT_OK && data != null) {
            Uri importImageUri = data.getData();
            Intent intent = new Intent(getBaseContext(), CropImageActivity.class);
            intent.putExtra("IMPORT_IMAGE_URL", importImageUri);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(startMain);
        finishAffinity();
        System.exit(0);
    }
}