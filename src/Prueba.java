import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class Prueba {

   public static void main(String[] args) throws Exception{
       String filename = "test500";
       
       String[] in = {"origin", "dest"};
       String[] out = {"OriginSorted", "DestSorted"};
       
       for (int i = 0; i < 2; ++i) {
    	   File file = new File(filename + "." + in[i]);
    	   Scanner infile = new Scanner(file);
		   RandomAccessFile raf = new RandomAccessFile(filename + "." + out[i],"rw");
	       FileChannel fc = raf.getChannel();
	       MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,4 * 2 * 1420);

	       while (infile.hasNextInt()) {
	    	   int x = infile.nextInt() - 1;
	    	   mbb.putInt(x);
	       }
	       
	       fc.close();
	       raf.close();
	       infile.close();
       }
       
       for (int i = 0; i < 2; ++i) {
    	   RandomAccessFile raf = new RandomAccessFile(filename + "." + out[i],"rw");
	       FileChannel fc = raf.getChannel();
	       MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,4 * 2 * 1420);
	       
	       System.out.println(out[i]);
	       //mbb.position(0);
	       while (mbb.hasRemaining()) {
	    	   System.out.println(mbb.getInt() + " " + mbb.getInt());
	       }
	       
	       fc.close();
	       raf.close();
       }
   }
}
