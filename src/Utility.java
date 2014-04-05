 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.MappedByteBuffer;
 import java.util.*;
 import java.io.File;

public class Utility {
    public static void main(String[] args) throws Exception {
        //convertEdgestoBytes(args[0]);
        printData(Integer.parseInt(args[0]),args[1]);
        //convertPointstoBytes(args[0]);
        //printVertices(Integer.parseInt(args[0]),args[1]);
        //printDataX(Integer.parseInt(args[0]),args[1]);
        //testBuffers();
    }

    public static void testBuffers() throws Exception{
        //IOVertexBuffer vertices = new IOVertexBuffer(500,"testverts");
        SuperArray s = new SuperArray(10);
        s.putInt(4);
        s.putInt(14);
        s.putInt(24);
        s.putInt(34);
        s.putInt(44);
        s.putInt(45);
        s.putInt(41);
        s.putInt(43);
        s.putInt(43);
        s.putInt(1);
        s.putInt(0,99);
        s.putInt(9,88);
        System.out.println(s);


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
        int i = 0;
        while(mbb.hasRemaining()){

            System.out.println(++i + ": " + mbb.getInt() + ", " + mbb.getInt());
        }
        System.out.println("-----------");
    }
    public static void printDataX(int n, String filenamepart) throws IOException {
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart,"rw");
        FileChannel fc = in.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());
        while(mbb.hasRemaining()){

            System.out.println(mbb.getInt());
        }
        System.out.println("-----------");
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
        System.out.println("-----------");
    }
}
