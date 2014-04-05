import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class IOVertexBuffer {
    final File vertFile;
	final RandomAccessFile rafVertices;
	final FileChannel verticesFileChannel;
	final MappedByteBuffer verticesBuffer;
	final int size;
	static final int FIELD_SIZE = 4;
	static final int NUM_FIELDS = 1;
    byte[] tempStorage = new byte[FIELD_SIZE*NUM_FIELDS];

    int curpos;
    int pos;
    int vid;

	public IOVertexBuffer(int size, String filename) throws IOException {
		this.size = size;
        vertFile = new File(filename);
		rafVertices = new RandomAccessFile(vertFile,"rw");
		verticesFileChannel = rafVertices.getChannel();
		verticesBuffer = verticesFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, NUM_FIELDS * FIELD_SIZE * size); // <id, time, x, y, edges>	
	}
	
	public final void addVertex(IOVertex v) {
		verticesBuffer.putInt(v.getId());
	}

	/* replaced with potentially faster version*/
	public final IOVertex getVertexAt(int id) {
		verticesBuffer.mark();
		this.pos = NUM_FIELDS * FIELD_SIZE * id;
		
		verticesBuffer.position(pos);
		this.vid = verticesBuffer.getInt();
		verticesBuffer.reset();
		
		return new IOVertex(vid);
	}

	public void close() throws IOException {
		verticesFileChannel.close();
        rafVertices.close();
	}
    public void delete() throws IOException{
        this.close();
        this.vertFile.delete();
    }
	
	public final int getSize() {
		return this.size;
	}
	
	public String toString() {
		StringBuilder ans = new StringBuilder();
		for (int i = 0; i < size; ++i)
			ans.append(this.getVertexAt(i) + "\n");
		return ans.toString();
	}
 }
