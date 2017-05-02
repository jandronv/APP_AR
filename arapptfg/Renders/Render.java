package tfg.eps.uam.es.arapptfg.Renders;

import android.content.Context;
import android.renderscript.Double2;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.rajawali3d.Camera;
import org.rajawali3d.Frustum;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.scene.RajawaliScene;
import org.rajawali3d.scenegraph.IGraphNode;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import javax.microedition.khronos.opengles.GL10;

import tfg.eps.uam.es.arapptfg.ImagenTracker.CameraProjectionAdapter;
import tfg.eps.uam.es.arapptfg.R;

/**
 *
 */
public class Render extends RajawaliRenderer implements OnObjectPickedListener {

    //Variable para el cambio de escena
    private Camera mCamera1;
    private Camera mCamera2;
    private RajawaliScene mScene1;
    private RajawaliScene mScene2;
    private DirectionalLight mLight1;

    private float[] mGLPose = new float[16];
    public Context context;

    //private DirectionalLight directionalLight;
    private Sphere earthSphere;
    private Texture earthTexture;
    private ObjectColorPicker mPicker;

    private boolean rotate = false;
    private boolean mTargetFound = false;
    private static final String TAG = "DEBUG_RENDERER";

    private double[] rVecArray;

    private CameraProjectionAdapter mCameraAdapter = new CameraProjectionAdapter();

    int cont = 0;

    public Render(Context context) {
        super(context);
        this.context = context;
        setFrameRate(60);
    }

    public void initScene(){

        //Inicializamos los objetos necesarios para tener dos escenas
        mCamera1 = new Camera(); //We will utilize the initial camera
        //float[] projection = mCameraAdapter.getProjectionGL();
        //mCamera1.updateFrustum(new Matrix4(projection));//TODO
        //Matrix4 matrix = mCamera1.getViewMatrix();
        mCamera1.setPosition(0, 0, 0);
        mCamera1.setLookAt(0.0f, 0.0f, 0.0f);
        //mCamera1.disableLookAt();
        //Actualizamos el frustrum de la camara


        //matrix = mCamera1.getViewMatrix();

        mCamera2 = new Camera(); //Lets create a second camera for the scene.
        mCamera2.setPosition(5, 0, -10);
        mCamera2.setLookAt(0.0f, 0.0f, 0.0f);
        mCamera2.setFarPlane(50);
        mCamera2.setFieldOfView(60);

        mScene1 = new RajawaliScene(this, IGraphNode.GRAPH_TYPE.OCTREE);
        mScene1.displaySceneGraph(true);
        mScene1.replaceAndSwitchCamera(mCamera1, 0);
        //We are creating a second scene
        mScene2 = new RajawaliScene(this, IGraphNode.GRAPH_TYPE.OCTREE);
        mScene2.displaySceneGraph(true);
        mScene2.replaceAndSwitchCamera(mCamera2, 0);


        //Inicializamos el render
        mLight1 = new DirectionalLight(1f, .2f, -1.0f);
        mLight1.setColor(1.0f, 1.0f, 1.0f);
        mLight1.setPower(10);

        mScene1.addLight(mLight1);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColor(0);

        mPicker = new ObjectColorPicker(this);

        earthTexture = new Texture("Earth", R.drawable.earthtruecolor_nasa_big);
        try{
            material.addTexture(earthTexture);

        } catch (ATexture.TextureException error){
            Log.d(TAG , "TEXTURE ERROR");
        }

        earthSphere = new Sphere(1 , 14, 14);

        earthSphere.setZ(-1);

        earthSphere.setScale(1.0f);
        earthSphere.setMaterial(material);
        mScene1.addChild(earthSphere);
        //getCurrentCamera().setZ(4.2f);

        mPicker.setOnObjectPickedListener(this);
        mPicker.registerObject(earthSphere);

        addScene(mScene1);
        addScene(mScene2);
        //Replace the default scene with our scene 1 and switch to it
        replaceAndSwitchScene(getCurrentScene(), mScene1);

        Log.i(TAG, "Posicion inicial de la camara en el espacio: "+ mCamera1.getX() +" "+mCamera1.getY()+" "+mCamera1.getZ());
        Log.i(TAG, "Posicion del objeto 3d: "+earthSphere.getX() +" "+earthSphere.getY()+" "+earthSphere.getZ());
        Log.i(TAG, "Angulos iniciales de la camara: "+mCamera1.getRotX() +" "+mCamera1.getRotY()+" "+mCamera1.getRotZ());
    }


    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);

            if(mTargetFound){

                //Empezamos moviendo la camara
                //Ponemos la camara en el sitio correcto
                mCamera1.setPosition(mGLPose[12], mGLPose[13], mGLPose[14]);
                //Log.i(TAG, "Traslacion camara X: "+mCamera1.getX() +" Y: "+ mCamera1.getY()+" Z: " +mCamera1.getZ());


                //mCamera1.rotate(new Matrix4(mGLPose));
                //TODO Rotamos la camara
                //earthSphere.setRotation(new Vector3(rVecArray[0], rVecArray[1], rVecArray[2]));
              //  Log.i(TAG, "Angulos de la camara: "+mCamera1.getRotX() +" "+mCamera1.getRotY()+" "+mCamera1.getRotZ());
                //mCamera1.setRotation(new Vector3(rVecArray[0], rVecArray[1], rVecArray[2]));


            //mCamera1.setRotation(new Vector3(rVecArray[0], rVecArray[1], rVecArray[2]));
            /*Matrix4 matrixRot = new Matrix4();//Identity
            matrixRot.setAll(new Vector3(mGLPose[0], mGLPose[1], mGLPose[2]),new Vector3(mGLPose[4], mGLPose[5], mGLPose[6]),new Vector3(mGLPose[8], mGLPose[9], mGLPose[10]),new Vector3(mGLPose[12], mGLPose[13], mGLPose[14]));
            mCamera1.setRotation(matrixRot);*/

            //TODO mCamera1.setRotation(new Quaternion()); o mCamera1.setRotation(new Matrix4());

            //mCamera1.setRotation(new Matrix4(mGLPose));
           // mCamera1.setRotation(new Quaternion());
           //TODO el movimiento se aplica Log.i(TAG, "Aplicamos la matriz de rotacion X: "+mCamera1.getRotX() +" Y: "+ mCamera1.getRotY()+" Z: " +mCamera1.getRotZ());
        }

    }

    //Cambiamos la escena para ocultar
    public void changeScene() {

        switchScene(mScene1);
        getCurrentScene().switchCamera(mCamera1);

    }

    public void changeScene2() {
        switchScene(mScene2);
        getCurrentScene().switchCamera(mCamera2);

    }

    public void onOffsetsChanged(float x, float y, float z, float w, int i, int j){

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    public void onObjectPicked(Object3D object) {


        //TODO 12/08 mirar bien vectores de rotacion
       // earthSphere.(new Vector3(100,0,3));

    }

    public void getObjectAt(float x, float y) {
        mPicker.getObjectAt(x, y);
    }

    public void setmTargetFound(boolean mTargetFound) {
        this.mTargetFound = mTargetFound;
    }

    public void setGLPose(float[] mGLPose) {
        this.mGLPose = mGLPose;
    }

    public void setrVecArray(double[] rVecArray) {
        this.rVecArray = rVecArray;
    }
}