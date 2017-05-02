package tfg.eps.uam.es.arapptfg;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import android.util.Log;

import android.widget.FrameLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;


import java.io.IOException;

import tfg.eps.uam.es.arapptfg.ImagenTracker.CameraProjectionAdapter;
import tfg.eps.uam.es.arapptfg.ImagenTracker.ComputeFrame;

import tfg.eps.uam.es.arapptfg.Renders.ARCubeRender;
import tfg.eps.uam.es.arapptfg.Renders.Render;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{


    private static final String STATE_CAMERA_INDEX = "cameraIndex";
    private static final String STATE_IMAGE_SIZE_INDEX = "imageSizeIndex";
    private static final String TAG = "DEBUG_MAIN";

    // The camera view.
    private CameraBridgeViewBase mOpenCvCameraView;
    // The index of the active camera.
    private int mCameraIndex;
    // The index of the active image size.
    private int mImageSizeIndex;
    private ComputeFrame mComputeFrame;
    // An adapter between the video camera and projection matrix.
    private CameraProjectionAdapter mCameraProjectionAdapter;

    // The renderer for 3D augmentations.
    private ARCubeRender mARRenderer;

    int cont = 0;


    public MainActivity(){
        Log.i(TAG, "Creada clase" + this.getClass());
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();


                    try {
                        mComputeFrame = new ComputeFrame(MainActivity.this, R.drawable.starry, mCameraProjectionAdapter, 1.0);
                        mARRenderer.filter = mComputeFrame;
                    } catch (IOException e) {
                        Log.e(TAG, "Error al inicializar ComputeFrame");
                        e.printStackTrace();
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
            mImageSizeIndex = savedInstanceState.getInt(STATE_IMAGE_SIZE_INDEX, 0);
        } else {
            mCameraIndex = 0;
            mImageSizeIndex = 0;
        }

        final FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(layout);
        //Añadimos a la vista de la aplicación lo que muestra la camara.
        mOpenCvCameraView = new JavaCameraView(this, 0);

        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mOpenCvCameraView.setMaxFrameSize(864, 480);

        mOpenCvCameraView.enableFpsMeter();
        layout.addView(mOpenCvCameraView);


        //Inicializmamos el 3d
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        glSurfaceView.setZOrderOnTop(true);
        glSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(glSurfaceView);

        mCameraProjectionAdapter = new CameraProjectionAdapter();

        mARRenderer = new ARCubeRender();
        mARRenderer.cameraProjectionAdapter = mCameraProjectionAdapter;

        // Earlier, we defined the printed image's size as 1.0
        // unit.
        // Define the cube to be half this size.
        mARRenderer.scale = 0.5f;
        glSurfaceView.setRenderer(mARRenderer);

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current camera index.
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
        // Save the current image size index.
        savedInstanceState.putInt(STATE_IMAGE_SIZE_INDEX, mImageSizeIndex);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
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


    //TODO ERROR Rajawali al minimizar la aplicacion
    @Override
    public void onStop(){
        super.onStop();


    }

    @Override
    public void onRestart(){
        super.onRestart();

    }

    public void onDestroy() {

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        super.onDestroy();

    }

 
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        final Mat mRgbFrame = inputFrame.rgba();

        //TODO 04/08-> usar el metodo de distancias ver como funcion, despues utilizar las SVM
        Log.i(TAG, "Tamaño:"+ mRgbFrame.size());
        mComputeFrame.apply(mRgbFrame, mRgbFrame);
        //printAreaTracking(mRgbFrame , mRgbFrame);
        //Con findChess dejamos de mostrar la entrada de la camara y mostramos el tablero con la estimacion

        return mRgbFrame;
    }


}
