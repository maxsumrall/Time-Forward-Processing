/**
 * Created by max on 3/11/14.
 *
 * Provides a method to generate random graphs from the pseudocode in the project pdf
 * The data is assumed to be too large to store in main memory
 * Therefore the data should be written to some file and this method should return a reference to this file
 *
 */
import java.nio.channels.FileChannel;
import java.util.*;
import java.io.*;
import java.nio.*;

public class DataGenerator {
    int n = 10;
    double alpha = 0.5;

    MappedByteBuffer buffer;
    FileChannel fc;
    File file = new File("genData.dat");

    public DataGenerator(){
        try{
            this.fc = new RandomAccessFile(this.file, "rw").getChannel();
            this.buffer = fc.map(FileChannel.MapMode.READ_WRITE,0,(int) fc.size());
            this.buffer.putInt(n);//the first int will be the number of vertices
        }
        catch(IOException e){
            System.out.println("IO Problem " + e.getMessage());
        }
    }

    public void GenerateData(int n, double alpha){
        int span = 0;
        int origin = 0;
        int V = 2;//NOOOO this is just temp
        for(int i=n; i< 3*n; i++){
            span = (int) Math.ceil((1 - Math.pow(Math.random(), this.alpha)) * (V - 1));
            origin = (int) Math.floor(Math.random() * (V - span));
            this.newEdge(origin,origin+span);
        }
        //Done with generating and writing data, close everything.
        this.buffer.force();
        try {
            this.fc.close();
        }
        catch (IOException e){System.out.println("IO Error on close: " + e.getMessage());}

    }

    /**
     * Add a new edge to the graph. Not sure how to implement this yet.
     * @param i origin
     * @param j destination
     */
    private void newEdge(int i, int j){
        if (this.buffer != null){
            this.buffer.putInt(i);
            this.buffer.putInt(j);
        }

    }

}
