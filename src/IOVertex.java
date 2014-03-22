
public class IOVertex {
	private int id, time, x, y, edges;
	
	public IOVertex(int id, int time, int x, int y, int edges) {
		this.id = id;
		this.time = time;
		this.x = x;
		this.y = y;
		this.edges = edges;
	}
	
	public IOVertex(int id, int time, int x, int y) {
		this.id = id;
		this.time = time;
		this.x = x;
		this.y = y;
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

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getEdges() {
		return edges;
	}

	public void setEdges(int edges) {
		this.edges = edges;
	}
	
	public String toString() {
		return "<" + this.id + ", " + this.time + ", " + this.x + ", " + this.y + ", " + this.edges + ">";
	}
}
