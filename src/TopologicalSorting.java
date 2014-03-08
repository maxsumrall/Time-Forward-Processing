import java.util.*;

/**
 * Implements both DFS and BFS topological sorting
 * test
 */
public class TopologicalSorting {
	private static boolean[] seen;
	private static ArrayList<Vertex> topsort;
	
	public static ArrayList<Vertex> TopologicalSortDFS(Graph G) {
		topsort = new ArrayList<Vertex>();
		int N = G.getSize();
		seen = new boolean[N];
		
		for (int i = 0; i < N; ++i) {
			if (!seen[i]) {
				Stack<Vertex> S = new Stack<Vertex>();
				S.push(G.getVertexAt(i));
				while (!S.isEmpty()) {
					Vertex v = S.pop();
					topsort.add(v);
					seen[v.getId()] = true;
					for (Edge edge : v.getEdges()) {
						Vertex w = edge.getTo();
						if (!seen[w.getId()])
							S.push(w);
					}
				}
			}
		}
		
		return topsort;
	}
	
	public static ArrayList<Vertex> TopologicalSortBFS(Graph G) {
		int N = G.getSize();
		Queue<Vertex> Q = new LinkedList<Vertex>();
		ArrayList<Vertex> topsort = new ArrayList<Vertex>();
		int[] inDegree = new int[N];
		
		for (int i = 0; i < N; ++i)
			for (Edge e : G.getVertexAt(i).getEdges())
				++inDegree[e.getTo().getId()];
		
		for (int i = 0; i < N; ++i)
			if (inDegree[i] == 0)
				Q.offer(G.getVertexAt(i));
		
		int time = 0;
		while (!Q.isEmpty()) {
			Vertex v = Q.poll();
			v.setTime(time++);
			topsort.add(v);
			for (Edge e : v.getEdges()) {
				Vertex w = e.getTo();
				--inDegree[w.getId()];
				if (inDegree[w.getId()] == 0)
					Q.offer(w);
			}
		}
		
		return topsort;
	}
}
