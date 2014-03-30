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

	final IOVertexBuffer getVertices() {
		return vertices;
	}

	final void setVertices(IOVertexBuffer vertices) {
		this.vertices = vertices;
	}

	final IOEdgesBuffer getEdges() {
		return edges;
	}

	final void setEdges(IOEdgesBuffer edges) {
		this.edges = edges;
	}

	final int getSize() {
		return size;
	}

	final void setSize(int size) {
		this.size = size;
	}
}
