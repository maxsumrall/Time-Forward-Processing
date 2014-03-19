import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.util.*;

/**
 * Created by max on 3/19/14.
 * Maintains a mappedbytebuffer as the underlying implementation, but lets you use it like a queue.
 * Allows for larger-than-physical-memory queues.
 *
 * Please call close on me when finished.
 */
public class IOQueue{
    private File file;
    private RandomAccessFile randomAccessFile;
    private FileChannel fileChannel;
    private MappedByteBuffer buffer;
    private int readPointer = 0;
    private int writePointer = 0;
    public IOQueue(int size) throws Exception{
        file = new File("IOQueue.temp");
        randomAccessFile = new RandomAccessFile(file, "rw");
        fileChannel = randomAccessFile.getChannel();
        buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE,0,size);
    }

    public int get(){
        buffer.position(readPointer);
        int returnVal = buffer.getInt();
        readPointer += 4;
        return returnVal;
    }
    public void put(int val){
        buffer.position(writePointer);
        buffer.putInt(val);
        writePointer += 4;
    }

    public void close() throws Exception{
        fileChannel.close();
        randomAccessFile.close();
        file.delete();
    }
    protected void finalize () throws Exception{
        this.close();
    }



}
