import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        /*Generate Data*/
        // 80000000 is about as big as this implementation can handle;
        int n = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]); //used in the TFP alg for the size of each period

        double alpha = 0.5;
        DataGenerator dg = new DataGenerator();
        dg.GenerateData(n,alpha);

        File edgesFile = new File("edgeData"+ n + ".dat");
        //File edgesFile = new File(args[2]);
        System.out.println("Beginning sort by Origin");
        IOSort originSorter = new IOSort(edgesFile, n, "originSorted");
        originSorter.sortSegments();
        originSorter.mergeSort();
        System.out.println("Beginning sort by Dest");
        SortByDestination destSorter = new SortByDestination(n);
        destSorter.sort(edgesFile);



        IOVertexBuffer IOVBuf = new IOVertexBuffer(n,"edges1.dat");
        IOVertexBuffer vertices = new IOVertexBuffer(n, "vertices1.dat");
        for (int i = 0; i < n; ++i)
            vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
        long startTime = System.currentTimeMillis();
        IOGraph G = TopologicalSorting.IOTopologicalSortBFS(vertices,n);
        System.out.print("TopoSorting: " + String.valueOf(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        LongestPath.IOLongestPathTimeForward(G,m);
        System.out.print("TFP: " + String.valueOf(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        LongestPath.IOLongestPathDP(G);
        System.out.println(", DP: " + String.valueOf(System.currentTimeMillis() - startTime));


        //convertTXTtoBytes("test3Mregular-edges-OriginSorted");
        //convertTXTtoBytes("test3Mregular-edges-DestSorted");
        //convertTXTtoBytes("test10Mregular-edges-OriginSorted");
        //convertTXTtoBytes("test10Mregular-edges-DestSorted");
        //-convertTXTtoBytes("test30Mregular-edges-OriginSorted");
        //-convertTXTtoBytes("test30Mregular-edges-DestSorted");
        //convertTXTtoBytes("test50Mregular-edges-OriginSorted");
        //convertTXTtoBytes("test50Mregular-edges-DestSorted");

        //printData();
        System.exit(0);
	}

    public static void printData(int n, String filenamepart) throws IOException{
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart +".dat","r");
        FileChannel fc = in.getChannel();
        int i = 0;
        //MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
        ByteBuffer mbb = ByteBuffer.allocateDirect((int) fc.size());
        fc.read(mbb);
        mbb.position(0);
        while(mbb.hasRemaining()){

            System.out.println(i++ + ": " + mbb.getInt() + ", " + mbb.getInt());
        }
        System.out.println("-----------");
    }

    public static void convertTXTtoBytes(String originFile) throws Exception{
        RandomAccessFile in = new RandomAccessFile(new File(originFile),"rw");
        RandomAccessFile out = new RandomAccessFile(new File(originFile + ".dat"), "rw");

        try {
            while (true) {
                String[] line = in.readLine().split(" ");
                out.writeInt(Integer.parseInt(line[0]));
                out.writeInt(Integer.parseInt(line[1]));
            }
        }catch(Exception e){
            System.out.println("Done");
        }





    }
}
