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

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.example.filescanner.constants.MyConstants.IMPORT_IMAGE_CHOSEN;
import static org.opencv.core.Core.addWeighted;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.threshold;

public class TransformImageActivity extends AppCompatActivity {
    Bitmap rectImage;
    Bitmap displayImage;
    ImageView transformedImage;
    Button save;

    Button originalBtn;
    Button sharpenBtn;
    Button blackWhiteAdaptiveBtn;
    Button blackWhiteHardBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transform_image);
        rectImage = ImageBitmap.transformToRectImage();
        displayImage = Bitmap.createBitmap(rectImage);
        initElements();
        initListeners();
//        Log.v("absolute path storage", String.valueOf(getExternalFilesDir(null).getAbsolutePath()));
//        Log.v("path storage", String.valueOf(getExternalFilesDir(null).getPath()));
        Toast.makeText(this, "image cropped", Toast.LENGTH_SHORT).show();
    }

    void initElements() {
        transformedImage = findViewById(R.id.transformed_image);
        transformedImage.setImageBitmap(displayImage);
        save = findViewById(R.id.save);
        originalBtn = findViewById(R.id.original);
        sharpenBtn = findViewById(R.id.sharpen);
        blackWhiteAdaptiveBtn = findViewById(R.id.b_w_adaptive);
        blackWhiteHardBtn = findViewById(R.id.b_w_hard);
    }

    void initListeners() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImage();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        originalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayImage = Bitmap.createBitmap(rectImage);
                transformedImage.setImageBitmap(displayImage);
            }
        });
        sharpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                displayImage = getSharpenedBitmap(rectImage);
                transformedImage.setImageBitmap(displayImage);
            }
        });
        blackWhiteAdaptiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                displayImage = getBlackWhiteBitmapAdaptive(getSharpenedBitmap(rectImage));
                transformedImage.setImageBitmap(displayImage);
            }
        });
        blackWhiteHardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View view) {
                displayImage = getBlackWhiteBitmapHard(getSharpenedBitmap(rectImage));
                transformedImage.setImageBitmap(displayImage);
            }
        });
    }

    Bitmap getSharpenedBitmap(Bitmap srcBitmap) {
        Mat src = ImageBitmap.bitmapToMat(srcBitmap);
        Mat dest = new Mat();
        // https://en.wikipedia.org/wiki/Unsharp_masking#Digital_unsharp_masking
        // sharpened = original + (original − blurred) × amount.
        GaussianBlur(src, dest, new Size(0.0, 0.0), 3.0);
//        addWeighted(src, 2.0, dest, - 1.0, 0, dest);
        addWeighted(src, 5.0, dest, - 4.0, 0, dest);
        return ImageBitmap.matToBitmap(dest);
    }

    Bitmap getBlackWhiteBitmapAdaptive(Bitmap srcBitmap) {
        Mat src = ImageBitmap.bitmapToMat(srcBitmap);
        Mat dest = new Mat();
        cvtColor(src, dest, COLOR_BGR2GRAY);
        dest.assignTo(src);
        // adaptiveThreshold​(Mat src, Mat dst, double maxValue, int adaptiveMethod, int thresholdType, int blockSize, double C)
        adaptiveThreshold(src, dest, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 15, 40);

        return ImageBitmap.matToBitmap(dest);
    }

    Bitmap getBlackWhiteBitmapHard(Bitmap srcBitmap) {
        Mat src = ImageBitmap.bitmapToMat(srcBitmap);
        Mat dest = new Mat();
        cvtColor(src, dest, COLOR_BGR2GRAY);
        dest.assignTo(src);

        // threshold​(Mat src, Mat dst, double thresh, double maxval, int type)
        threshold(src, dest, 160.0, 255.0, THRESH_BINARY);
        return ImageBitmap.matToBitmap(dest);
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
            displayImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
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