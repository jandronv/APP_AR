package tfg.eps.uam.es.arapptfg.ImagenTracker;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tfg.eps.uam.es.arapptfg.Interfaces.ARFilter;

/**
 * Created by Alejandro on 02/08/2016.
 */
public class ComputeFrame implements ARFilter {

    private final Mat mReferenceImage;

    private static final String TAG = "DEBUG_CF";

//TODO comentar las variables
    private final MatOfKeyPoint mReferenceKeypoints = new MatOfKeyPoint();
    private final Mat mReferenceDescriptors = new Mat(CvType.CV_32F);
    // CVType defines the color depth, number of channels, and
    // channel layout in the image.
    private final Mat mReferenceCorners = new Mat(4, 1, CvType.CV_32FC2);
    private final MatOfKeyPoint mSceneKeypoints = new MatOfKeyPoint();
    private final Mat mSceneDescriptors = new Mat(CvType.CV_32F);
    private final Mat mCandidateSceneCorners = new Mat(4, 1, CvType.CV_32FC2);
    private final Mat mSceneCorners = new Mat(4, 1, CvType.CV_32FC2);
    private final MatOfPoint mIntSceneCorners = new MatOfPoint();

    private final MatOfPoint2f mSceneCorners2D = new MatOfPoint2f();
    // The reference image's corner coordinates, in 3D, in real units.
    private final MatOfPoint3f mReferenceCorners3D = new MatOfPoint3f();

    private MatOfPoint2f output = new MatOfPoint2f();
    private Mat homography;

    private final Mat mGraySrc = new Mat();
    private final MatOfDMatch mMatches = new MatOfDMatch();
    private final FeatureDetector mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
    private final DescriptorExtractor mDescriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    private final DescriptorMatcher mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
    private final Fla
    private final Scalar mLineColor = new Scalar(0, 255, 0);

    private final MatOfDouble mDistCoeffs = new MatOfDouble(0.06273886410053066, 0.03613977480779063, 0.0, 0.0, 0.0);

    private final CameraProjectionAdapter mCameraProjectionAdapter;
    private final MatOfDouble mRVec = new MatOfDouble();
    private final MatOfDouble mTVec = new MatOfDouble();
    private final MatOfDouble mRotation = new MatOfDouble();
    private float[] mGLPose = null;

    private boolean mTargetFound = false;

    private Context context;


    public ComputeFrame(final Context context, final int referenceImageResourceID, final CameraProjectionAdapter cameraProjectionAdapter, double realSize) throws IOException {

        //TODO cargamos la imagen de referencia, mas adelante cargaremos la SVM
        mReferenceImage = Utils.loadResource(context, referenceImageResourceID, Highgui.CV_LOAD_IMAGE_COLOR);
        //Imagen en escala de grises
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage, Imgproc.COLOR_BGR2RGBA);
        //Guardamos las esquinas de la imagen de referencia.
        mReferenceCorners.put(0, 0, new double[] {0.0, 0.0});
        mReferenceCorners.put(1, 0, new double[] {referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0, new double[] {referenceImageGray.cols(), referenceImageGray.rows()});
        mReferenceCorners.put(3, 0, new double[] {0.0, referenceImageGray.rows()});

        // Compute the image's width and height in real units, based
        // on the specified real size of the image's smaller
        // dimension.
        final double aspectRatio = (double)referenceImageGray.cols() / (double)referenceImageGray.rows();
        final double halfRealWidth;
        final double halfRealHeight;
        if (referenceImageGray.cols() > referenceImageGray.rows()) {
            halfRealHeight = 0.5f * realSize;
            halfRealWidth = halfRealHeight * aspectRatio;
        } else {
            halfRealWidth = 0.5f * realSize;
            halfRealHeight = halfRealWidth / aspectRatio;
        }

        // Define the real corner coordinates of the printed image
        // so that it normally lies in the xy plane (like a painting
        // or poster on a wall).
        // That is, +z normally points out of the page toward the
        // viewer.
        mReferenceCorners3D.fromArray(new Point3(-halfRealWidth, -halfRealHeight, 0.0),
                new Point3( halfRealWidth, -halfRealHeight, 0.0),
                new Point3( halfRealWidth,  halfRealHeight, 0.0),
                new Point3(-halfRealWidth,  halfRealHeight, 0.0));



        mFeatureDetector.detect(referenceImageGray, mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray, mReferenceKeypoints, mReferenceDescriptors);


        mCameraProjectionAdapter = cameraProjectionAdapter;

mDescriptorMatcher.
        this.context = context;
    }

    /**
     *
     * @param src
     * @param dst
     */
    public void apply(final Mat src, final Mat dst) {

        if (dst != src) {
            src.copyTo(dst);
        }
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);
        mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
        mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints, mSceneDescriptors);
        mDescriptorMatcher.match(mSceneDescriptors, mReferenceDescriptors, mMatches);
        findPose();
        draw(src, dst);

    }

    private void findPose() {


        //TODO ----Metodo con distancias cambiar por SVM ------- Si no hay distancia minima el target pasa a false
        List<DMatch> matchesList = mMatches.toList();
        if (matchesList.size() < 4) {
            // There are too few matches to find the homography.
            return;
        }
        List<KeyPoint> referenceKeypointsList =
                mReferenceKeypoints.toList();
        List<KeyPoint> sceneKeypointsList =
                mSceneKeypoints.toList();
        // Calculate the max and min distances between keypoints.
        double maxDist = 0.0;
        double minDist = Double.MAX_VALUE;
        for(DMatch match : matchesList) {
            double dist = match.distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        // The thresholds for minDist are chosen subjectively
        // based on testing. The unit is not related to pixel
        // distances; it is related to the number of failed tests
        // for similarity between the matched descriptors.
        if (minDist > 50.0) {
            // The target is completely lost.
            // Discard any previously found corners.
            mSceneCorners.create(0, 0, mSceneCorners.type());
            mTargetFound = false;

            return;
        } else if (minDist > 25.0) {
            // The target is lost but maybe it is still close.
            // Keep any previously found corners.
            mTargetFound = false;

            return;
        }

        // Identify "good" keypoints based on match distance.
        final List<Point> goodReferencePointsList = new ArrayList<Point>();
        final ArrayList<Point> goodScenePointsList = new ArrayList<Point>();
        final double maxGoodMatchDist = 1.75 * minDist;
        for (final DMatch match : matchesList) {
            if (match.distance < maxGoodMatchDist) {
                goodReferencePointsList.add(
                        referenceKeypointsList.get(match.trainIdx).pt);
                goodScenePointsList.add(
                        sceneKeypointsList.get(match.queryIdx).pt);
            }
        }

        if (goodReferencePointsList.size() < 4 ||
                goodScenePointsList.size() < 4) {
            // There are too few good points to find the pose.
            mTargetFound = false;

            return;
        }

        //TODO -----Metodo de distancias hasta aqui-------

        // There are enough good points to find the pose.
        // (Otherwise, the method would have already returned.)
        // Convert the matched points to MatOfPoint2f format, as
        // required by the Calib3d.findHomography function.
        final MatOfPoint2f goodReferencePoints = new MatOfPoint2f();
        goodReferencePoints.fromList(goodReferencePointsList);
        final MatOfPoint2f goodScenePoints = new MatOfPoint2f();
        goodScenePoints.fromList(goodScenePointsList);

        // Find the homography
        homography = Calib3d.findHomography(goodReferencePoints, goodScenePoints);

        // Use the homography to project the reference corner
        // coordinates into scene coordinates.
        Core.perspectiveTransform(mReferenceCorners,
                mCandidateSceneCorners, homography);

        // Convert the scene corners to integer format, as required
        // by the Imgproc.isContourConvex function.
        mCandidateSceneCorners.convertTo(mIntSceneCorners,
                CvType.CV_32S);

        // Check whether the corners form a convex polygon. If not,
        // (that is, if the corners form a concave polygon), the
        // detection result is invalid because no real perspective can
        // make the corners of a rectangular image look like a concave
        // polygon!
        if (!Imgproc.isContourConvex(mIntSceneCorners)) {
            return;
        }

        final double[] sceneCorner0 = mCandidateSceneCorners.get(0, 0);
        final double[] sceneCorner1 = mCandidateSceneCorners.get(1, 0);
        final double[] sceneCorner2 = mCandidateSceneCorners.get(2, 0);
        final double[] sceneCorner3 = mCandidateSceneCorners.get(3, 0);
        mSceneCorners2D.fromArray(new Point(sceneCorner0[0], sceneCorner0[1]),
                new Point(sceneCorner1[0], sceneCorner1[1]),
                new Point(sceneCorner2[0], sceneCorner2[1]),
                new Point(sceneCorner3[0], sceneCorner3[1]));
       final MatOfDouble projection = mCameraProjectionAdapter.getProjectionCV();

        // Find the target's Euler angles and XYZ coordinates.
        Calib3d.solvePnP(mReferenceCorners3D, mSceneCorners2D, projection, mDistCoeffs, mRVec, mTVec, false, Calib3d.ITERATIVE);


        // Positive y is up in OpenGL, down in OpenCV.
        // Positive z is backward in OpenGL, forward in OpenCV.
        // Positive angles are counter-clockwise in OpenGL,
        // clockwise in OpenCV.
        // Thus, x angles are negated but y and z angles are
        // double-negated (that is, unchanged).
        // Meanwhile, y and z positions are negated.

        final double[] rVecArray = mRVec.toArray();

        rVecArray[0] *= -1.0; // negate x angle
        mRVec.fromArray(rVecArray);

        // Convert the Euler angles to a 3x3 rotation matrix.
        Calib3d.Rodrigues(mRVec, mRotation);

        final double[] tVecArray = mTVec.toArray();

        // OpenCV's matrix format is transposed, relative to
        // OpenGL's matrix format.
        mGLPose = new float[16];
        mGLPose[0]  =  (float)mRotation.get(0, 0)[0];//X
        mGLPose[1]  =  (float)mRotation.get(0, 1)[0];
        mGLPose[2]  =  (float)mRotation.get(0, 2)[0];
        mGLPose[3]  =  0f;
        mGLPose[4]  =  (float)mRotation.get(1, 0)[0];//Rot Y
        mGLPose[5]  =  (float)mRotation.get(1, 1)[0];
        mGLPose[6]  =  (float)mRotation.get(1, 2)[0];
        mGLPose[7]  =  0f;
        mGLPose[8]  =  (float)mRotation.get(2, 0)[0];
        mGLPose[9]  =  (float)mRotation.get(2, 1)[0];
        mGLPose[10] =  (float)mRotation.get(2, 2)[0];
        mGLPose[11] =  0f;
        mGLPose[12] =  (float)tVecArray[0];
        mGLPose[13] = -(float)tVecArray[1]; // negate y position
        mGLPose[14] = -(float)tVecArray[2]; // negate z position
        mGLPose[15] =  1f;


        mTargetFound = true;
    }

    protected void draw(Mat src, Mat dst) {
        if (dst != src) {
            src.copyTo(dst);
        }
        if (!mTargetFound) {
            // The target has not been found.
            // Draw a thumbnail of the target in the upper-left
            // corner so that the user knows what it is.
            int height = mReferenceImage.height();
            int width = mReferenceImage.width();
            int maxDimension = Math.min(dst.width(),
                    dst.height()) / 2;
            double aspectRatio = width / (double)height;
            if (height > width) {
                height = maxDimension;
                width = (int)(height * aspectRatio);
            } else {
                width = maxDimension;
                height = (int)(width / aspectRatio);
            }
            Mat dstROI = dst.submat(0, height, 0, width);
            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(),
                    0.0, 0.0, Imgproc.INTER_AREA);

        }

    }

    @Override
    public float[] getGLPose() {
        return (mTargetFound ? mGLPose : null);
    }

    @Override
    public boolean ismTargetFound() {
        return mTargetFound;
    }

    public MatOfDouble getmTVec() {
        return mTVec;
    }

    public MatOfDouble getmRVec() {
        return mRVec;
    }
}
