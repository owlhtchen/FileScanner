package com.example.filescanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DrawQuadrilateral extends View {
    // https://stackoverflow.com/questions/8974088/how-to-create-a-resizable-rectangle-with-user-touch-events-on-android
    Point[] points = new Point[4];

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    private ArrayList<ColorBall> colorballs = new ArrayList<>();

    // array that holds the balls
    private int balID = 0;
    // variable to know what ball is being dragged
    Paint paint;
    Path path;

    public DrawQuadrilateral(Context context) {
        this(context, null);
    }

    public DrawQuadrilateral(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DrawQuadrilateral(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        path = new Path();
        setFocusable(true); // necessary for getting the touch events
    }

    private void initRectangle() {
        //initialize rectangle.
        RelativeLayout r = (RelativeLayout) (this.getParent());
        ImageView imageView = r.findViewById(R.id.imported_image);
//        Bitmap tempBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        List<org.opencv.core.Point> contourPoints = ImageBitmap.getContourPoints();

//        double imageWidth = ImageBitmap.getOriginal().getWidth();
        double imageWidth = imageView.getDrawable().getIntrinsicWidth();
//        double imageHeight = ImageBitmap.getOriginal().getHeight();
        double imageHeight = imageView.getDrawable().getIntrinsicHeight();
//        double displayWidth = getWidth();
        double displayWidth = imageView.getMeasuredWidth();
//        double displayHeight = getHeight();
        double displayHeight = imageView.getMeasuredHeight();

        // https://stackoverflow.com/questions/27730557/coordinate-conversion-from-org-opencv-core-point-to-android-graphics-point
        double scale = Math.min(displayWidth / imageWidth, displayHeight / imageHeight);
        double xOffset = (displayWidth - scale * imageWidth) / 2.0;
        double yOffset = (displayHeight - scale * imageHeight) / 2.0;

        for(int i = 0; i < 4; i++) {
            points[i] = new Point();
            points[i].x = (int) (contourPoints.get(i).x * scale + xOffset);
            points[i].y = (int) (contourPoints.get(i).y * scale + yOffset);
//            Log.v("width", contourPoints.get(i).x  + "");
//            Log.v("height", contourPoints.get(i).y  + "");
        }
        // debug
//        points[0] = new Point();
//        points[0].x = 0;
//        points[0].y = 0;
//        points[1] = new Point();
//        points[1].x = imageView.getWidth();
//        points[1].y = 0;
//        points[2] = new Point();
//        points[2].x = imageView.getWidth();
//        points[2].y = imageView.getHeight();
//        points[3] = new Point();
//        points[3].x = 0;
//        points[3].y = imageView.getHeight();

        // declare each ball with the ColorBall class
        for (int i = 0; i < points.length; i++) {
            colorballs.add(new ColorBall(getContext(), R.drawable.gray_circle, points[i], i));
        }
    }

    // the method that draws the balls
    @Override
    protected void onDraw(Canvas canvas) {
        if(points[3]==null) {
            //point4 null when view first create
            initRectangle();
        }

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(15);

        path.reset();
        path.moveTo(points[0].x, points[0].y);
        path.lineTo(points[0].x, points[0].y);
        path.lineTo(points[1].x, points[1].y);
        path.lineTo(points[2].x, points[2].y);
        path.lineTo(points[3].x, points[3].y);
        path.lineTo(points[0].x, points[0].y);
        canvas.drawPath(path, paint);

        // draw the balls on the canvas
        paint.setColor(Color.GRAY);
        paint.setTextSize(30);
        paint.setStrokeWidth(0);
        for (int i =0; i < colorballs.size(); i ++) {
            ColorBall ball = colorballs.get(i);
            canvas.drawBitmap(ball.getBitmap(), (float) (ball.getX() - ball.getWidthOfBall() / 2.0),
                    (float) (ball.getY() - ball.getHeightOfBall() / 2.0),
                    paint);

            canvas.drawText("" + i, (float) (ball.getX() - ball.getWidthOfBall() / 2.0),
                    (float) (ball.getY() - ball.getHeightOfBall() / 2.0), paint);
        }
    }

    // events when touching the screen
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        int X = (int) event.getX();
        int Y = (int) event.getY();

        switch (eventAction) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                // a ball
                if (points[0] == null) {
                    initRectangle();
                } else {
                    //resize rectangle
                    balID = -1;
                    for (int i = colorballs.size()-1; i>=0; i--) {
                        ColorBall ball = colorballs.get(i);
                        // check if inside the bounds of the ball (circle)
                        // get the center for the ball
                        int centerX = ball.getX() - ball.getWidthOfBall() / 2;
                        int centerY = ball.getY() - ball.getHeightOfBall() / 2;
                        paint.setColor(Color.CYAN);
                        // calculate the radius from the touch to the center of the
                        // ball
                        double radCircle = Math
                                .sqrt((double) (((centerX - X) * (centerX - X)) + (centerY - Y)
                                        * (centerY - Y)));

                        double TOUCH_SCALE = 1.2;
                        if (radCircle < TOUCH_SCALE * ball.getWidthOfBall()
                                || radCircle < TOUCH_SCALE * ball.getHeightOfBall()) {

                            balID = ball.getID();
                            invalidate();
                            break;
                        }
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE: // touch drag with the ball

                if (balID > -1) {
                    // move the balls the same as the finger
                    colorballs.get(balID).setX(X);
                    colorballs.get(balID).setY(Y);
//                    Log.v("touching", X + ", " + Y);

                    paint.setColor(Color.CYAN);
                    invalidate();
                }

                break;

            case MotionEvent.ACTION_UP:
                // touch drop - just do things here after dropping
                // doTheCrop()
                break;
        }
        // redraw the canvas
        invalidate();
        return true;
    }

    public static class ColorBall {

        Bitmap bitmap;
        Context mContext;
        Point point;
        int id;

        public ColorBall(Context context, int resourceId, Point point, int id) {
            this.id = id;
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    resourceId);
            mContext = context;
            this.point = point;
        }

        public int getWidthOfBall() {
            return bitmap.getWidth();
        }

        public int getHeightOfBall() {
            return bitmap.getHeight();
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getX() {
            return point.x;
        }

        public int getY() {
            return point.y;
        }

        public int getID() {
            return id;
        }

        public void setX(int x) {
            point.x = x;
        }

        public void setY(int y) {
            point.y = y;
        }
    }
}
