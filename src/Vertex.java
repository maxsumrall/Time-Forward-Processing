import java.util.*;

public class Vertex {
	private int id;
	private ArrayList<Edge> edges;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}

	public Vertex(int id) {
		this.id = id;
		this.edges = new ArrayList<Edge>();
	}
	
	public String toString() {
		return "" + id;
	}
}
