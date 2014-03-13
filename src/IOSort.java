import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

/**
 * Created by max on 3/13/14.
 */
public class IOSort {
    /**
     * At the end of this function, the file has the smaller subsections sorted, still in the same file.
     * @param edgesFile
     */
    ByteBuffer edgesBuffer;
    int smallestSubsetSize = 8; //how many edges to sort in-memory. NUMBER OF EDGES!

    IOSort(File edgesFile){
        //Open the file containing the edges for reading only.
        MappedByteBuffer edgesBuffer = null;
        ByteBuffer tempBuffer = ByteBuffer.allocate(8);
        try{
            FileChannel edgesFileChannel = new RandomAccessFile(edgesFile,"rw").getChannel();
            edgesFileChannel.read(tempBuffer);  //read the first number from the file
            tempBuffer.flip();
            int n = tempBuffer.getInt() + tempBuffer.getInt();   //remember the padding which makes the first one 8 bytes.
            long edgesBufferSize =  (n*3*2*4)+8;
            this.edgesBuffer = edgesFileChannel.map(FileChannel.MapMode.READ_WRITE, 8, edgesBufferSize); //Prepared to handle huge number of edges without consuming heap space
        }
        catch (IOException e){e.printStackTrace(); System.exit(1);}

    }
    public void sortSegments(){

        ArrayList<IOEdge> temp = new ArrayList<IOEdge>();
        int count = edgesBuffer.remaining();
        System.out.println(count);
        int bytesPerSubset = smallestSubsetSize*8;//an edge is two ints, which are 4 bytes
        for (int i = 0; i < count; i+=bytesPerSubset){//for each subset...
            edgesBuffer.mark();
            for(int j = 0; (j < smallestSubsetSize) && (edgesBuffer.remaining() > 8); j++){
                temp.add(new IOEdge(edgesBuffer.getInt(),edgesBuffer.getInt()));
            }
            edgesBuffer.reset();
            Collections.sort(temp);
            for (IOEdge e: temp){
                System.out.println(e.toString());
                edgesBuffer.putInt(e.getID());
                edgesBuffer.putInt(e.getTo());
            }
         temp.clear();
        }
    }
}

