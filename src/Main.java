import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        /*Generate Data*/
        // 80000000 is about as big as this implementation can handle;
        /*
        int n = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]); //used in the TFP alg for the size of each period
        double alpha = 0.5;
        DataGenerator dg = new DataGenerator();
        dg.GenerateData(n,alpha);

        File edgesFile = new File("edgeData"+ n + ".dat");
        //File edgesFile = new File("outFileEdgesBytes.dat"); //for testing the given test files
        //System.out.println("Beginning sort by Origin");
        IOSort originSorter = new IOSort(edgesFile, n, "originSorted");
        originSorter.sortSegments();
        originSorter.mergeSort();

        //printData(n, "originSorted");

        //System.out.println("Beginning sort by Dest");
        SortByDestination destSorter = new SortByDestination(n);
        destSorter.sort(edgesFile);


        IOVertexBuffer IOVBuf = new IOVertexBuffer(n,"edges1.dat");
        IOVertexBuffer vertices = new IOVertexBuffer(n, "vertices1.dat");
        for (int i = 0; i < n; ++i)
            vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
        IOGraph G = TopologicalSorting.IOTopologicalSortBFS(vertices,n);
        long startTime = System.currentTimeMillis();

        LongestPath.IOLongestPathTimeForward(G,m);

         System.out.print("TFP: " + String.valueOf(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        LongestPath.IOLongestPathDP(G);
        System.out.println(", DP: " + String.valueOf(System.currentTimeMillis() - startTime));


        //convertTXTtoBytes(new File("../../../randomgraphs/test10Mregular-edges"))

        */

        convertTXTtoBytes("test25regular-edges");
        convertTXTtoBytes("test500regular-edges");
        convertTXTtoBytes("test3000regular-edges");
        convertTXTtoBytes("test10Kregular-edges");
        convertTXTtoBytes("test200Kregular-edges");
        convertTXTtoBytes("test3Mregular-edges");
        convertTXTtoBytes("test10Mregular-edges");
        convertTXTtoBytes("test30Mregular-edges");
        System.out.println("123");
        convertTXTtoBytes("test50Mregular-edges");
        System.out.println("Done with edges");
        convertTXTtoBytes("test25regular-point");
        convertTXTtoBytes("test500regular-points");
        convertTXTtoBytes("test3000regular-points");
        convertTXTtoBytes("test10Kregular-points");
        convertTXTtoBytes("test200Kregular-points");
        convertTXTtoBytes("test3Mregular-points");
        convertTXTtoBytes("test10Mregular-points");
        System.out.println("456");
        convertTXTtoBytes("test30Mregular-points");
        convertTXTtoBytes("test50Mregular-points");
		System.exit(0);
	}

    public static void printData(int n, String filenamepart) throws IOException{
        //RandomAccessFile in = new RandomAccessFile(filenamepart + n + ".dat","r");
        RandomAccessFile in = new RandomAccessFile(filenamepart +".dat","r");
        FileChannel fc = in.getChannel();
        int i = 0;
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY,0,fc.size());
        while(mbb.hasRemaining()){

            System.out.println(i++ + ": " + mbb.getInt() + ", " + mbb.getInt());
        }
        System.out.println("-----------");
    }

    public static void convertTXTtoBytes(String originFile) throws Exception{
        Scanner in = new Scanner(new File("../../../randomgraphs/" + originFile));
        RandomAccessFile out = new RandomAccessFile(new File("../../randomgraphsMine/" + originFile + ".dat"), "rw");
        while(in.hasNextInt()){
            out.writeInt(in.nextInt());
        }
    }
}
