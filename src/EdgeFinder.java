import java.nio.MappedByteBuffer;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

/**
 * Created by max on 3/18/14.
 *
 * A class which manages the huge SORTED file of edges
 *
 * Give me a vertex, and I'll give you the edges originating from that edge.
 */
public class EdgeFinder {

    MappedByteBuffer edgesBuffer;
    int edgesInFile;
    int maxEdge;

    public EdgeFinder(int n) throws IOException{
        //initialize the buffers, etc.
        maxEdge = n;
        File file = new File("edgeData" + n + ".dat");
        RandomAccessFile rFile = new RandomAccessFile(file,"rw");
        FileChannel rChannel = rFile.getChannel();
        edgesBuffer = rChannel.map(FileChannel.MapMode.READ_WRITE,0,rChannel.size());
        edgesInFile = edgesBuffer.capacity()/8;
    }

    public ArrayList<Object> getEdgesFrom(int desiredOriginID){
        //Given this originID, find the edges which start here.
        //So lets do binary search?
        int max = edgesInFile*8;
        int min = 0;
        int currentEdgeB;
        if(desiredOriginID > maxEdge){return null;}

        while(max > min){
            currentEdgeB = midpoint(max,min);//middle of the file, in BYTES
            int currOriginID = edgesBuffer.getInt(currentEdgeB);
            if(currOriginID == desiredOriginID){
                ArrayList<IOEdge> edges = new ArrayList<IOEdge>();
                //step back until we find the first edge with this originID, then step forward to find the last one, and then return this range
                int startingByte = currentEdgeB;
                while (desiredOriginID == edgesBuffer.getInt(startingByte)){
                    System.out.println(startingByte);
                    startingByte -= 8;
                }

                int endingByte = currentEdgeB;
                while(desiredOriginID == edgesBuffer.getInt(endingByte)){
                    endingByte += 8;
                }
                edgesBuffer.position(startingByte);
                while(edgesBuffer.position() != endingByte){
                    IOEdge temp = new IOEdge(edgesBuffer.getInt(),edgesBuffer.getInt());
                    edges.add(temp);
                }
                ArrayList<Object> returnList = new  ArrayList<Object>();
                returnList.add(edges);
                returnList.add(startingByte);
                return returnList;

            }else if(currOriginID < desiredOriginID){
                min = currentEdgeB + 8; //plus 8 bytes for the next edge
            } else{
                max = currentEdgeB - 8;
            }
        }
        return null;//not found
    }
    private int midpoint(int max, int min){
        return Math.round(min + ((max - min) / 2));
    }
}
