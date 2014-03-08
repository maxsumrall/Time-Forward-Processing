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
}
