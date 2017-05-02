package tfg.eps.uam.es.arapptfg.ImagenTracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import tfg.eps.uam.es.arapptfg.R;

public class PoseEstimation {


    private static final String TAG = "POSE_ESTIMATION";
    private Mat mReferenceImage;
    private Mat mReferenceImageColor;
    private CameraProjectionAdapter mCameraProjectionAdapter;
    private boolean find;
    private Bitmap bm;
    private TermCriteria criteria;
    private MatOfDouble cameraMatrix;
    private MatOfDouble mDistCoeffs;
    private final MatOfDouble mRVec;
    private final MatOfDouble mTVec;
    private MatOfPoint3f objp;

    public PoseEstimation(Context context,  int referenceImageResourceID) throws IOException {

       //Cargamos la imagen que vamos a analizar y la transformamos en bitmap para android
        mReferenceImage = Utils.loadResource(context, referenceImageResourceID, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        mReferenceImageColor = Utils.loadResource(context, referenceImageResourceID, Highgui.CV_LOAD_IMAGE_COLOR);
       bm = Bitmap.createBitmap(mReferenceImage.cols(), mReferenceImage.rows(),Bitmap.Config.ARGB_8888);
       Utils.matToBitmap(mReferenceImage, bm);

        //inicializamos el termcriteria
        criteria = new TermCriteria(TermCriteria.EPS+TermCriteria.MAX_ITER,30,0.1);
        mTVec = new MatOfDouble();
        mRVec = new MatOfDouble();
        objp = new MatOfPoint3f();


    }

    public Bitmap getBitmap(){
        return bm;
    }

    public Bitmap findChessCorners(){


        //Creamos el axis y la referencia 2d de la imagen y la salida de projecoint
        MatOfPoint3f axis = new MatOfPoint3f(new Point3(3,0,0),new Point3(0,3,0),new Point3(0,0,3));
        Log.i(TAG,"antes " + axis.dump());
        axis.reshape(-1,3);
        Log.i(TAG,"des " + axis.dump());
        MatOfPoint2f corners = new MatOfPoint2f();
        MatOfPoint2f imgpts = new MatOfPoint2f();


        //Buscamos el tablero de ajedrez
        find = Calib3d.findChessboardCorners(mReferenceImage, new Size(6, 9), corners, Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_FILTER_QUADS);
        if(find) {
            //TODO inicializar a 0?? objp[:,:2] = np.mgrid[0:7,0:6].T.reshape(-1,2), object coordinate space
            Log.i(TAG,"Encontrado" );
            //Refinamosla busqueda del tablero
            Imgproc.cornerSubPix(mReferenceImage, corners, new Size(11,11), new Size(-1,-1),criteria);
            iniProjectionAndDist();

            Log.i(TAG, "Array of object points in the object coordinate space: "+objp.dump());
            Log.i(TAG, "Array of corresponding image points: "+corners.dump());
            Log.i(TAG, "Matriz de proyeccion: "+cameraMatrix.dump());
            Log.i(TAG, "Matriz de distorsión: "+mDistCoeffs.dump());

            //Finds an object pose from 3D-2D point correspondences.
            Calib3d.solvePnPRansac(objp, corners, cameraMatrix, mDistCoeffs, mRVec, mTVec);
            Log.i(TAG, "Vector de rotación:"+ mRVec.dump());
            Log.i(TAG, "Vector de rotación:"+ mTVec.dump());
            //Projects 3D points to an image plane.
            Calib3d.projectPoints(axis, mRVec, mTVec, cameraMatrix, mDistCoeffs, imgpts);
            Axis(mReferenceImageColor, corners, imgpts);

            Bitmap finalBm = Bitmap.createBitmap(mReferenceImageColor.cols(), mReferenceImageColor.rows(),Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mReferenceImageColor, finalBm);

            return finalBm;

        }else {
            Log.i(TAG,"No encontrado");

        }

        return null;
    }

    public void iniProjectionAndDist(){

        //Cargamos a "mano" los datos obtenidos de la calibración de la camara
        cameraMatrix = new MatOfDouble();
        cameraMatrix.create(3,3, CvType.CV_64FC1);
        //row x col
        cameraMatrix.put(0,0,832.8247303013591);
        cameraMatrix.put(0,1,0);
        cameraMatrix.put(0,2,431.5);
        cameraMatrix.put(1,0,0);
        cameraMatrix.put(1,1,832.8247303013591);
        cameraMatrix.put(1,2,239.5);
        cameraMatrix.put(2,0,0);
        cameraMatrix.put(2,1,0);
        cameraMatrix.put(2,2,1);

        //DAtos obtenidos de CameraCalibration de OpencvApp
        mDistCoeffs = new MatOfDouble(0.06273886410053066, 0.03613977480779063, 0.0, 0.0, 0.0);

        //Inicializar objp 0 0 0, 1 0 0, 2 0 0... 8 0 0, 0
        ArrayList<Point3> point3ArrayList = new ArrayList<Point3>();
        for(int i = 0;i < 9;i++) {
            for(int j = 0;j< 6; j++ ){
                point3ArrayList.add(new Point3((double) i, (double)j, 0.0));
            }
        }
        objp.fromList(point3ArrayList);

        Log.i(TAG, "Matriz objp"+objp.dump());
    }

    public void Axis(Mat dst, MatOfPoint2f corners, MatOfPoint2f imgpt){

        Log.i(TAG,"corners "+ corners.dump() + "  ->" + corners.get(0,0)[0] + " "+corners.get(0,0)[1]);

        Log.i(TAG,"imgpt "+ imgpt.dump() + "  ->" + imgpt.get(1,0)[0] + " "+imgpt.get(2,0)[1]);

        //cv2.line(img, corner, tuple(imgpts[0].ravel()), (255,0,0), 5) , ravel => ([[1, 2, 3], [4, 5, 6]]) --> [1 2 3 4 5 6] || tuple =>aList = (123, 'xyz', 'zara', 'abc'); -->  (123, 'xyz', 'zara', 'abc')
        Core.line(dst, new Point(corners.get(0,0)[0], corners.get(0,0)[1]), new Point(imgpt.get(0,0)[0],imgpt.get(0,0)[1]), new Scalar(255, 0, 0),5);//Red
        Core.line(dst, new Point(corners.get(0,0)[0], corners.get(0,0)[1]), new Point(imgpt.get(1,0)[0],imgpt.get(1,0)[1]), new Scalar(0, 255, 0),5);//Green
        Core.line(dst, new Point(corners.get(0,0)[0], corners.get(0,0)[1]), new Point(imgpt.get(2,0)[0],imgpt.get(2,0)[1]), new Scalar(0, 0, 255),5);//Blue*/

    }
}
