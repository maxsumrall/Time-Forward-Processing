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
<<<<<<< HEAD
    public static IOGraph IOTopologicalSortBFS(IOVertexBuffer vertices, int N) throws Exception{
    	
    	RandomAccessFile RAFile = new RandomAccessFile(new File("indegree.tmp"),"rw");
    	FileChannel indegreeFileChannel = RAFile.getChannel();
    	MappedByteBuffer indegreeBuffer = indegreeFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * N);
    	
    	
    	// Calculate the indegree for each vertex from the destination-sorted edge list
    	RandomAccessFile destRAFile = new RandomAccessFile(new File("destSorted" + N + ".dat"),"rw");
    	FileChannel destFileChannel = destRAFile.getChannel();
    	MappedByteBuffer destBuffer = destFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * 2 * N);
    	
    	
        //IOQueue Q = new IOQueue(N);
        //ArrayList<Vertex> topsort = new ArrayList<Vertex>();
        //int[] inDegree = new int[N];
=======
    public static ArrayList<Vertex> TopologicalSortBFS_IO(Graph G) throws Exception{
        int N = G.getSize();
        Queue<Vertex> Q = new LinkedList<Vertex>();
        //IOQueue Q = new IOQueue(N);
        ArrayList<Vertex> topsort = new ArrayList<Vertex>();
        int[] inDegree = new int[N];
>>>>>>> 693084350c5cf4d6fddfd847e3a632e6f1ef3c45

        /* this loop calculates for each vertex how many edges arrive at it*/
    	indegreeBuffer.position(0);
        for (int i = 0; i < N; ++i)
        	indegreeBuffer.putInt(0);
        
        indegreeBuffer.position(0);
    	int prev = -1;
        while (destBuffer.hasRemaining()) { //for each vertex
        	int u = destBuffer.getInt();
        	int v = destBuffer.getInt();
        	
        	if (v != prev) {
        		indegreeBuffer.putInt(4 * v, 1);
        	} else {
	            int d = indegreeBuffer.getInt(4 * v);
	            indegreeBuffer.putInt(4 * v, d + 1);
        	}
            
            prev = v;
        }

        destFileChannel.close();
        destRAFile.close();
        
        // Create the graph representation from the origin-sorted edge list
        RandomAccessFile originRAFile = new RandomAccessFile(new File("originSorted" + N + ".dat"),"rw");
    	FileChannel originFileChannel = originRAFile.getChannel();
    	MappedByteBuffer originBuffer = originFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4 * 2 * N);
    	
    	IOEdgesBuffer edges = new IOEdgesBuffer(N, "edges1.dat");
    	
    	int pointer = 0;
    	prev = -1;
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
        	}
        	edges.addEdge(v);
        	++pointer;
        	
        	prev = u;
    	}
    	for (int j = prev; j < N; ++j) {
    		edges.addEdge(-1);
    		++pointer;
    	}
    	
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
            	int curpos = indegreeBuffer.position();
                int d = indegreeBuffer.getInt(4 * w.getId());
                indegreeBuffer.putInt(4 * w.getId(), --d);
                indegreeBuffer.position(curpos);
                
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

        return new IOGraph(N, sortedVertices, sortedEdges);
    }
    
    public static void main(String[] args) throws IOException {
    	int edges = 8;
    	int bytes = 2 * 4 * edges;
    	int N = 8;
    	
    	RandomAccessFile rafOrigin = new RandomAccessFile(new File("originSorted" + N + ".dat"), "rw");
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
        rafDest.close();
        
        IOVertexBuffer vertices = new IOVertexBuffer(N, "vertices1.dat");
        vertices.addVertex(new IOVertex(0, 0, 10, 20, -1));
        vertices.addVertex(new IOVertex(1, 1, 10, 20, -1));
        vertices.addVertex(new IOVertex(2, 2, 10, 20, -1));
        vertices.addVertex(new IOVertex(3, 3, 10, 20, -1));
        vertices.addVertex(new IOVertex(4, 4, 10, 20, -1));
        vertices.addVertex(new IOVertex(5, 5, 10, 20, -1));
        vertices.addVertex(new IOVertex(6, 6, 10, 20, -1));
        vertices.addVertex(new IOVertex(7, 7, 10, 20, -1));
        
        try {
        	IOGraph graph = IOTopologicalSortBFS(vertices, N);
        	System.out.println(graph.getVertices());
        	System.out.println(graph.getEdges());
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
