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
	static final int NUM_FIELDS = 5;
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
		verticesBuffer.putInt(v.getTime());
		verticesBuffer.putInt(v.getX());
		verticesBuffer.putInt(v.getY());
		verticesBuffer.putInt(v.getEdges());
	}

	/* replaced with potentially faster version
	public final IOVertex getVertexAt(int id) {
		this.curpos = verticesBuffer.position();
		this.pos = NUM_FIELDS * FIELD_SIZE * id;
		
		verticesBuffer.position(pos);
		this.vid = verticesBuffer.getInt();
		this.time = verticesBuffer.getInt();
		this.x = verticesBuffer.getInt();
		this.y = verticesBuffer.getInt();
		this.edges = verticesBuffer.getInt();
		verticesBuffer.position(curpos);
		
		return new IOVertex(vid, time, x, y, edges);
	}*/
    public final IOVertex getVertexAt(int id) {
        verticesBuffer.mark();
        verticesBuffer.position(NUM_FIELDS * FIELD_SIZE * id);
        verticesBuffer.get(tempStorage);
        verticesBuffer.reset();
        return new IOVertex(/*vid*/tempStorage[0] << 24 | tempStorage[1] << 16 | tempStorage[2] << 8 | tempStorage[3],
                            /*time*/tempStorage[4] << 24 | tempStorage[5] << 16 | tempStorage[6] << 8 | tempStorage[7],
                             /*x*/tempStorage[8] << 24 | tempStorage[9] << 16 | tempStorage[10] << 8 | tempStorage[11],
                             /*y*/tempStorage[12] << 24 | tempStorage[13] << 16 | tempStorage[14] << 8 | tempStorage[15],
                             /*edges*/tempStorage[16] << 24 | tempStorage[17] << 16 | tempStorage[18] << 8 | tempStorage[19]);
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
	
	public final void setEdgesAt(int id, int edgesLocal) {
        verticesBuffer.mark();
        this.pos = NUM_FIELDS * FIELD_SIZE * id + FIELD_SIZE * (NUM_FIELDS - 1);
		verticesBuffer.putInt(pos, edgesLocal);
        verticesBuffer.reset();
    }
	
	public final void setTimeAt(int id, int timeLocal) {
        verticesBuffer.mark();
		this.pos = NUM_FIELDS * FIELD_SIZE * id + FIELD_SIZE;
		verticesBuffer.putInt(pos, timeLocal);
		verticesBuffer.reset();
	}
	
	public String toString() {
		StringBuilder ans = new StringBuilder();
		for (int i = 0; i < size; ++i)
			ans.append(this.getVertexAt(i) + "\n");
		return ans.toString();
	}
 }
