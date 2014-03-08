import java.util.*;
import java.io.*;

public class Main {
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(new File("test.txt"));
		
		int N = in.nextInt();
		Graph G = new Graph(N);
		while (in.hasNextInt()) {
			int u = in.nextInt();
			int v = in.nextInt();
			
			G.addEdge(u, v);
		}
		
		System.out.println(TopologicalSorting.TopologicalSortBFS(G));
		System.out.println(Arrays.toString(LongestPath.LongestPathDP(G)));
		System.out.println(Arrays.toString(LongestPath.LongestPathTimeForward(G, 2)));
		
		in.close();
		System.exit(0);
	}
}
