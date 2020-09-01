package com.example.filescanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.filescanner.constants.MyConstants;

import static com.example.filescanner.constants.MyConstants.IMPORT_IMAGE_CHOSEN;

public class MainActivity extends AppCompatActivity {

    Button importImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initElements();
        initListeners();
    }

    void initElements() {
        importImage = findViewById(R.id.import_image);
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
}