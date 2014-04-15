import java.io.IOException;

public class UnsafeVertexBuffer {
    final SuperArray verticesBuffer;
    final int size;
    static final int FIELD_SIZE = 1;
    static final int NUM_FIELDS = 1;

    long curpos;
    int pos;
    int vid;

    public UnsafeVertexBuffer(int size, String filename) throws Exception {
        this.size = size;
        verticesBuffer = new SuperArray(size * NUM_FIELDS);
    }

    public final void addVertex(IOVertex v) {
        verticesBuffer.putInt(v.getId());
    }

    public final IOVertex getVertexAt(int id) {
        this.curpos = verticesBuffer.position();
        this.pos = NUM_FIELDS * FIELD_SIZE * id;

        verticesBuffer.position(pos);
        this.vid = verticesBuffer.getInt();
        verticesBuffer.position(curpos);

        return new IOVertex(vid);
    }

    public void delete() throws IOException{
        this.verticesBuffer.discard();
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
