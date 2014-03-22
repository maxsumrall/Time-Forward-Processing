import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.channels.FileChannel;

public class IOGraph {
	IOVertexBuffer vertices;
	IOEdgesBuffer edges;
	int size;
	
	public IOGraph(int size, IOVertexBuffer vertices, IOEdgesBuffer edges) throws IOException {
		this.size = size;
		this.vertices = vertices;
		this.edges = edges;
	}

	public IOVertexBuffer getVertices() {
		return vertices;
	}

	public void setVertices(IOVertexBuffer vertices) {
		this.vertices = vertices;
	}

	public IOEdgesBuffer getEdges() {
		return edges;
	}

	public void setEdges(IOEdgesBuffer edges) {
		this.edges = edges;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
