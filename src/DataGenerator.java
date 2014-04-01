/**
 * Created by max on 3/11/14.
 *
 * Provides a method to generate random graphs from the pseudocode in the project pdf
 * The data is assumed to be too large to store in main memory
 * Therefore the data should be written to some file and this method should return a reference to this file
 *
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DataGenerator {

    MappedByteBuffer buffer;
    FileChannel fc;
    File file;


    /**
     * Opens a file, gets the channel, makes a memorymappedbuffer over this channel,
     * writes bytes to this buffer using the buffers putInt method,
     * and finally flushes everything to disk, and closes
     * @param n
     * @param alpha
     */
    public void GenerateData(int n, double alpha, String filename){
        //System.out.println("Beginning Data generation...");
        file = new File(filename);
        
        if (file.exists()) {
        	file.delete();
        }

        long bytesNeeded  = (n*4*2*3);//how large to make the buffer
        try{
            this.fc = new RandomAccessFile(this.file, "rw").getChannel();
            this.buffer = fc.map(FileChannel.MapMode.READ_WRITE,0,bytesNeeded);
            //this.buffer.putInt(0);//byte alignment
            //this.buffer.putInt(n);//the first int will be the number of vertices
        }
        catch(IOException e){
            System.out.println("IO Problem " + e.getMessage());
        }
        int span = 0;
        int origin = 0;
        for(int i=1; i <= 3*n; i++){
            span = (int) Math.ceil((1 - Math.pow(Math.random(), alpha)) * (n - 1));
            origin = (int) Math.floor(Math.random() * (n - span));
            this.newEdge(origin,origin+span);
        }
        //Done with generating and writing data, close everything.
        //System.out.println("Completed Data generation.");
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
            //System.out.println(Integer.toString(i) + " " + Integer.toString(j));
            assert((i != 0)&&(j !=0));
            this.buffer.putInt(i);
            this.buffer.putInt(j);
        }

    }

}
