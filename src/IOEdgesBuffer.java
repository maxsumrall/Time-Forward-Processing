import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class IOEdgesBuffer {
    File edgesFile;
	RandomAccessFile rafEdges;
	FileChannel edgesFileChannel;
	MappedByteBuffer edgesBuffer;
	int size; // number of edges <= 4V
	
	static final int FIELD_SIZE = 4;
	
	public IOEdgesBuffer(int V, String filename) throws IOException {
        edgesFile = new File(filename);
		this.size = 4 * V; // Number of edges <= 3V + V
		rafEdges = new RandomAccessFile(edgesFile,"rw");
		edgesFileChannel = rafEdges.getChannel();
		edgesBuffer = edgesFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FIELD_SIZE * size); // <dest>	
	}
	
	final void addEdge(int to) {
		this.edgesBuffer.putInt(to);
	}
	
	final int getEdge(int position) {
		int curpos = edgesBuffer.position();
		int pos = position * FIELD_SIZE;
		int to = edgesBuffer.getInt(pos);
		edgesBuffer.position(curpos);
		
		return to;
	}
	
	public String toString() {
		StringBuilder ans = new StringBuilder();
		int curpos = edgesBuffer.position();
		edgesBuffer.position(0);
		boolean first = true;
		while (edgesBuffer.hasRemaining()) {
			if (!first) {
				ans.append(", ");
			} else {
				first = false;
			}
			ans.append(edgesBuffer.getInt());
		}
			
			
		edgesBuffer.position(curpos);
		
		return ans.toString();
	}
    public void close() throws IOException{
        this.edgesFileChannel.close();
        this.rafEdges.close();
    }
    public void delete() throws IOException{
        this.close();
        this.edgesFile.delete();
    }
}
