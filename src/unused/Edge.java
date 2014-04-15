
public class Edge implements Comparable<Edge> {
    private Vertex to;
    private int ID;
	public Edge(Vertex to, int ID){
        this.ID = ID;
        this.to = to;
    }
    public Edge clone(){
        return new Edge(this.to,this.ID);
    }
	public Vertex getTo() {
		return to;
	}

	public void setTo(Vertex to) {
		this.to = to;
	}
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int compareTo(Edge e){
        return this.ID - e.ID;
    }
}


