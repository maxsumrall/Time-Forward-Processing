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

    /**
     * -----------------Does this really put the items in order in memory or does it swap pointers
     * @param G
     * @return
     */
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
			topsort.add(v.clone());//Clone So that java does not just do pointer changes
			for (Edge e : v.getEdges()) {
				Vertex w = G.getVertexAt(e.getID());//e.getTo();
				--inDegree[w.getId()];
				if (inDegree[w.getId()] == 0)
					Q.offer(w);
			}
		}
		
		return topsort;
	}
    /*
    * I'm thinking we can model the queue used here as a buffer easily.
    * We can make a class or function to handle it and write our own get() and put() methods.
    * We would make a big buffer, the size of the number of vertices, but its not going to be all used.
    * Its going to "waste" a lot of space on the disk, but disk space is cheap and this file would be deleted afterwards anyways.
    * How it works:
    * We have two pointers/markers for the buffer: the first for the next read position and the second for the next write position
    * When you want to read the next vertex, move the read pointer of the buffer to this first pointer and read the bytes there, and increment our pointer.
    * When you want to write another vertex, more the pointer of the buffer to the second pointer and start writing, and update out second pointer.
    * */
    public static ArrayList<Vertex> TopologicalSortBFS_IO(Graph G) throws Exception{
        int N = G.getSize();
        //Queue<Vertex> Q = new LinkedList<Vertex>();
        IOQueue Q = new IOQueue(N);
        ArrayList<Vertex> topsort = new ArrayList<Vertex>();
        int[] inDegree = new int[N];

        /* this loop calculates for each vertex how many edges arrive at it*/
        for (int i = 0; i < N; ++i) //for each vertex
            for (Edge e : G.getVertexAt(i).getEdges()) //for each edge at this vertex
                ++inDegree[e.getTo().getId()]; //increment the in-degree of the receiving vertex at this edge

        /*this loop gathers all the vertices which have 0 arriving edges to it and puts it in a queue*/
        for (int i = 0; i < N; ++i)
            if (inDegree[i] == 0)
                Q.offer(G.getVertexAt(i));

        int time = 0;
        while (!Q.isEmpty()) {
            Vertex v = Q.poll();
            v.setTime(time++);
            topsort.add(v.clone());//Clone So that java does not just do pointer changes
            for (Edge e : v.getEdges()) {
                Vertex w = G.getVertexAt(e.getID());//e.getTo();
                --inDegree[w.getId()];
                if (inDegree[w.getId()] == 0)
                    Q.offer(w);
            }
        }

        return topsort;
    }
}
