import java.util.*;

public class Vertex implements Comparable<Vertex> {
	private int id;
	private int time;
    private ArrayList<Edge> edges;


    public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

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
		return "(" + id + ", " + time + ")";
	}

	@Override
	public int compareTo(Vertex v) {
		return this.time - v.time;
	}
}
