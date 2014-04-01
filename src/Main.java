import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Main {
    /**
     *
     * @param args
     * @throws Exception As much as this code had grown, this could be anything
     * options: source of data: generate new data or use existing data
     *          if generating new data, generate it and sort it and be done
     *          if using existing data, give me next the name, and what to do to it: which alg to use or to run sorting on it
     *  java Main -n N
     *  java Main "filename" N topoSort
     *  java Main "filename" N DP
     *  java Main "filename" N TFP/TFPExperimental M
     *                                             ^ NOTICE THE M
     *                                            ^^^
     *                                          ~^^^^^ ~
     */
    public static void main(String[] args) throws Exception{
        int n = 0;
        n = Integer.parseInt(args[1]);
        //Make file names comprehensible
        String fileName = determineFileName(n);

        /**
         * Generate New Random Data and Sort it!  Then we quit!
         */
        if (args[0].equals("-n")){
            generateAndSort(n, fileName);
        }
        /**
         * Use already made data!
         * So we can assume there exists the topologically sorted data already there
         */
        else{
            String readFileName = args[0];
            System.out.println(readFileName);
            /**
             * Do topological sorting. Should only be done to NON-RANDOM a.k.a test data
             */
            if(args[2].equals("topoSort")){
                IOEdgesBuffer edges = new IOEdgesBuffer(n, readFileName+".TempEdges");
                IOVertexBuffer vertices = new IOVertexBuffer(n, readFileName+".TempVertices");
                for (int i = 0; i < n; ++i)
                    vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
                long startTime = System.currentTimeMillis();
                IOGraph G = TopologicalSorting.IOTopologicalSortBFS(vertices,edges, n, fileName);
                System.out.println("Topological Sorting RunTime: " + String.valueOf(System.currentTimeMillis() - startTime) + " m/s");
                edges.delete();
                vertices.delete();
            }

            /**
             * Assume topological sorting has been done!
             */
            else{
                int M = Integer.parseInt(args[3]);
                n = Integer.parseInt(args[1]);

                IOEdgesBuffer edges = new IOEdgesBuffer(n, readFileName + ".TopoEdges");
                IOVertexBuffer vertices = new IOVertexBuffer(n, readFileName + ".TopoVertices");
                IOGraph G = new IOGraph(n, vertices, edges);
                /**
                 * Do DP algorithm!
                 */
                if(args[2].equals("DP")){
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathDP(G);
                    System.out.println("DP: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Do TFP algorithm!
                 */
                else if (args[2].equals("TFP")){
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathTimeForward(G,M);
                    System.out.println("TFP: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Do Experimental TFP, the TFP with some changes to make it fast!
                 */
                else if (args[2].equals("TFP_Exp")){
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathTimeForwardExperiment(G,M);
                    System.out.println("TFPexperiment: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Do Water Flow Calculation!
                 */
                else if (args[2].equals("WaterFlow")){
                    long startTime = System.currentTimeMillis();
                    LongestPath.waterflowTFPIO(G,M);
                    System.out.println("TFPexperiment: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
            }
        }

    }



//    public static void main(String[] args) throws Exception {
//        /*Generate Data*/
//        // 80000000 is about as big as this implementation can handle;
//        int n = Integer.parseInt(args[0]);
//        int m = Integer.parseInt(args[1]); //used in the TFP alg for the size of each period
//
//        IOGraph G;
//        if (args[2].equals("a")) {
//
//            generateAndSort(n); //
//
//            File edgesFile = new File("edgeData" + n + ".dat");
//            //File edgesFile = new File(args[2]);
//
//
//            //re-do topo sort
//            IOEdgesBuffer edges = new IOEdgesBuffer(n, "edges1.dat");
//            IOVertexBuffer vertices = new IOVertexBuffer(n, "vertices1.dat");
//            for (int i = 0; i < n; ++i)
//                vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
//
//            long startTime = System.currentTimeMillis();
//            G = TopologicalSorting.IOTopologicalSortBFS(vertices, n);
//            System.out.println("TopoSorting: " + String.valueOf(System.currentTimeMillis() - startTime));
//        }
//        else if(args[2].equals("b")){
//            //reuse toposorted lists
//            IOEdgesBuffer edges = new IOEdgesBuffer(n, n + "edges2.dat");
//            IOVertexBuffer vertices = new IOVertexBuffer(n, n + "vertices2.dat");
//            G = new IOGraph(n, vertices, edges);
//
//        /*
//        startTime = System.currentTimeMillis();
//        LongestPath.IOLongestPathTimeForward(G,m);
//        System.out.println("TFP: " + String.valueOf(System.currentTimeMillis() - startTime));
//        */
//
//        //long startTime = System.currentTimeMillis();
//        //LongestPath.IOLongestPathDP(G);
//        //System.out.println("DP: " + String.valueOf(System.currentTimeMillis() - startTime));
//
//        //startTime = System.currentTimeMillis();
//        //LongestPath.IOLongestPathDPUnsafe(G);
//        //System.out.println("DPunsafe: " + String.valueOf(System.currentTimeMillis() - startTime));
//
//
//        //long startTime = System.currentTimeMillis();
//        //LongestPath.IOLongestPathTimeForwardExperiment(G,m);
//        //System.out.println("TFPexperiment: " + String.valueOf(System.currentTimeMillis() - startTime));
//
//        LongestPath.waterflowTFPIO(G,m);
//
//        }
//        /**
//         * Test data, NOT random data
//         */
//        else if(args[2].equals("test")){
//            IOVertexBuffer vertices = new IOVertexBuffer(n, n + "vertices2Test.dat");
//            G = TopologicalSorting.IOTopologicalSortBFS(vertices, n);
//            IOEdgesBuffer edges = new IOEdgesBuffer(n, n + "edges2Test.dat");
//            IOVertexBuffer vertices = new IOVertexBuffer(n, n + "vertices2Test.dat");
//            G = new IOGraph(n, vertices, edges);
//
//        }
//
//        else{System.out.println("missing arg 3: a or b");System.exit(0);}
//
//        System.exit(0);
//	}

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

    public static void generateAndSort(int n, String fileName) throws Exception{
        File edgesFile = new File(fileName);

        double alpha = 0.5;
        DataGenerator dg = new DataGenerator();
        dg.GenerateData(n,alpha,fileName);
        System.out.println("Sorting New Edges by Origin");
        IOSort originSorter = new IOSort(edgesFile, n, fileName + ".OriginSorted");
        originSorter.sortSegments();
        originSorter.mergeSort();
        System.out.println("Sorting New Edges by Destination");
        SortByDestination destSorter = new SortByDestination(n);
        destSorter.sort(edgesFile, fileName);
    }
    public static String determineFileName(int n){
        if (n > 1000000){return "randomEdges"+ (n/1000000) + "M";}
        return "randomEdges"+n;
    }
}
