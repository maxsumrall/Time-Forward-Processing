
public class IOVertex {
	public int id;
	
	public IOVertex(int id) {
		this.id = id;	
	}

	final int getId() {
		return id;
	}

	final void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return "<" + this.id + ">";
	}
}
