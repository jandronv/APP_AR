package tfg.eps.uam.es.arapptfg;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import java.io.IOException;
import java.util.ArrayList;

import tfg.eps.uam.es.arapptfg.ImagenTracker.ComputeFrame;
import tfg.eps.uam.es.arapptfg.ImagenTracker.PoseEstimation;

public class FindChessActivity extends Activity {

    private Bitmap bitmap;

    private ImageView iv;
    private static final String TAG = "FindChess::Activity";
    private PoseEstimation poseEstimation;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        poseEstimation = new PoseEstimation(FindChessActivity.this, R.drawable.chess5);

                        bitmap = poseEstimation.getBitmap();
                        muestraImagen(bitmap);

                        //Calclamos la pose
                        bitmap = poseEstimation.findChessCorners();
                        muestraImagen(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(layout);

        iv = new ImageView(this);

        layout.addView(iv);
    }

    public void muestraImagen(Bitmap bitmap){
        iv.setImageBitmap(bitmap);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}