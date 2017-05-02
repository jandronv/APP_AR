package tfg.eps.uam.es.arapptfg.ImagenTracker;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.opengl.Matrix;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Size;

/**
 * Created by Alejandro Núñez Valle on 07/01/2016.
 *
 * Clase necesaria para construir las matrices de proyección
 */
@SuppressWarnings("deprecation")
public class CameraProjectionAdapter  {

    float mFOVY   = 42.5f;//42.5f; degrees //radians 7.41;
    float mFOVX   = 54.9f;//54.9f; degrees //radians 9.58;
    int mHeightPx = 864; //ancho
    int mWidthPx  = 480; //alto
    float mFocalLengh = 3.5f;
    //TODO probar con distancia focal 3,5 y la resolucion máxima
    float mNear   = 1f;
    float mFar    = 10000f;

    final float[] mProjectionGL = new float[16];
    boolean mProjectionDirtyGL = true;
    MatOfDouble mProjectionCV;
    boolean mProjectionDirtyCV = true;


    /**
     * Utilizamos los valores por defecto que nos proporciona Camera.Parameters
     *
     * @param parameters
     */
    public void setCameraParameters(Parameters parameters){

        mFOVY = parameters.getVerticalViewAngle();
        mFOVX = parameters.getHorizontalViewAngle();

        Camera.Size pictureSize = parameters.getPictureSize();
        mHeightPx = pictureSize.height;
        mWidthPx = pictureSize.width;

        mFocalLengh = parameters.getFocalLength();

        mProjectionDirtyGL = true;
        mProjectionDirtyCV = true;
    }

    /**
     * Metodo para obtener la relación de aspecto de la imagen
     *
     * @return ratio de la imagen
     */
    public float getAspectRatio() {
        return (float)mWidthPx / (float)mHeightPx;
    }

    /**
     *
     * @param near
     * @param far
     */
    public void setClipDistances(float near, float far){

        mNear = near;
        mFar = far;
        mProjectionDirtyGL = true;
    }

    /**
     * Metodo para obtener la proyeccion en OpenGL utilizando la función "frustumM"
     * @return
     */
    public float[] getProjectionGL(){

        if (mProjectionDirtyGL) {
            final float top = (float)Math.tan(mFOVY * Math.PI / 360f) * mNear;
            final float right = (float)Math.tan(mFOVX * Math.PI / 360f) * mNear;
            Matrix.frustumM(mProjectionGL, 0, -right, right, -top, top, mNear, mFar);
            mProjectionDirtyGL = false;
        }
        return mProjectionGL;
    }

    /**
     * La matriz de proyeccion es mas complicada en openCV. Tenemos que calcularla a "mano"
     * @return
     */
    public MatOfDouble getProjectionCV(){



        if (mProjectionCV == null) {
            mProjectionCV = new MatOfDouble();
            mProjectionCV.create(3, 3, CvType.CV_64FC1);
        }

        mProjectionCV.put(0,0,832.8247303013591);
        mProjectionCV.put(0,1,0);
        mProjectionCV.put(0,2,431.5);
        mProjectionCV.put(1,0,0);
        mProjectionCV.put(1,1,832.8247303013591);
        mProjectionCV.put(1,2,239.5);
        mProjectionCV.put(2,0,0);
        mProjectionCV.put(2,1,0);
        mProjectionCV.put(2,2,1);

        return mProjectionCV;
    }

    /**
     *1ª Salida
     *
     Average re-projection error: 0,186779

      Camera matrix: [832.8247303013591, 0, 431.5;
                      0, 832.8247303013591, 239.5;
                      0, 0, 1]
     Distortion coefficients: [0.06273886410053066;
                              0.03613977480779063;
                              0;
                              0;
                              0]
     Saved camera matrix: [832.8247303013591, 0, 431.5;
                          0, 832.8247303013591, 239.5;
                         0, 0, 1]
      Saved distortion coefficients: [0.06273886410053066;
                                     0.03613977480779063;
                                     0;
                                     0;
                                     0]


     2ª Salida

     Camera matrix: [824.5179590637462, 0, 431.5;
                     0, 824.5179590637462, 239.5;
                     0, 0, 1]
     Distortion coefficients: [0.08881853949410584;
                              -0.3497709308248249;
                               0;
                               0;
                               0]
     Saved camera matrix: [824.5179590637462, 0, 431.5;
                          0, 824.5179590637462, 239.5;
                          0, 0, 1]
     Saved distortion coefficients: [0.08881853949410584;
                                     -0.3497709308248249;
                                     0;
                                     0;
                                     0]


     */
}
