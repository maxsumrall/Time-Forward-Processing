
public class IOVertex {
	public int id, time, x, y, edges;
	
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

	final int getTime() {
		return time;
	}

	final void setTime(int time) {
		this.time = time;
	}

	final int getId() {
		return id;
	}

	final void setId(int id) {
		this.id = id;
	}

	final int getX() {
		return x;
	}

	final void setX(int x) {
		this.x = x;
	}

	final int getY() {
		return y;
	}

	final void setY(int y) {
		this.y = y;
	}

	final int getEdges() {
		return edges;
	}

	final void setEdges(int edges) {
		this.edges = edges;
	}
	
	public String toString() {
		return "<" + this.id + ", " + this.time + ", " + this.x + ", " + this.y + ", " + this.edges + ">";
	}
}
