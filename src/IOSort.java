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
    long edgesBufferSize;
    IOSort(File edgesFile){
        //Open the file containing the edges for reading only.
        ByteBuffer tempBuffer = ByteBuffer.allocate(8);
        try{
            this.edgesFileChannel = new RandomAccessFile(edgesFile,"rw").getChannel();
            this.edgesInFile = this.edgesFileChannel.size()/8;//divide by 8 because thats how many bytes there are per edge, and thats what I want to know
            edgesFileChannel.read(tempBuffer);  //read the first number from the file
            tempBuffer.flip();
            int n = tempBuffer.getInt() + tempBuffer.getInt();   //remember the padding which makes the first one 8 bytes.
            edgesBufferSize =  (n*3*2*4);
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

    /**
     * We have two buffers which are actual files but we can treat them like buffers in memory since the OS handles paging
     *
     * EDGES = buffer holding the edges in various states of unorderedness
     * TEMP = buffer where merges go
     *
     * Edges are group into subsets. Initialy I set this as 8. Every set of 8 edges in the EDGES buffer are brought into memory and sorted naively.
     * Then, we do mergesort starting with these subsets.
     *
     * P and Q represent the current active point within a subset while merging. Initially they refer to the first element in their respective subsets
     *
     * You compare the edge at P and the edge at Q. Take the smaller one and put it in TEMP (along with the second element, since an edge is 2 Ints but we compare only the first)
     * Whichever one (P or Q) was smaller and was put in TEMP, increment P or Q so that the next comparison works.
     * Continue until either P or Q reaches the end of its subsection, and put the remaining elements of the other subsection into TEMP
     * Increment both P and Q to the next subsections.
     * Repeat until all subsections have been merged into TEMP
     * Swap EDGES and TEMP, double the size of the subsections, and repeat until the size of the subsections is the size of the full buffer.
     * Make sure you get EDGES and TEMP swapped back or something.
     */
    public void mergeSort(){

        int subsetSize = smallestSubsetSize*8;
        int currentIndex = 0;
        int P;//P and Q are location indices into the two sets I will merge[sort]
        int Q;
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
            System.out.println(subsetSize/8);
                while(currentIndex+ 2*subsetSize < (this.edgesInFile*8)){
                try{
                    this.edgesFileChannel.position(8);
                    B = tempFileChannel.map(FileChannel.MapMode.READ_WRITE,0,this.edgesBufferSize);  //size of the original buffer
                    //P = this.edgesFileChannel.map(FileChannel.MapMode.READ_WRITE,currentIndex,currentIndex+subsetSize);
                    //Q = this.edgesFileChannel.map(FileChannel.MapMode.READ_WRITE,currentIndex+subsetSize, currentIndex + 2*subsetSize);

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

