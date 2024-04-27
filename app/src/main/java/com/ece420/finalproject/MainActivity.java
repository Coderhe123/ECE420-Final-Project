package com.ece420.finalproject;

import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.findContours;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.Manifest;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.content.res.Resources;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.core.MatOfPoint2f;
import org.opencv.android.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    // UI Variables
    private Button startButton;
    private Button captureButton;

    // Declare OpenCV based camera view base
    private CameraBridgeViewBase mOpenCvCameraView;

    // Mat to store RGBA and Grayscale camera preview frame
    private Mat mRgba;
    private Mat mGray;
    private Mat transformRgba;
    private Mat transformGray;

    private final int transWidth = 600;
    private final int transHeight = 600;
    private final int WIDTH2048 = 4;
    private final int HEIGHT2048 = 4;
    private int opencv_loaded_flag = -1;
    private int start_flag = -1;

    private int [][] Mat2048;
    private List<Pair<Integer, Mat>> Templates;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Request User Permission on Camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        // OpenCV Loader and Avoid using OpenCV Manager
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        // Setup start button
        startButton = (Button) findViewById((R.id.startButton));
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_flag == -1 && opencv_loaded_flag == 1) {
                    start_flag = 1;
                    startButton.setText("STOP"); // Modify text
                    mOpenCvCameraView.enableView();
                } else if (start_flag == 1) {
                    start_flag = -1;
                    startButton.setText("START"); // Modify text
                    mOpenCvCameraView.disableView();
                }
            }
        });

        captureButton = (Button) findViewById((R.id.captureButton));
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start_flag == -1) return;
                getTransformView();
                DigitRecognition();
                getSolution();
            }
        });

        // Setup OpenCV Camera View
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_camera_preview);
        // Use main camera with 0 or front camera with 1
        mOpenCvCameraView.setCameraIndex(0);
        // Force camera resolution
        // mOpenCvCameraView.setMaxFrameSize(1280, 720);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    opencv_loaded_flag = 1;
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    // OpenCV Camera Functionality Code
    @Override
    public void onCameraViewStarted(int width, int height) {
        LoadLib();
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        transformRgba = new Mat(transWidth, transHeight, CvType.CV_8UC4);
        transformGray = new Mat(transWidth, transHeight, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        transformRgba.release();
        transformGray.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // Grab camera frame in rgba and grayscale format
        mRgba = inputFrame.rgba();
        // Grab camera frame in gray format
        mGray = inputFrame.gray();
        // Returned frame will be displayed on the screen
        return mRgba;
    }

    public void LoadLib() {
        // Test Game2048 AI algorithm
//        Game2048 game = new Game2048();
//        game.playWithAI();


        Mat2048 = new int[HEIGHT2048][WIDTH2048];
        // Initialize original matrix value to 0
        for (int i = 0; i < HEIGHT2048; i++) {
            for (int j = 0; j < WIDTH2048; j++) {
                Mat2048[i][j] = 0;
            }
        }

        // Import the templates
        Resources resources = getResources();
        Templates = new ArrayList<>();
        int num = 2;
        while (num <= 2048) {
            String fileName = "gray" + num;
            int resourceId = resources.getIdentifier(fileName, "raw", getPackageName());
            InputStream inputStream = resources.openRawResource(resourceId);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Mat template_ = new Mat();
            Utils.bitmapToMat(bitmap, template_);
            Imgproc.cvtColor(template_, template_, Imgproc.COLOR_RGBA2GRAY);
            Templates.add(new Pair<>(num, template_));

            num *= 2;
        }
    }

    public void getTransformView() {
        // Create a copy for stability in real-time system
        Mat grayCopy = mGray.clone();
        Mat rgbaCopy = mRgba.clone();

        // Get canny edges
        Mat edges = new Mat();
        Imgproc.Canny(grayCopy, edges,50, 150, 3, false);

        // Get contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Get outer-contours by maximized area
        MatOfPoint outerContour = null;
        double maxArea = -1;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                outerContour = contour;
            }
        }

        if (outerContour == null) return;
        // Calculate hull convex
        MatOfInt hull = new MatOfInt();
        Imgproc.convexHull(outerContour, hull);

        Point[] hullPoints = new Point[hull.rows()];
        for (int i = 0; i < hull.rows(); i++) {
            int index = (int) hull.get(i, 0)[0];
            hullPoints[i] = outerContour.toArray()[index];
        }

        // Find four corner points
        Point topLeft = hullPoints[0];
        Point topRight = hullPoints[0];
        Point bottomLeft = hullPoints[0];
        Point bottomRight = hullPoints[0];
        for (Point point : hullPoints) {
            if (point.x + point.y < topLeft.x + topLeft.y) topLeft = point;
            if (point.x - point.y > topRight.x - topRight.y) topRight = point;

            if (point.x - point.y < bottomLeft.x - bottomLeft.y) bottomLeft = point;
            if (point.x + point.y > bottomRight.x + bottomRight.y) bottomRight = point;
        }

        // Create point source and point destination
        Point[] pts_dst = new Point[]{
                new Point(0, 0),
                new Point(transWidth - 1, 0),
                new Point(transWidth - 1, transHeight - 1),
                new Point(0, transHeight - 1)
        };
        MatOfPoint2f pts_src = new MatOfPoint2f();
        Point[] corner_points_array = new Point[]{
                topLeft,
                topRight,
                bottomRight,
                bottomLeft
        };
        pts_src.fromArray(corner_points_array);

        // Perspective transform
        Mat perspective_matrix = Imgproc.getPerspectiveTransform(pts_src, new MatOfPoint2f(pts_dst));
        Imgproc.warpPerspective(rgbaCopy, transformRgba, perspective_matrix, new Size(transWidth, transHeight));
        Imgproc.warpPerspective(grayCopy, transformGray, perspective_matrix, new Size(transWidth, transHeight));

        // Image View on app
        ImageView imageView = (ImageView) findViewById(R.id.transformView);
        Bitmap bitmap = Bitmap.createBitmap(transformRgba.cols(), transformRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(transformRgba, bitmap);
        imageView.setImageBitmap(bitmap);

        // Release Memory
        edges.release();
        hierarchy.release();
        hull.release();
    }

    public void DigitRecognition() {
        final int PerDigitWidth = transWidth / WIDTH2048;
        final int PerDigitHeight = transHeight / HEIGHT2048;
        for (int row = 0; row < HEIGHT2048; row++) {
            for (int col = 0; col < WIDTH2048; col++) {

                // Calculate ROI coordinates
                int x = col * PerDigitWidth, y = row * PerDigitHeight;
                Rect roi = new Rect(x, y, PerDigitWidth, PerDigitHeight);
                Mat croppedImage = new Mat(transformGray, roi);

                List<Pair<Integer, Double>> Matches = new ArrayList<>();
                for (Pair<Integer, Mat> pair : Templates) {
                    int num = pair.first;
                    Mat template = pair.second;
                    Mat result = new Mat();

                    // Match with template
                    Imgproc.matchTemplate(croppedImage, template, result, Imgproc.TM_CCOEFF_NORMED);
                    Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                    double maxVal = mmr.maxVal;

                    // Store the result along with the index of the template
                    Matches.add(new Pair<>(num, maxVal));
                }

                // Find the index of the template with the highest maximum value
                int bestIndex = -1;
                double maxVal = 0.0;
                for (Pair<Integer, Double> Match : Matches) {
                    if (Match.second > maxVal) {
                        maxVal = Match.second;
                        bestIndex = Match.first;
                    }
                }

                // Claim it to be a number if maxVal >= 0.6
                int result = (maxVal >= 0.6) ? (bestIndex ) : 0;
                Mat2048[row][col] = result;
            }
        }

        // Visual Effect, write on the text view
        Resources resources = getResources();
        for (int i = 0; i < HEIGHT2048; i++) {
            for (int j = 0; j < WIDTH2048; j++) {
                String textName = "A" + i + j;
                int textID = resources.getIdentifier(textName, "id", getPackageName());
                TextView textElement = (TextView) findViewById(textID);
                if (Mat2048[i][j] == 0) textElement.setText("");
                else textElement.setText(String.valueOf(Mat2048[i][j]));
            }
        }
    }

    public void getSolution() {
        if (Mat2048.length != HEIGHT2048 || Mat2048[0].length != WIDTH2048) return;
        Game2048 game = new Game2048(Mat2048);
        int action = game.AImove();
        TextView textLeft= (TextView) findViewById(R.id.Left);
        TextView textRight = (TextView) findViewById(R.id.Right);
        TextView textUp= (TextView) findViewById(R.id.Up);
        TextView textDown = (TextView) findViewById(R.id.Down);

        textLeft.setBackgroundResource(R.drawable.grid_border);
        textRight.setBackgroundResource(R.drawable.grid_border);
        textUp.setBackgroundResource(R.drawable.grid_border);
        textDown.setBackgroundResource(R.drawable.grid_border);
        if (action == 0) textLeft.setBackgroundResource(R.drawable.red_border);
        else if (action == 1) textRight.setBackgroundResource(R.drawable.red_border);
        else if (action == 2) textUp.setBackgroundResource(R.drawable.red_border);
        else if (action == 3) textDown.setBackgroundResource(R.drawable.red_border);
    }
}