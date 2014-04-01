import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class IOVertexBuffer {
    File vertFile;
	RandomAccessFile rafVertices;
	FileChannel verticesFileChannel;
	MappedByteBuffer verticesBuffer;
	int size;
	
	static final int FIELD_SIZE = 4;
	static final int NUM_FIELDS = 5;
	
	public IOVertexBuffer(int size, String filename) throws IOException {
		this.size = size;
        vertFile = new File(filename);
		rafVertices = new RandomAccessFile(vertFile,"rw");
		verticesFileChannel = rafVertices.getChannel();
		verticesBuffer = verticesFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, NUM_FIELDS * FIELD_SIZE * size); // <id, time, x, y, edges>	
	}
	
	public void addVertex(IOVertex v) {
		verticesBuffer.putInt(v.getId());
		verticesBuffer.putInt(v.getTime());
		verticesBuffer.putInt(v.getX());
		verticesBuffer.putInt(v.getY());
		verticesBuffer.putInt(v.getEdges());
	}
	
	public IOVertex getVertexAt(int id) {
		int curpos = verticesBuffer.position();
		int pos = NUM_FIELDS * FIELD_SIZE * id;
		
		verticesBuffer.position(pos);
		int vid = verticesBuffer.getInt();
		int time = verticesBuffer.getInt();
		int x = verticesBuffer.getInt();
		int y = verticesBuffer.getInt();
		int edges = verticesBuffer.getInt();
		
		IOVertex v = new IOVertex(vid, time, x, y, edges);
		verticesBuffer.position(curpos);
		
		return v;
	}
	
	public void close() throws IOException {
		verticesFileChannel.close();
        rafVertices.close();
	}
    public void delete() throws IOException{
        this.close();
        this.vertFile.delete();
    }
	
	public int getSize() {
		return this.size;
	}
	
	public void setEdgesAt(int id, int edges) {
		int curpos = verticesBuffer.position();
		int pos = NUM_FIELDS * FIELD_SIZE * id + FIELD_SIZE * (NUM_FIELDS - 1);
		
		verticesBuffer.putInt(pos, edges);
		
		verticesBuffer.position(curpos);
	}
	
	public void setTimeAt(int id, int time) {
		int curpos = verticesBuffer.position();
		int pos = NUM_FIELDS * FIELD_SIZE * id + FIELD_SIZE;
		
		verticesBuffer.putInt(pos, time);
		
		verticesBuffer.position(curpos);
	}
	
	public String toString() {
		StringBuilder ans = new StringBuilder();
		for (int i = 0; i < size; ++i)
			ans.append(this.getVertexAt(i) + "\n");
		
		return ans.toString();
	}
 }
