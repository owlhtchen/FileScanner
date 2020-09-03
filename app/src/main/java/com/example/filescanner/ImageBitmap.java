package com.example.filescanner;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_LIST;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.findContours;

public class ImageBitmap {
    public static Bitmap original;

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
        original = rotateImage(rotateAngle);
        return original;
    }

    public static Bitmap rotateImage(float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(),
                matrix, true);
    }

    public static List<org.opencv.core.Point> getContourPoints() {
        return getContourPoints(original);
    }

    public static List<org.opencv.core.Point> getContourPoints(Bitmap bitmap) {
        Mat grey = new Mat();
        Mat blurredGrey = new Mat();
        Mat edged = new Mat();
        cvtColor(bitmapToMat(bitmap), grey, COLOR_BGR2GRAY);
        GaussianBlur(grey, blurredGrey, new Size(5.0, 5.0), 0);
        Canny(blurredGrey, edged, 75.0, 200.0);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        findContours(edged, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        contours.sort(new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint mop1, MatOfPoint mop2) {
                return (int)  -(contourArea(mop1) - contourArea(mop2));
            }
        });
//        Log.v("length of contours lists", contours.size() + "");
//        logContourAreaList(contours);

        Point topLeft = new Point(70.0, 70.0);
        Point topRight = new Point(70.0 + 800, 70.0);
        Point bottomRight = new Point(70.0 + 800, 70.0 + 800);
        Point bottomLeft = new Point(70.0, 70.0 + 800);

        List<Point> contourPoints = new ArrayList<Point>(Arrays.asList(topLeft, topRight, bottomRight, bottomLeft));
        for( MatOfPoint mop: contours ){

            MatOfPoint2f mop2f = new MatOfPoint2f(mop.toArray());
            double peri = arcLength(mop2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            approxPolyDP(mop2f, approx, 0.05 * peri, true);

            if(approx.toList().size() == 4) {
                contourPoints = approx.toList();
                break;
            }
        }
        contourPoints = orderPoints(contourPoints);
//        logCVPoints(contourPoints);
//        Log.v("image width", bitmap.getWidth()+ "");
//        Log.v("image height", bitmap.getHeight() + "");
        return contourPoints;
    }

    public static void getContourPoints(Bitmap bitmap, MatOfPoint2f contourPoints) {
        Mat grey = new Mat();
        Mat blurredGrey = new Mat();
        Mat edged = new Mat();
        cvtColor(bitmapToMat(bitmap), grey, COLOR_BGR2GRAY);
        GaussianBlur(grey, blurredGrey, new Size(5.0, 5.0), 0);
        Canny(blurredGrey, edged, 75.0, 200.0);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        findContours(edged, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        contours.sort(new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint mop1, MatOfPoint mop2) {
                return (int)  -(contourArea(mop1) - contourArea(mop2));
            }
        });
//        Log.v("length of contours lists", contours.size() + "");
//        logContourAreaList(contours);

        Point topLeft = new Point(70.0, 70.0);
        Point topRight = new Point(70.0 + 800, 70.0);
        Point bottomRight = new Point(70.0 + 800, 70.0 + 800);
        Point bottomLeft = new Point(70.0, 70.0 + 800);

        for( MatOfPoint mop: contours ){

            MatOfPoint2f mop2f = new MatOfPoint2f(mop.toArray());
            double peri = arcLength(mop2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            approxPolyDP(mop2f, approx, 0.02 * peri, true);

            if(approx.toList().size() == 4) {
                contourPoints = approx;
                break;
            }
        }
    }

    public static void logContourAreaList(List<MatOfPoint> contours) {
        for( MatOfPoint mop: contours ){
            if(mop.toList().size() == 4) {
                // Log.v("contour area", contourArea(mop) + "");
                Log.v("contour area", contourArea(mop) + "with points: ");
                for( Point p: mop.toList() ){
                    // ... p.x, p.y ... // another coordinate
                    Log.v("         -- x, y", p.x + ", " + p.y);
                }
            }
        }
    }

    public static void logCVPoints(List<org.opencv.core.Point> points) {
        for(Point point: points) {
            Log.v("point", point.x + ", " + point.y);
        }
    }

    public static List<Point> orderPoints(List<Point> points) {
        // top left, top right, bottom right, bottom left
        List<Double> pointSum = new ArrayList<>();
        List<Double> pointDiff = new ArrayList<>();
        for(Point point: points) {
            pointSum.add(point.x + point.y);
            pointDiff.add(point.y - point.x);
        }
        Point topLeft = points.get(Helper.getIndexOfSmallest(pointSum));
        Point bottomRight = points.get(Helper.getIndexOfLargest(pointSum));
        Point topRight = points.get(Helper.getIndexOfSmallest(pointDiff));
        Point bottomLeft = points.get(Helper.getIndexOfLargest(pointDiff));
        return new ArrayList<>(Arrays.asList(topLeft, topRight, bottomRight, bottomLeft));
    }

    public static Mat bitmapToMat(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        return mat;
    }

    public static Bitmap matToBitmap(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }
}
