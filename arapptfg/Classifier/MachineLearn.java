package tfg.eps.uam.es.arapptfg.Classifier;

import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

/**
 * Created by Alejandro on 02/08/2016.
 */
public class MachineLearn {

    public static final int COL_SAMPLE = 1;
    public static final int ROW_SAMPLE = 0;

    private CvSVM svm;
    private CvSVMParams params;


    /**
     * Constructor que inicializa la clase SVM con
     * los parametros por defecto.
     *
     */
    public MachineLearn() {

        svm = new CvSVM();
        params = new CvSVMParams();

    }

    /**
     *
     * @param kernel Tipo de Kernel
     * @param type Tipo de SVM
     * @param gamma Gamma
     * @param nu Nu
     * @param c C
     * @param term Terminación
     * @param maxCount Máximo de repeticiones
     * @param eps Epsilon
     */
    public MachineLearn(int kernel, int type, double gamma, double nu, double c, double term, double maxCount, double eps){

        svm = new CvSVM();
        params = new CvSVMParams();
        params.set_kernel_type(kernel);
        params.set_svm_type(type);
        params.set_gamma(gamma);
        params.set_nu(nu);
        params.set_C(c);
        //TODO Definimos los criteros de terminación {Tipo, MaxCount, Epsilon}
        double[] vals = {term, maxCount, eps};
        params.set_term_crit(new TermCriteria(vals));

    }

    /**
     *Método para entrenar la SVM
     * @param samples Ejemplos de clase
     * @param labels Etiquetas
     * @return TRUE si la maquina se ha entrenado con exito.
     */
    public boolean trainMachinLearn(Mat samples, Mat labels){


        return svm.train(samples, labels,new Mat(),new Mat(), params);
    }

    /**
     *
     * @param samples
     * @return Distancia con el centro de la clase
     */
    public float predictClass(Mat samples){

        return svm.predict(samples, true);

    }


    public int getSupporVectorCount(){

        return svm.get_support_vector_count();
    }

    public void save(String path){

        svm.save(path);
    }

    public void load(String path) {

        svm.load(path);
    }

}