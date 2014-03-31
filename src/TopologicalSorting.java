import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * Implements both DFS and BFS topological sorting
 * test
 */
public class TopologicalSorting {
    /**
     * Non I/O efficient implementation of topological sorting, used only for
     * comparisons with the I/O efficient
     * 
     * @param G: Object representation of the graph
     * @return
     */
	public static ArrayList<Vertex> TopologicalSortBFS(Graph G) {
		int N = G.getSize();
		Queue<Vertex> Q = new LinkedList<Vertex>();
		
		// Resulting list of vertices in topological sorting
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

	/**
	 * I/O efficient implementation of topological sorting, based on the same approach as 
	 * above version. Used MappedByteBuffers on top of random access files to manage the
	 * paging of arrays between memory and disk.
	 * 
	 * @param vertices: Original vertices in the same order as the input
	 * @param N: Number of vertices
	 * @return Graph with the vertices in topological order, and edges also ordered
	 * the same as the vertices.
	 */
	public static IOGraph IOTopologicalSortBFS(IOVertexBuffer vertices, int N) throws Exception{
    	
		// Buffer that will contain the indegree for each vertex allocated with N integers
		File indegreeFile = new File("indegree.tmp");
    	RandomAccessFile RAFile = new RandomAccessFile(indegreeFile,"rw");
    	FileChannel indegreeFileChannel = RAFile.getChannel();
    	MappedByteBuffer indegreeBuffer = indegreeFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * N);
    	
    	
    	// Calculate the indegree for each vertex from the destination-sorted edge list
    	RandomAccessFile destRAFile = new RandomAccessFile(new File("destSorted" + N + ".dat"),"rw");
    	FileChannel destFileChannel = destRAFile.getChannel();
    	MappedByteBuffer destBuffer = destFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * 2 * 3 * N);
    	
    	/*destBuffer.position(0);
        while (destBuffer.hasRemaining())
        	System.out.println(destBuffer.getInt() + ", " + destBuffer.getInt());
        destBuffer.position(0);*/

        /* this loop calculates for each vertex how many edges arrive at it*/
    	indegreeBuffer.position(0);
        for (int i = 0; i < N; ++i)
        	indegreeBuffer.putInt(0);
        
    	int prev = -1;
    	// If there are repeated edges, don't count them twice
    	HashSet<Integer> seen = new HashSet<Integer>();
    	int maxIndegree = 0; // Just to see what the max indegree is
        while (destBuffer.hasRemaining()) { //for each vertex
        	int u = destBuffer.getInt();
        	int v = destBuffer.getInt();
        	
        	if (v != prev)
        		seen.clear();
    		if (!seen.contains(u)) {
	            int d = indegreeBuffer.getInt(4 * v);
	            indegreeBuffer.putInt(4 * v, d + 1);
	            seen.add(u);
	            maxIndegree = Math.max(maxIndegree, d + 1);
    		}
            
            prev = v;
        }
        //System.out.println("max indegree: " + maxIndegree);
        
        /*indegreeBuffer.position(0);
        for (int i = 0; i < N; ++i)
        	System.out.println(indegreeBuffer.getInt());
        indegreeBuffer.position(0);*/

        destFileChannel.close();
        destRAFile.close();
        
        // Create the graph representation from the origin-sorted edge list
        RandomAccessFile originRAFile = new RandomAccessFile(new File("originSorted" + N + ".dat"),"rw");
    	FileChannel originFileChannel = originRAFile.getChannel();
    	// 4 bytes * <origin, destination> * (3N)
    	MappedByteBuffer originBuffer = originFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * 2 * 3 * N);
    	
    	/*originBuffer.position(0);
        while (originBuffer.hasRemaining())
        	System.out.println(originBuffer.getInt() + ", " + originBuffer.getInt());
        originBuffer.position(0);*/
    	
    	
    	// Create the edges buffer based on the origin-sorted edge list
    	IOEdgesBuffer edges = new IOEdgesBuffer(N, "edges1.dat");
    	
    	int pointer = 0;
    	prev = -1;
    	// If there are repeated edges, don't store them twice
    	seen = new HashSet<Integer>();
    	while (originBuffer.hasRemaining()) {
    		int u = originBuffer.getInt();
        	int v = originBuffer.getInt();
        	
        	// If this is the first edge of current vertex...
        	if (prev != u) {
        		if (prev >= 0) {
        			// If a vertex doesn't have outgoing edges, put NULL in edge list
        			for (int j = prev; j < u; ++j) {
        				edges.addEdge(-1);
        				++pointer;
        			}
        		}
        		// Set the pointer to the index of first edge in the edges buffer for the current vertex
        		vertices.setEdgesAt(u, pointer);
        		seen.clear();
        	}
        	// Add edge if it doesn't exist yet
        	if (!seen.contains(v)) {
        		seen.add(v);
        		edges.addEdge(v);
        		++pointer;
        	}
        	
        	prev = u;
    	}
    	// If the last vertices don't have outgoing edges, add NULLs to edge buffer
    	for (int j = prev; j < N; ++j) {
    		edges.addEdge(-1);
    		++pointer;
    	}
    	
    	//System.out.println(vertices);
    	//System.out.println(edges);
    	
    	originFileChannel.close();
        originRAFile.close();
    	
        // Run the actual topological sorting algorithm
        IOVertexBuffer sortedVertices = new IOVertexBuffer(N, N+"vertices2.dat");
        IOEdgesBuffer sortedEdges = new IOEdgesBuffer(N, N+"edges2.dat");
        
        Queue<Integer> Q = new LinkedList<Integer>();
                
        /*this loop gathers all the vertices which have 0 arriving edges to it and puts it in a queue*/
        for (int i = 0; i < N; ++i) {
            int d = indegreeBuffer.getInt(4 * i);
            if (d == 0)
            	Q.offer(i);
        }

        int time = 0;// Index of vertex in topological sorting
        pointer = 0;// Pointer to the current position in edge buffer
        while (!Q.isEmpty()) {
            int uid = Q.poll();
            IOVertex u = vertices.getVertexAt(uid);
            // Store the new position of this vertex in the topological sorting,
            // to map later the old id to the new one
            vertices.setTimeAt(uid, time);
            IOVertex v = new IOVertex(time, time, u.getX(), u.getY(), pointer);
            ++time;
            
            sortedVertices.addVertex(v);
            for (int e = u.getEdges(), to = 0; e >= 0 && (to = edges.getEdge(e)) != -1; ++e) {
            	IOVertex w = vertices.getVertexAt(to);
                int d = indegreeBuffer.getInt(4 * w.getId());
                indegreeBuffer.putInt(4 * w.getId(), --d);
                
                if (d == 0)
                    Q.offer(w.getId());
                
                sortedEdges.addEdge(to);
                ++pointer;
            }
            sortedEdges.addEdge(-1);
            ++pointer;
        }
        
        // Change ids of the vertices in the edge buffer, according to
        // the new order in the topological sorting.
        MappedByteBuffer edgesTmp = sortedEdges.edgesBuffer;
        edgesTmp.position(0);
        int ind = 0;
        while (edgesTmp.hasRemaining()) {
        	int to = edgesTmp.getInt();
        	++ind;
        	if (to < 0 || to >= N) continue;
        	int newid = vertices.getVertexAt(to).getTime();
        	edgesTmp.putInt(4 * (ind - 1), newid);
        }

        indegreeFileChannel.close();
        RAFile.close();
        
        if (indegreeFile.exists())
        	indegreeFile.delete();

        return new IOGraph(N, sortedVertices, sortedEdges);
    }
}
