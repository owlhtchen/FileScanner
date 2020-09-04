package com.example.filescanner;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.opencv.core.CvType.CV_32F;
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
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.warpPerspective;
import static org.opencv.utils.Converters.vector_Point_to_Mat;

public class ImageBitmap {
    private static Bitmap original;
    private static Bitmap resizedImage;
    private static List<org.opencv.core.Point> quadrilateralPoints;
    private static double originalResizeRatio;

    public static Bitmap getOriginal() {
        return original;
    }

    public static void setOriginal(Bitmap original) {
        ImageBitmap.original = original;
    }

    public static Bitmap getResizedImage() {
        return resizedImage;
    }

    public static void setResizedImage(Bitmap resizedImage) {
        ImageBitmap.resizedImage = resizedImage;
    }

    public static List<Point> getQuadrilateralPoints() {
        return quadrilateralPoints;
    }

    public static void setQuadrilateralPoints(List<Point> points) {
        for(Point point: points) {
            point.x = originalResizeRatio * point.x;
            point.y = originalResizeRatio * point.y;
        }
        ImageBitmap.quadrilateralPoints = orderPoints(points);
    }

    public static Bitmap rotateImageUpright() throws IOException {
        // https://android-developers.googleblog.com/2016/12/introducing-the-exifinterface-support-library.html

        int rotateAngle = 0;
        int width = original.getWidth();
        int height = original.getHeight();
        if(width > height) {
            rotateAngle = 90;
        }
        original = rotateImage(original, rotateAngle);
        return original;
    }

    public static Bitmap resizeImage(int newHeight) {
        originalResizeRatio = 1.0;
        if(original.getHeight() > newHeight) {
            resizedImage = resizeImage(original, newHeight);
            originalResizeRatio = original.getHeight() * 1.0 / newHeight;
        } else {
            resizedImage = Bitmap.createBitmap(original);
        }
        return resizedImage;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int newHeight) {
        double ratio = bitmap.getWidth() * 1.0 / bitmap.getHeight();
        int newWidth = (int) (ratio * newHeight);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    public static Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                matrix, true);
    }

    public static List<org.opencv.core.Point> getContourPoints() {
        return getContourPoints(resizedImage);
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
        Log.v("bitmap width, length", bitmap.getWidth() + ", " + bitmap.getHeight());
        Log.v("length of contours lists", contours.size() + "");
        logContourAreaList(contours);

        Point topLeft = new Point(70.0, 70.0);
        Point topRight = new Point(70.0 + 800, 70.0);
        Point bottomRight = new Point(70.0 + 800, 70.0 + 800);
        Point bottomLeft = new Point(70.0, 70.0 + 800);

        List<Point> contourPoints = new ArrayList<Point>(Arrays.asList(topLeft, topRight, bottomRight, bottomLeft));
        for( MatOfPoint mop: contours ){

            MatOfPoint2f mop2f = new MatOfPoint2f(mop.toArray());
            double peri = arcLength(mop2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            approxPolyDP(mop2f, approx, 0.02 * peri, true);

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

    public static void logContourAreaList(List<MatOfPoint> contours) {
        for( MatOfPoint mop: contours ){
            MatOfPoint2f mop2f = new MatOfPoint2f(mop.toArray());
            double peri = arcLength(mop2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            approxPolyDP(mop2f, approx, 0.02 * peri, true);

            Log.v(" ~ all contour area ", contourArea(mop) + ", with # corners " + approx.toList().size());
            if(approx.toList().size() == 4) {
                Log.v("contour area", contourArea(approx) + " with points: ");
                for( Point p: approx.toList() ){
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

    public static Bitmap transformToRectImage() {
        return transformToRectImage(original, quadrilateralPoints);
    }

    public static Bitmap transformToRectImage(Bitmap bitmap, List<Point> points) {
        org.opencv.core.Point tempTopLeft = quadrilateralPoints.get(0);
        org.opencv.core.Point tempTopRight = quadrilateralPoints.get(1);
        org.opencv.core.Point tempBottomRight = quadrilateralPoints.get(2);
        org.opencv.core.Point tempBottomLeft = quadrilateralPoints.get(3);

        double width1 = sqrt(pow(tempTopLeft.x - tempTopRight.x, 2.0) +
                pow(tempTopLeft.y - tempTopRight.y, 2.0));
        double width2 = sqrt(pow(tempBottomLeft.x - tempBottomRight.x, 2.0) +
                pow(tempBottomLeft.y - tempBottomRight.y, 2.0));
        double maxWidth = max(width1, width2);

        double height1 = sqrt(pow(tempTopRight.x - tempBottomRight.x, 2.0) +
                pow(tempTopRight.y - tempBottomRight.y, 2.0 ));
        double height2 = sqrt(pow(tempTopLeft.x - tempBottomLeft.x, 2.0) +
                pow(tempTopLeft.y - tempTopLeft.y, 2.0));
        double maxHeight = max(height1, height2);

        List<Point> destPoints = new ArrayList<>();
        org.opencv.core.Point topLeft = new Point(0.0, 0.0);
        org.opencv.core.Point topRight = new Point(maxWidth - 1, 0.0);
        org.opencv.core.Point bottomRight = new Point(maxWidth - 1, maxHeight - 1);
        org.opencv.core.Point bottomLeft = new Point(0.0, maxHeight - 1);
        destPoints.add(topLeft);
        destPoints.add(topRight);
        destPoints.add(bottomRight);
        destPoints.add(bottomLeft);

        Mat src = new Mat();
        Mat dest = new Mat();
        Mat tempSrc = vector_Point_to_Mat(points);
        Mat tempDest = vector_Point_to_Mat(destPoints);
        tempSrc.convertTo(src, CV_32F);
        tempDest.convertTo(dest, CV_32F);

         Log.v("src", src.dump());
         Log.v("dest", dest.dump());
        Log.v("- src", String.valueOf((src.checkVector(2, CV_32F) == 4)));
        Log.v("- dest", String.valueOf((dest.checkVector(2, CV_32F) == 4)));
        Log.v("src depth", src.depth() + "");
        Log.v("dest depth", dest.depth() + "");


        Mat transform = getPerspectiveTransform(src, dest);
         Mat srcImg = bitmapToMat(bitmap);
         Mat destImg = new Mat();
         warpPerspective(srcImg, destImg, transform, new Size(maxWidth, maxHeight));

         return matToBitmap(destImg);
    }
}
