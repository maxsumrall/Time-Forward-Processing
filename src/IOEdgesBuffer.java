import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class IOEdgesBuffer {
    static final int FIELD_SIZE = 4;
    final File edgesFile;
	final RandomAccessFile rafEdges;
	final FileChannel edgesFileChannel;
	final MappedByteBuffer edgesBuffer;
	final int size; // number of edges <= 4V
    int curpos;
	int pos;
    int to;

	public IOEdgesBuffer(int V, String filename) throws IOException {
        edgesFile = new File(filename);
		this.size = 4 * V; // Number of edges <= 3V + V
		rafEdges = new RandomAccessFile(edgesFile,"rw");
		edgesFileChannel = rafEdges.getChannel();
		edgesBuffer = edgesFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FIELD_SIZE * size); // <dest>	
	}
	
	final void addEdge(int toLocal) {
		this.edgesBuffer.putInt(toLocal);
	}
	
	final int getEdge(int position) {
		this.curpos = edgesBuffer.position();
		this.pos = position * FIELD_SIZE;
		this.to = edgesBuffer.getInt(pos);
		edgesBuffer.position(curpos);
		return this.to;
	}
	
	public String toString() {
		StringBuilder ans = new StringBuilder();
		this.curpos = edgesBuffer.position();
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
