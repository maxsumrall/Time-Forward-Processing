import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Utility {
    public static void main(String[] args) throws Exception {
    	String opt = args[0].toLowerCase();
    	
    	switch (opt) {
    	case "ceb":
    		convertEdgestoBytes(args[1]);
    		break;
    	case "pd":
    		printData(Integer.parseInt(args[1]),args[2]);
    		break;
    	case "cpb":
    		convertPointstoBytes(args[1]);
    		break;
    	case "pv":
    		printVertices(Integer.parseInt(args[1]),args[2]);
    		break;
    	case "pdx":
    		printDataX(Integer.parseInt(args[1]),args[2]);
    		break;
    		//testBuffers();
    	case "pe":
    		printEdgesWith2Vals(Integer.parseInt(args[1]),args[2]);
    		break;
    	}
    }




    public static void convertEdgestoBytes(String originFile) throws Exception {
        RandomAccessFile in = new RandomAccessFile(new File(originFile), "rw");
        RandomAccessFile out = new RandomAccessFile(new File(originFile + "_min1"), "rw");

        try {
            while (true) {
                String[] line = in.readLine().split(" ");
                int i =  Integer.parseInt(line[0]) - 1;
                int j =  Integer.parseInt(line[1]) - 1;
                out.writeInt(j);
                out.writeInt(i);
            }
        } catch (Exception e) {
            System.out.println("Done");
        }
    }

    public static void convertPointstoBytes(String originFile) throws Exception {
        RandomAccessFile in = new RandomAccessFile(new File(originFile), "rw");
        RandomAccessFile out = new RandomAccessFile(new File(originFile + "_min1"), "rw");

        try {
            while (true) {
                String[] line = in.readLine().split(" ");
                out.writeInt(Integer.parseInt(line[0]) - 1);
                out.writeInt(Integer.parseInt(line[1]));
                out.writeInt(Integer.parseInt(line[2]));

            }
        } catch (Exception e) {
            System.out.println("Done");
        }
    }
    public static void printData(int n, String filenamepart) throws IOException {
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart,"rw");
        FileChannel fc = in.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());
        int i = mbb.remaining();
        for(int j = 0; j < i; j+=4){
            System.out.println(j/4 + ": " + mbb.getInt());
        }
        //System.out.println("-----------");
    }
    public static void printDataX(int n, String filenamepart) throws IOException {
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart,"rw");
        FileChannel fc = in.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());
        while(mbb.hasRemaining()){

            System.out.println(mbb.getInt());
        }
        //System.out.println("-----------");
    }


    public static void printVertices(int n, String filenamepart) throws IOException {
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart,"rw");
        FileChannel fc = in.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());
        int i = 0;
        while(mbb.hasRemaining()){
            String outline = "";
            System.out.print(mbb.getInt() + " ");
            mbb.getInt();
            System.out.print(mbb.getInt() + " ");
            System.out.print(mbb.getInt() + "\n");
            mbb.getInt();
            //System.out.println(mbb.getInt() + " " + mbb.getInt() + mbb.getInt() + mbb.getInt() + mbb.getInt());
        }
        //System.out.println("-----------");
    }


    public static void printEdgesWith2Vals(int n, String filenamepart) throws IOException {
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart,"rw");
        FileChannel fc = in.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());
        while(mbb.hasRemaining()){
            System.out.print(mbb.getInt() + " ");
            System.out.print(mbb.getInt() + "\n");
        }
        //System.out.println("-----------");
    }




}
