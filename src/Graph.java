import java.util.*;

public class Graph {
	private ArrayList<Vertex> vertices;
	
	public Graph(int N) {
		this.vertices = new ArrayList<Vertex>(N);
		for (int i = 0; i < N; ++i)
			vertices.add(new Vertex(i));
	}
	
	public void addEdge(int i, int j) {
		this.getVertexAt(i).getEdges().add(new Edge(this.getVertexAt(j),this.getVertexAt(j).getId()));
	}
	
	public int getSize() {
		return this.vertices.size();
	}
	
	public Vertex getVertexAt(int i) {
		return this.vertices.get(i);
	}
}
