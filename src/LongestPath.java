import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class LongestPath {
	
	static final int FIELD_SIZE = 4; // 4 bytes
	
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
	    MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);
	    
	    buffer.position(0);
		for (int i = 0; i < N; ++i)
			buffer.putInt(0);
			
		buffer.position(0);
		for (int i = 0; i < N; ++i) {
			IOVertex u = G.getVertices().getVertexAt(i);
			for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
				
				int distU = buffer.getInt(FIELD_SIZE * u.getId());
				int distV = buffer.getInt(FIELD_SIZE * to);
				int newDist = Math.max(distV, distU + 1);
				buffer.putInt(FIELD_SIZE * to, newDist);
			}
		}

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
			
			if (u.getTime() % M == 0) {
				++currentPeriod;
				Q.clear();
				for (QueueItem x : files.get(currentPeriod))
					Q.offer(x);
			}
			
			// Process current vertex
			int maxDistance = 0;
			while (!Q.isEmpty()) {
				QueueItem top = Q.peek();
				if (top.time != u.getTime())
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
	    MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);
		
		int B = (int)Math.ceil((double)N / M);
		
		File fileTf = new File("tf.tmp");
		RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
		FileChannel fcTf = raf.getChannel();
		
		MappedByteBuffer[] buffers = new MappedByteBuffer[B];
		int[] counter = new int[B]; // Counts how many edges per buffer
		
		int maxIndegree = 20;
		
		int nBytes = FIELD_SIZE * 3 * maxIndegree * M; // 4 bytes * <id, time, dist> * max_indegree * M
		for (int i = 0; i < B; ++i) {
			buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
		}

		int currentPeriod = -1;
		PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
		for (int i = 0; i < N; ++i) {
			
			IOVertex u = G.getVertices().getVertexAt(i);
			
			if (u.getTime() % M == 0) {
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
				if (top.time != u.getTime())
					break;
				Q.poll();
				maxDistance = Math.max(maxDistance, top.distance + 1);
			}
			
			distBuffer.putInt(FIELD_SIZE * u.getId(), maxDistance);
			
			// Put information of neighbors in data structure
			for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
            	IOVertex v = G.getVertices().getVertexAt(to);
            	int period = v.getTime() / M;
            	int d = distBuffer.getInt(FIELD_SIZE * u.getId());
				QueueItem newItem = new QueueItem(to, v.getTime(), d);
				if (period == currentPeriod) {
					Q.offer(newItem);
				} else {
					buffers[period].putInt(3 * FIELD_SIZE * counter[period], to);
					buffers[period].putInt(3 * FIELD_SIZE * counter[period] + FIELD_SIZE, v.getTime());
					buffers[period].putInt(3 * FIELD_SIZE * counter[period] + 2 * FIELD_SIZE, d);
					++counter[period];
				}
			}
		}

		fc.close();
		raf.close();
		fcTf.close();
		rafTf.close();
		
		if (fileTf.exists())
			fileTf.delete();
	}
	
	public static void main(String[] args) throws IOException {
		int N = 50000;
        
        IOVertexBuffer vertices = new IOVertexBuffer(N, "vertices1.dat");
        for (int i = 0; i < N; ++i)
        	vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
        
        try {
        	IOGraph graph = TopologicalSorting.IOTopologicalSortBFS(vertices, N);
        	//System.out.println(graph.getVertices());
        	//System.out.println(graph.getEdges());
        	
        	Graph G = new Graph(N);
        	for (int i = 0; i < N; ++i) {
        		IOVertex u = graph.getVertices().getVertexAt(i);
	        	for (int e = u.getEdges(), to = 0; e >= 0 && (to = graph.getEdges().getEdge(e)) != -1; ++e) {
	        		G.addEdge(u.getId(), to);
	        	}
        	}
        	
        	System.out.println("Regular DP:");
        	int[] dist = LongestPathDP(G);
        	for (int i = N - 20; i < N; ++i)
    			System.out.print(dist[i] + ", ");
        	System.out.println();
        	
        	System.out.println("IO DP:");
        	IOLongestPathDP(graph);
        	
        	System.out.println("Regular TF");
        	int[] dist2 = LongestPathTimeForward(G, 5);
        	for (int i = N - 20; i < N; ++i)
    			System.out.print(dist2[i] + ", ");
        	System.out.println();
        	
        	System.out.println("IO TF:");
        	IOLongestPathTimeForward(graph, 5);
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
