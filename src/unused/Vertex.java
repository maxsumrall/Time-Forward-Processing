import java.util.ArrayList;

public class Vertex implements Comparable<Vertex> {
	private int id;
	private int time;
    private ArrayList<Edge> edges;

    public Vertex(int id) {
        this.id = id;
        this.edges = new ArrayList<Edge>();
    }

    public Vertex(int id, int time, ArrayList<Edge> edges){
        this.id = id;
        this.time = time; //QUESTION: is this supposed to represent the absolute ordering?
        this.edges = edges;
    }

    public Vertex clone(){
        ArrayList<Edge> edgesClone = new ArrayList<Edge>();
        for(Edge edge: edges){
            edgesClone.add(edge.clone());
        }
        return new Vertex(this.id, this.time, edgesClone);
    }

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
	
	public String toString() {
		return "(" + id + ", " + time + ")";
	}

	@Override
	public int compareTo(Vertex v) {
		return this.time - v.time;
	}
}
