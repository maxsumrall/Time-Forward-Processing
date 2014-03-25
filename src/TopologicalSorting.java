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

	public static IOGraph IOTopologicalSortBFS(IOVertexBuffer vertices, int N) throws Exception{
    	
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
    	HashSet<Integer> seen = new HashSet<Integer>();
    	int maxIndegree = 0;
        while (destBuffer.hasRemaining()) { //for each vertex
        	int u = destBuffer.getInt();
        	int v = destBuffer.getInt();
        	
        	if (v != prev) {
        		//indegreeBuffer.putInt(4 * v, 0);
        		seen.clear();
        	}
    		if (!seen.contains(u)) {
	            int d = indegreeBuffer.getInt(4 * v);
	            indegreeBuffer.putInt(4 * v, d + 1);
	            seen.add(u);
	            maxIndegree = Math.max(maxIndegree, d + 1);
    		}
            
            prev = v;
        }
        System.out.println("max indegree: " + maxIndegree);
        
        /*indegreeBuffer.position(0);
        for (int i = 0; i < N; ++i)
        	System.out.println(indegreeBuffer.getInt());
        indegreeBuffer.position(0);*/

        destFileChannel.close();
        destRAFile.close();
        
        // Create the graph representation from the origin-sorted edge list
        RandomAccessFile originRAFile = new RandomAccessFile(new File("originSorted" + N + ".dat"),"rw");
    	FileChannel originFileChannel = originRAFile.getChannel();
    	MappedByteBuffer originBuffer = originFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * 2 * 3 * N);
    	
    	/*originBuffer.position(0);
        while (originBuffer.hasRemaining())
        	System.out.println(originBuffer.getInt() + ", " + originBuffer.getInt());
        originBuffer.position(0);*/
    	
    	IOEdgesBuffer edges = new IOEdgesBuffer(N, "edges1.dat");
    	
    	int pointer = 0;
    	prev = -1;
    	seen = new HashSet<Integer>();
    	while (originBuffer.hasRemaining()) {
    		int u = originBuffer.getInt();
        	int v = originBuffer.getInt();
        	
        	if (prev != u) {
        		if (prev >= 0) {
        			for (int j = prev; j < u; ++j) {
        				edges.addEdge(-1);
        				++pointer;
        			}
        		}
        		vertices.setEdgesAt(u, pointer);
        		seen.clear();
        	}
        	if (!seen.contains(v)) {
        		seen.add(v);
        		edges.addEdge(v);
        		++pointer;
        	}
        	
        	prev = u;
    	}
    	for (int j = prev; j < N; ++j) {
    		edges.addEdge(-1);
    		++pointer;
    	}
    	
    	//System.out.println(vertices);
    	//System.out.println(edges);
    	
    	originFileChannel.close();
        originRAFile.close();
    	
        // Run the actual topological sorting algorithm
        IOVertexBuffer sortedVertices = new IOVertexBuffer(N, "vertices2.dat");
        IOEdgesBuffer sortedEdges = new IOEdgesBuffer(N, "edges2.dat");
        
        Queue<Integer> Q = new LinkedList<Integer>();
                
        /*this loop gathers all the vertices which have 0 arriving edges to it and puts it in a queue*/
        for (int i = 0; i < N; ++i) {
            int d = indegreeBuffer.getInt(4 * i);
            if (d == 0)
            	Q.offer(i);
        }

        int time = 0;
        pointer = 0;
        while (!Q.isEmpty()) {
            int uid = Q.poll();
            IOVertex u = vertices.getVertexAt(uid);
            IOVertex v = new IOVertex(uid, time++, u.getX(), u.getY(), pointer);
            
            sortedVertices.addVertex(v); //Clone So that java does not just do pointer changes
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

        indegreeFileChannel.close();
        RAFile.close();
        
        if (indegreeFile.exists())
        	indegreeFile.delete();

        return new IOGraph(N, sortedVertices, sortedEdges);
    }
    
    public static void main(String[] args) throws IOException {
    	int N = 10;
    	int edges = 3 * N;
    	int bytes = 2 * 4 * edges;
    	
    	
    	/*RandomAccessFile rafOrigin = new RandomAccessFile(new File("originSorted" + N + ".dat"), "rw");
    	FileChannel fcOrigin = rafOrigin.getChannel();
        MappedByteBuffer bufferOrigin = fcOrigin.map(FileChannel.MapMode.READ_WRITE,0,bytes);

        bufferOrigin.putInt(0); bufferOrigin.putInt(1);
        bufferOrigin.putInt(0); bufferOrigin.putInt(2);
        bufferOrigin.putInt(1); bufferOrigin.putInt(6);
        bufferOrigin.putInt(2); bufferOrigin.putInt(5);
        bufferOrigin.putInt(3); bufferOrigin.putInt(2);
        bufferOrigin.putInt(3); bufferOrigin.putInt(5);
        bufferOrigin.putInt(4); bufferOrigin.putInt(1);
        bufferOrigin.putInt(6); bufferOrigin.putInt(7);
        
        fcOrigin.close();
        rafOrigin.close();
        
        RandomAccessFile rafDest = new RandomAccessFile(new File("destSorted" + N + ".dat"), "rw");
    	FileChannel fcDest = rafDest.getChannel();
        MappedByteBuffer bufferDest = fcDest.map(FileChannel.MapMode.READ_WRITE,0,bytes);

        bufferDest.putInt(0); bufferDest.putInt(1);
        bufferDest.putInt(4); bufferDest.putInt(1);
        bufferDest.putInt(0); bufferDest.putInt(2);
        bufferDest.putInt(3); bufferDest.putInt(2);
        bufferDest.putInt(2); bufferDest.putInt(5);
        bufferDest.putInt(3); bufferDest.putInt(5);
        bufferDest.putInt(1); bufferDest.putInt(6);
        bufferDest.putInt(6); bufferDest.putInt(7);
        
        fcDest.close();
        rafDest.close();*/
        
        IOVertexBuffer vertices = new IOVertexBuffer(N, "vertices1.dat");
        for (int i = 0; i < N; ++i)
        	vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
        
        try {
        	IOGraph graph = IOTopologicalSortBFS(vertices, N);
        	System.out.println(graph.getVertices());
        	System.out.println(graph.getEdges());
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
