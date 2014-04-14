/**
 * Compare speed of Unsafe arrays compared to ByteBuffers
 */
import java.io.File;
import java.nio.ByteBuffer;

public class Benchmark{

    public static void main(String[] args) throws Exception{
        long n = 50000000; //50 Million



        /*long time = System.currentTimeMillis();
        SuperArray sa = new SuperArray(n);
        for(int i = 0; i < n; i++){
            sa.putInt((int)Math.random()*100);
        }
        for(int i = 0; i < n; i++){
            sa.getInt();
        }
        System.out.println("SuperArray: " + (System.currentTimeMillis() - time));
        */

        long time = System.currentTimeMillis();
        //File file = new File("trashFile.deleteMe");
        //MappedFileBuffer buff = new MappedFileBuffer(file,50000000,true,n);
        ByteBuffer buff = ByteBuffer.allocateDirect((int)n*4);
        for(int i = 0; i < n; i++){
            buff.putInt((int)Math.random()*100);
        }
        for(int i = 0; i< n; i++){
            buff.getInt(i);
        }
        System.out.println("MappedFileBuffer: " + (System.currentTimeMillis() - time));

    }
}
