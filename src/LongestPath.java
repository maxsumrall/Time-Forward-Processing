import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class LongestPath {
	
	static class QueueItem implements Comparable<QueueItem> {
		int time, distance;
		public QueueItem(int time, int distance) {
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
				QueueItem newItem = new QueueItem(v.getTime(), distance[u.getId()]);
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
	public static int[] IOLongestPathTimeForward(IOGraph G, int M) throws IOException {
		int N = G.getSize();
		RandomAccessFile raf = new RandomAccessFile(new File("outputTF.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE,0, 4 * N);
		
		int B = (int)Math.ceil((double)N / M);
		ArrayList<ArrayList<QueueItem>> files = new ArrayList<ArrayList<QueueItem>>();
		for (int i = 0; i < B; ++i)
			files.add(new ArrayList<QueueItem>());
		
		int currentPeriod = -1;
		PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
		for (int i = 0; i < N; ++i) {
			
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
				QueueItem newItem = new QueueItem(v.getTime(), distance[u.getId()]);
				if (period == currentPeriod) {
					Q.offer(newItem);
				} else {
					files.get(period).add(newItem);
				}
			}
		}
		
		return distance;
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
        	IOGraph graph = TopologicalSorting.IOTopologicalSortBFS(vertices, N);
        	System.out.println(graph.getVertices());
        	System.out.println(graph.getEdges());
        	
        	IOLongestPathDP(graph);
        	
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
