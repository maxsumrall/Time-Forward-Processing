import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
public class Main {
	public static void main(String[] args) throws IOException {
        //int n = 40;
        //double alpha = 0.5;
        //DataGenerator dg = new DataGenerator();
        //dg.GenerateData(n,alpha);
        try{
            IOVersion();
        }
        catch (Exception e){e.printStackTrace(); }
		/*Scanner in = new Scanner(new File("edges.txt"));
		
		int N = in.nextInt();
		Graph G = new Graph(N);
		while (in.hasNextInt()) {
			int u = in.nextInt();
			int v = in.nextInt();
			
			G.addEdge(u, v);
		}

        DataGenerator dg = new DataGenerator();
        dg.GenerateData(40,0.5);
		
		System.out.println(TopologicalSorting.TopologicalSortBFS(G));
		System.out.println(Arrays.toString(LongestPath.LongestPathDP(G)));
		System.out.println(Arrays.toString(LongestPath.LongestPathTimeForward(G, 2)));
		
		in.close();
		*/
		System.exit(0);
	}
    public static void IOVersion() throws Exception{
        File edgesFile = new File("edgeData40.dat");
        IOSort sorter = new IOSort(edgesFile);
        sorter.sortSegments();
        sorter.mergeSort();


    }
}
