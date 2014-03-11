/**
 * Created by max on 3/11/14.
 *
 * Provides a method to generate random graphs from the pseudocode in the project pdf
 * The data is assumed to be too large to store in main memory
 * Therefore the data should be written to some file and this method should return a reference to this file
 *
 */
import java.util.*;

public class DataGenerator {
    int n = 10;
    double alpha = 0.5;

    public void GenerateData(int n, double alpha){
        int span = 0;
        int origin = 0;
        for(int i=n; i< 3*n; i++){
            span = Math.ceil((1 - Math.random()**alpha)*(V - 1));    //What is V??
            origin = Math.floor(Math.random() * (V - span));
            this.newEdge(origin,origin+span);
        }

    }

    /**
     * Add a new edge to the graph. Not sure how to implement this yet.
     * @param i
     * @param j
     */
    private void newEdge(int i, int j){

    }

}
