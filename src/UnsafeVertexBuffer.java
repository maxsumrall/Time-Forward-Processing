import java.io.IOException;

public class UnsafeVertexBuffer {
    final SuperArray verticesBuffer;
    final int size;
    static final int FIELD_SIZE = 1;
    static final int NUM_FIELDS = 5;

    long curpos;
    int pos;
    int time;
    int vid;
    int x;
    int y;
    int edges;

    public UnsafeVertexBuffer(int size, String filename) throws Exception {
        this.size = size;
        verticesBuffer = new SuperArray(size * NUM_FIELDS);
    }

    public final void addVertex(IOVertex v) {
        verticesBuffer.putInt(v.getId());
        verticesBuffer.putInt(v.getTime());
        verticesBuffer.putInt(v.getX());
        verticesBuffer.putInt(v.getY());
        verticesBuffer.putInt(v.getEdges());
    }

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
    }

    public void delete() throws IOException{
        this.verticesBuffer.discard();
    }

    public final int getSize() {
        return this.size;
    }

    public final void setEdgesAt(int id, int edgesLocal) {
        this.curpos = verticesBuffer.position();
        this.pos = NUM_FIELDS * FIELD_SIZE * id + FIELD_SIZE * (NUM_FIELDS - 1);

        verticesBuffer.putInt(pos, edgesLocal);

        verticesBuffer.position(curpos);
    }

    public final void setTimeAt(int id, int timeLocal) {
        this.curpos = verticesBuffer.position();
        this.pos = NUM_FIELDS * FIELD_SIZE * id + FIELD_SIZE;

        verticesBuffer.putInt(pos, timeLocal);

        verticesBuffer.position(curpos);
    }

    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < size; ++i)
            ans.append(this.getVertexAt(i) + "\n");

        return ans.toString();
    }
}
