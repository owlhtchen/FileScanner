package com.example.filescanner;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import java.io.IOException;

public class ImageBitmap {
    private static Bitmap original;

    public static Bitmap getOriginal() {
        return original;
    }

    public static void setOriginal(Bitmap original) {
        ImageBitmap.original = original;
    }

    public static Bitmap rotateImageUpright() throws IOException {
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
        int width = original.getWidth();
        int height = original.getHeight();
        if(width > height) {
            rotateAngle = 90;
        }
        Log.v("width", Integer.valueOf(original.getWidth()).toString());
        Log.v("height", Integer.valueOf(original.getHeight()).toString());
        Log.v("rotate angle", (Integer.valueOf(rotateAngle)).toString());
        original = rotateImage(rotateAngle);
        return original;
    }

    public static Bitmap rotateImage(float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(),
                matrix, true);
    }
}
