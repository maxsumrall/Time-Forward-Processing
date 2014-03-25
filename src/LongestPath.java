import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class LongestPath {
	
	static class QueueItem implements Comparable<QueueItem> {
		int id, time, distance;
		public QueueItem(int id, int time, int distance) {
			this.id = id;
			this.time = time;
			this.distance = distance;
		}
		@Override
		public int compareTo(QueueItem q) {
			return this.time - q.time;
		}
	}
	
	public static int[] LongestPathDP(Graph G) {
		ArrayList<Vertex> topsort = TopologicalSorting.TopologicalSortBFS(G);
		
		int[] dist = new int[G.getSize()];
		for (Vertex v : topsort)
			for (Edge e : v.getEdges()) {
				Vertex w = e.getTo();
				dist[w.getId()] = Math.max(dist[w.getId()], dist[v.getId()] + 1);
			}
		
		return dist;
	}
	
	/**
	 * The graph is assumed to be in topological order.
	 * 
	 * @param G
	 * @return
	 */
    public static void IOLongestPathDP(IOGraph G) throws IOException {
		int N = G.getSize();
		RandomAccessFile raf = new RandomAccessFile(new File("outputDP.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE,0, 4 * N);
	    
	    buffer.position(0);
		for (int i = 0; i < N; ++i)
			buffer.putInt(0);
			
		buffer.position(0);
		for (int i = 0; i < N; ++i) {
			IOVertex u = G.getVertices().getVertexAt(i);
			for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
				
				int distU = buffer.getInt(4 * u.getId());
				int distV = buffer.getInt(4 * to);
				int newDist = Math.max(distV, distU + 1);
				buffer.putInt(4 * to, newDist);
			}
		}
		
		buffer.position(0);
		for (int i = 0; i < N; ++i)
			System.out.println(buffer.getInt());
		
		fc.close();
		raf.close();
		
	}
	
	public static int[] LongestPathTimeForward(Graph G, int M) {
		ArrayList<Vertex> topsort = TopologicalSorting.TopologicalSortBFS(G);
		int T = G.getSize();
		
		int[] distance = new int[T];
		
		int B = (int)Math.ceil((double)T / M);
		ArrayList<ArrayList<QueueItem>> files = new ArrayList<ArrayList<QueueItem>>();
		for (int i = 0; i < B; ++i)
			files.add(new ArrayList<QueueItem>());
		
		int currentPeriod = -1;
		PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
		for (int i = 0; i < T; ++i) {
			
			Vertex u = topsort.get(i);
			int time = u.getTime();
			
			if (time % M == 0) {
				++currentPeriod;
				Q.clear();
				for (QueueItem x : files.get(currentPeriod))
					Q.offer(x);
			}
			
			// Process current vertex
			int maxDistance = 0;
			while (!Q.isEmpty()) {
				QueueItem top = Q.peek();
				if (top.time != time)
					break;
				Q.poll();
				maxDistance = Math.max(maxDistance, top.distance + 1);
			}
			
			distance[u.getId()] = maxDistance;
			
			// Put information of neighbors in data structure
			for (Edge e : u.getEdges()) {
				Vertex v = e.getTo();
				int period = v.getTime() / M;
				QueueItem newItem = new QueueItem(v.getId(), v.getTime(), distance[u.getId()]);
				if (period == currentPeriod) {
					Q.offer(newItem);
				} else {
					files.get(period).add(newItem);
				}
			}
		}
		
		return distance;
	}
	
	/**
	 * The graph is assumed to be in topological order.
	 * 
	 * @param G
	 * @return
	 */
	public static void IOLongestPathTimeForward(IOGraph G, int M) throws IOException {
		int N = G.getSize();
		RandomAccessFile raf = new RandomAccessFile(new File("outputTF.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, 4 * N);
		
		int B = (int)Math.ceil((double)N / M);
		
		RandomAccessFile rafTf = new RandomAccessFile(new File("tf.tmp"), "rw");
		FileChannel fcTf = raf.getChannel();
		
		MappedByteBuffer[] buffers = new MappedByteBuffer[B];
		int[] counter = new int[B]; // Counts how many edges per buffer
		
		int nBytes = 4 * 3 * 10 * M; // 4 bytes * <id, time, dist> * max_indegree * M
		for (int i = 0; i < B; ++i) {
			buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
		}

		int currentPeriod = -1;
		PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
		for (int i = 0; i < N; ++i) {
			
			IOVertex u = G.getVertices().getVertexAt(i);
			int time = u.getTime();
			
			if (time % M == 0) {
				++currentPeriod;
				Q.clear();
				MappedByteBuffer buf = buffers[currentPeriod];
				buf.position(0);
				for (int k = 0; k < counter[currentPeriod]; ++k) {
					int id = buf.getInt();
					int t = buf.getInt();
					int dist = buf.getInt();
					Q.offer(new QueueItem(id, t, dist));
				}
			}
			
			// Process current vertex
			int maxDistance = 0;
			while (!Q.isEmpty()) {
				QueueItem top = Q.peek();
				if (top.time != time)
					break;
				Q.poll();
				maxDistance = Math.max(maxDistance, top.distance + 1);
			}
			
			distBuffer.putInt(4 * u.getId(), maxDistance);
			
			// Put information of neighbors in data structure
			for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
            	IOVertex v = G.getVertices().getVertexAt(to);
            	int period = v.getTime() / M;
            	int d = distBuffer.getInt(4 * u.getId());
				QueueItem newItem = new QueueItem(v.getId(), v.getTime(), d);
				if (period == currentPeriod) {
					Q.offer(newItem);
				} else {
					buffers[period].putInt(4 * counter[period], v.getId());
					buffers[period].putInt(4 * (counter[period] + 1), v.getTime());
					buffers[period].putInt(4 * (counter[period] + 2), d);
					++counter[period];
				}
			}
		}
		
		distBuffer.position(0);
		System.out.println();
		for (int i = 0; i < N; ++i)
			System.out.println(distBuffer.getInt());
		
		fc.close();
		raf.close();
		fcTf.close();
		rafTf.close();
	}
	
	public static void main(String[] args) throws IOException {
		int N = 9;
    	int edges = 3 * N;
    	int bytes = 2 * 4 * edges;
    	
    	/*RandomAccessFile raf = new RandomAccessFile(new File("prueba.dat"), "rw");
    	FileChannel fc = raf.getChannel();
    	
    	MappedByteBuffer[] buffers = new MappedByteBuffer[3];
    	for (int i = 0; i < 3; ++i)
    		buffers[i] = fc.map(FileChannel.MapMode.READ_WRITE, 4 * 5 * i, 4 * 5);
    	
    	int k = 1;
    	for (int i = 0; i < 3; ++i) {
    		for (int j = 0; j < 5; ++j)
    			buffers[i].putInt(k++);
    	}
    	
    	for (int i = 0; i < 3; ++i) {
    		buffers[i].position(0);
    		for (int j = 0; j < 5; ++j)
    			System.out.println(buffers[i].getInt(4 * j));
    	}
    	
    	raf.close();*/
    	
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
        	IOGraph graph = TopologicalSorting.IOTopologicalSortBFS(vertices, N);
        	System.out.println(graph.getVertices());
        	System.out.println(graph.getEdges());
        	
        	IOLongestPathDP(graph);
        	IOLongestPathTimeForward(graph, N);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
