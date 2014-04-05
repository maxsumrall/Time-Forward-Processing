import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Main {
    /**
     *
     * @param args
     * @throws Exception As much as this code had grown, this could be anything
     * options: source of data: generate new data or use existing data
     *          if generating new data, generate it and sort it and be done
     *          if using existing data, give me next the name, and what to do to it: which alg to use or to run sorting on it
     *
     *     Examples
     *  java Main -n N   //create new random data with N vertices. DOES NOT TOPOSORT
     *  java Main "filename" N topoSort   // execute toposort alg on "fileName" with N vertices
     *  java Main "filename" N DP         // execute DP alg on "filename" with N vertices
     *  java Main "filename" N TFP/TFP_Exp M
     *                                      ^ NOTICE THE M
     *                                     ^^^
     *                                   ~^^^^^ ~
     */
    public static void main(String[] args) throws Exception{
        int n = 0;
        int M = 0;
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
                long startTime = System.currentTimeMillis();
                IOGraph G = TopologicalSorting.IOTopologicalSortBFS(n, readFileName);
                System.out.println("Topological Sorting RunTime: " + String.valueOf(System.currentTimeMillis() - startTime) + " m/s");
            }
            /*else if(args[2].equals("topoSortWithXY")){
                IOEdgesBuffer edges = new IOEdgesBuffer(n, readFileName+".TempEdges");
                IOVertexBuffer vertices = new IOVertexBuffer(n, readFileName+".TempVertices");
                File verticesFile = new File("test3000regular-points_min1"); // CHECK FILE NAMES!!!!!

                RandomAccessFile raf = new RandomAccessFile(verticesFile,"rw");
                FileChannel verticesFileChannel = raf.getChannel();
                MappedByteBuffer verticesBuffer = verticesFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4 * 3 * n);

                for (int i = 0; i < n; ++i) {
                    int id = verticesBuffer.getInt();
                    int time = id;
                    int x = verticesBuffer.getInt();
                    int y = verticesBuffer.getInt();
                    vertices.addVertex(new IOVertex(id, time, x, y, -1));
                }

                verticesFileChannel.close();
                raf.close();

                long startTime = System.currentTimeMillis();
                IOGraph G = TopologicalSorting.IOTopologicalSortBFS(vertices,edges, n, readFileName);
                System.out.println("Topological Sorting RunTime: " + String.valueOf(System.currentTimeMillis() - startTime) + " m/s");
                edges.delete();
                vertices.delete();
            }*/


            /**
             * Assume topological sorting has been done!
             */
            else{
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
                else if(args[2].equals("DPUnsafe")){
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathDPUnsafe(G);
                    System.out.println("DPUnsafe: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Do TFP algorithm!
                 */
                else if (args[2].equals("TFP")){
                    M = Integer.parseInt(args[3]);
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathTimeForward(G,M);
                    System.out.println("TFP: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Do Experimental TFP, the TFP with some changes to make it fast!
                 */
                else if (args[2].equals("TFP_Exp")){
                    M = Integer.parseInt(args[3]);
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathTimeForwardExperiment(G,M);
                    System.out.println("TFPexperiment: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Not using vertices
                 */
                else if (args[2].equals("TFP_NV")){
                    M = Integer.parseInt(args[3]);
                    long startTime = System.currentTimeMillis();
                    LongestPath.IOLongestPathTimeForwardNoVertices(G,M);
                    System.out.println("TFPNoV: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                /**
                 * Do Water Flow Calculation!
                 */
                else if (args[2].equals("WaterFlow")){
                    M = Integer.parseInt(args[3]);
                    long startTime = System.currentTimeMillis();
                    LongestPath.waterflowTFPIO(G,M);
                    System.out.println("Waterflow: " + String.valueOf(System.currentTimeMillis() - startTime));

                }
                else if (args[2].equals("sort")){
                    sort(n, readFileName);
                }
                else{
                    System.out.println("No algorithm selected");
                }
            }
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
    public static void sort(int n, String readFileName) throws Exception{
        File edgesFile = new File(readFileName);
        System.out.println("Sorting New Edges by Origin");
        IOSort originSorter = new IOSort(edgesFile, n, readFileName + ".OriginSorted");
        originSorter.sortSegments();
        originSorter.mergeSort();
        System.out.println("Sorting New Edges by Destination");
        SortByDestination destSorter = new SortByDestination(n);
        destSorter.sort(edgesFile, readFileName);
    }
    public static String determineFileName(int n){
        if (n > 1000000){return "randomEdges"+ (n/1000000) + "M";}
        return "randomEdges"+n;
    }
}
