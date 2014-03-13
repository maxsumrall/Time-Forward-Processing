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
    MappedByteBuffer edgesBuffer;
    FileChannel edgesFileChannel;
    int smallestSubsetSize = 8; //how many edges to sort in-memory. NUMBER OF EDGES!
    long edgesInFile;
    IOSort(File edgesFile){
        //Open the file containing the edges for reading only.
        ByteBuffer tempBuffer = ByteBuffer.allocate(8);
        try{
            this.edgesFileChannel = new RandomAccessFile(edgesFile,"rw").getChannel();
            this.edgesInFile = this.edgesFileChannel.size()/8;//divide by 8 because thats how many bytes there are per edge, and thats what I want to know
            edgesFileChannel.read(tempBuffer);  //read the first number from the file
            tempBuffer.flip();
            int n = tempBuffer.getInt() + tempBuffer.getInt();   //remember the padding which makes the first one 8 bytes.
            long edgesBufferSize =  (n*3*2*4)+8;
            this.edgesBuffer = this.edgesFileChannel.map(FileChannel.MapMode.READ_WRITE, 8, edgesBufferSize); //Prepared to handle huge number of edges without consuming heap space
        }
        catch (IOException e){e.printStackTrace();}

    }
    public void sortSegments(){

        ArrayList<IOEdge> temp = new ArrayList<IOEdge>();
        int count = edgesBuffer.remaining();
        int bytesPerSubset = smallestSubsetSize*8;//an edge is two ints, which are 4 bytes
        for (int i = 0; i < count; i+=bytesPerSubset){//for each subset...
            edgesBuffer.mark();
            for(int j = 0; (j < smallestSubsetSize) && (edgesBuffer.remaining() > 8); j++){
                temp.add(new IOEdge(edgesBuffer.getInt(),edgesBuffer.getInt()));
            }
            edgesBuffer.reset();
            Collections.sort(temp);
            for (IOEdge e: temp){
                edgesBuffer.putInt(e.getID());
                edgesBuffer.putInt(e.getTo());
            }
         temp.clear();
        }
    }

    public void mergeSort(){
        int subsetSize = smallestSubsetSize;
        int currentIndex = 0;
        MappedByteBuffer P;//P and Q are the two sets I will merge[sort]
        MappedByteBuffer Q;
        //make two maps over the file channel, one for each subset which will be merged.
        // File = [<------P------>|<------Q------>|....................]

        //Make an intermediary file/buffer to move the merged copies to.
        MappedByteBuffer B;
        File tempFile = new File("temp.dat");
        FileChannel tempFileChannel;
       try{
        RandomAccessFile rTempFile = new RandomAccessFile(tempFile,"rw");
        tempFileChannel = rTempFile.getChannel();
       }
        catch(IOException e){e.printStackTrace();return;}
        while(subsetSize < edgesInFile){
                while(currentIndex+ 2*subsetSize < (this.edgesInFile*8)){
                try{
                    B = tempFileChannel.map(FileChannel.MapMode.READ_WRITE,0,this.edgesBuffer.remaining());  //size of the original buffer
                    P = this.edgesFileChannel.map(FileChannel.MapMode.READ_WRITE,currentIndex,currentIndex+subsetSize);
                    Q = this.edgesFileChannel.map(FileChannel.MapMode.READ_WRITE,currentIndex+subsetSize, currentIndex + 2*subsetSize);
                }
                catch (IOException e){e.printStackTrace(); return;}
                int x = P.getInt();
                int y = Q.getInt();

                while(P.hasRemaining() && Q.hasRemaining()){
                    if (x < y){
                        B.putInt(x);
                        B.putInt(P.getInt());//put both the values for the edge
                        x = P.getInt();
                    }
                    else{
                        B.putInt(y);
                        B.putInt(Q.getInt());
                        y = Q.getInt();}
                }
                //Either P or Q has been emptied, so the other one with remaining elements should be dumped into B
                while (P.hasRemaining()){B.putInt(P.getInt());}
                while(Q.hasRemaining()){B.putInt(Q.getInt());}

                currentIndex += 2*subsetSize;
            }
            subsetSize = subsetSize * 2; //merge done, repeat for larger subset sizes until all merges are done.
            //The merges are done in B, So I need to swap B's channel and the main channel
            FileChannel temp = tempFileChannel;
            tempFileChannel = this.edgesFileChannel;
            this.edgesFileChannel = temp;
        }
    }
}

