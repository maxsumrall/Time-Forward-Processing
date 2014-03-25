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
    RandomAccessFile RAFile;
    File edgesFile;
    //int smallestSubsetSize = 2000000; //how many edges to sort in-memory. NUMBER OF EDGES!
    int smallestSubsetSize;
    long EDGES_IN_FILE;
    long BYTES_IN_FILE;
    int N;      //the first number in the input file of edges
    long edgesBufferSize;
    String tempFileName;
    IOSort(File edgesFile, int n, String tempFileName) throws Exception{
        //Open the file containing the edges for reading only.
        this.tempFileName = tempFileName;
        this.edgesFile = edgesFile;
        //ByteBuffer tempBuffer = ByteBuffer.allocate(8);

        this.RAFile = new RandomAccessFile(edgesFile,"rw");
        this.edgesFileChannel = this.RAFile.getChannel();
        this.BYTES_IN_FILE = this.RAFile.length();
        this.EDGES_IN_FILE = this.edgesFileChannel.size()/8;//divide by 8 because thats how many bytes there are per edge, and thats what I want to know
        //edgesFileChannel.read(tempBuffer);  //read the first number from the file
        //tempBuffer.flip();
        this.N = n;
        this.edgesBufferSize =  (N*3*2*4+8);
        this.edgesBuffer = this.edgesFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, this.edgesFileChannel.size()); //Prepared to handle huge number of edges without consuming heap space
        this.smallestSubsetSize = Math.min(1000000,this.N/2); //There is not enough room to sort more than 1mil in memory, but we want it to be smaller than N.


    }
    public void sortSegments(){
        this.edgesBuffer.position(0);
        int count = this.edgesBuffer.remaining();
        int bytesPerSubset = this.smallestSubsetSize*8;//an edge is two ints, which are 4 bytes
        for (int i = 0; i < count; i+=bytesPerSubset){//for each subset...
            this.edgesBuffer.mark();
            ArrayList<IOEdge> temp = new ArrayList<IOEdge>();
            for(int j = 0; (j < smallestSubsetSize) && (this.edgesBuffer.remaining() >= 8); j++){
                temp.add(new IOEdge(this.edgesBuffer.getInt(),this.edgesBuffer.getInt()));
            }
            this.edgesBuffer.reset();
            Collections.sort(temp);
            //System.out.println(temp);
            for (IOEdge e: temp){
                assert((e.getID() != 0)&&(e.getTo() != 0));
                this.edgesBuffer.putInt(e.getID());
                this.edgesBuffer.putInt(e.getTo());
            }
        }
       this.edgesBuffer.force();
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
    public void mergeSort() throws IOException{
        int subsetSize = smallestSubsetSize*8;  //bytes
        int currentIndex = 0;     //bytes?  the 8 means I skip the first guy
        FileChannel P;//P and Q are location indices into the two sets I will merge[sort]
        FileChannel Q;
        MappedByteBuffer PBuffer;
        MappedByteBuffer QBuffer;
        RandomAccessFile rTempFile;
        RandomAccessFile rTempFileP;
        RandomAccessFile rTempFileQ;
        //make two maps over the file channel, one for each subset which will be merged.
        // File = [<------P------>|<------Q------>|....................]

        //Make an intermediary file/buffer to move the merged copies to.
        MappedByteBuffer temp;
        File tempFile = new File(this.tempFileName + this.N + ".dat");
        
        if (tempFile.exists())
        	tempFile.delete();
        
        FileChannel tempFileChannel;
       try{
            rTempFile = new RandomAccessFile(tempFile,"rw");
            rTempFileP = new RandomAccessFile(this.edgesFile,"rw");
            rTempFileQ = new RandomAccessFile(this.edgesFile,"rw");
            tempFileChannel = rTempFile.getChannel().position(0);
           temp = tempFileChannel.map(FileChannel.MapMode.READ_WRITE,0,this.edgesFileChannel.size());  //size of the original buffer
           P = rTempFileP.getChannel().position(0);
            Q = rTempFileQ.getChannel().position(0);
       }
        catch(IOException e){e.printStackTrace();return;}

        //debug
        //System.out.println("Bytes in file:" + this.BYTES_IN_FILE);


        while(subsetSize < this.BYTES_IN_FILE){//continue until the current size of groups to merge is the size of the whole file
            //System.out.println(this.BYTES_IN_FILE/subsetSize);
            //System.out.println("subsetSize: " + subsetSize + ", BytesinFile: " + BYTES_IN_FILE);
            //temp = tempFileChannel.map(FileChannel.MapMode.READ_WRITE,0,this.edgesFileChannel.size());  //size of the original buffer
            temp.position(0);
            //printData(40);
            for(int i = 0; i < Math.ceil((this.BYTES_IN_FILE/subsetSize)/2); i++){
                //System.out.println(this.BYTES_IN_FILE + ": " + subsetSize);

                //System.out.println("inner loop: " + currentIndex + 2*subsetSize);
                //System.out.println("P: "+currentIndex +", " + subsetSize);
                //System.out.println("Q: "+(currentIndex + subsetSize) +", " + subsetSize);
                PBuffer = P.map(FileChannel.MapMode.READ_WRITE,currentIndex,subsetSize);
                QBuffer = Q.map(FileChannel.MapMode.READ_WRITE,currentIndex+subsetSize, subsetSize);


                int x = PBuffer.getInt();
                int y = QBuffer.getInt();

                while(PBuffer.hasRemaining() && QBuffer.hasRemaining()){
                    //System.out.println(x+ " <--x, y-->"+y);

                    if (x < y){
                        int tempX = PBuffer.getInt();
                        assert((x != 0) && (tempX != 0));
                        temp.putInt(x);
                        temp.putInt(tempX);//put both the values for the edge
                        if(PBuffer.hasRemaining()){x = PBuffer.getInt();}
                        else{temp.putInt(y);}
                    }
                    else{
                        int tempY = QBuffer.getInt();
                        assert((y != 0)&&(tempY != 0));
                        temp.putInt(y);
                        temp.putInt(tempY);
                        if (QBuffer.hasRemaining()){y = QBuffer.getInt();}
                        else{temp.putInt(x);}
                    }
                }
                //Either P or Q has been emptied, so the other one with remaining elements should be dumped into Temp
                while (PBuffer.hasRemaining()){temp.putInt(PBuffer.getInt());}
                while(QBuffer.hasRemaining()){temp.putInt(QBuffer.getInt());}
                PBuffer.force();
                QBuffer.force();
                currentIndex += 2*subsetSize;

            }
            /**
             * Two cases: Data not yet sorted is less than 1 subset, or data left is more than 1 subset but less than 2
             */
            if((this.BYTES_IN_FILE - currentIndex) <= subsetSize){
                //less than one subset, should already be in order so we can just copy
                //tempFileChannel.transferFrom(P,tempFileChannel.position(),this.BYTES_IN_FILE-currentIndex);
                PBuffer = P.map(FileChannel.MapMode.READ_WRITE,currentIndex,this.BYTES_IN_FILE-currentIndex);
                while(PBuffer.hasRemaining()){temp.putInt(PBuffer.getInt());}


            }
            else{
                //more than 1 but less than 2, can do sort but with smaller Q buffer sizes
                //This code could be re-written so that it works in the lines above, and not having to be copy-pasted
                //this would mean changing the if/else so that it catches it above in the for-loop
                PBuffer = P.map(FileChannel.MapMode.READ_WRITE,currentIndex,subsetSize);
                QBuffer = Q.map(FileChannel.MapMode.READ_WRITE,currentIndex+subsetSize, this.BYTES_IN_FILE - currentIndex - subsetSize);


                int x = PBuffer.getInt();
                int y = QBuffer.getInt();

                while(PBuffer.hasRemaining() && QBuffer.hasRemaining()){
                    //System.out.println(x+ " <--x, y-->"+y);

                    if (x < y){
                        int tempX = PBuffer.getInt();
                        assert((x != 0) && (tempX != 0));
                        temp.putInt(x);
                        temp.putInt(tempX);//put both the values for the edge
                        if(PBuffer.hasRemaining()){x = PBuffer.getInt();}
                        else{temp.putInt(y);}
                    }
                    else{
                        int tempY = QBuffer.getInt();
                        assert((y != 0)&&(tempY != 0));
                        temp.putInt(y);
                        temp.putInt(tempY);
                        if (QBuffer.hasRemaining()){y = QBuffer.getInt();}
                        else{temp.putInt(x);}
                    }
                }
                while (PBuffer.hasRemaining()){temp.putInt(PBuffer.getInt());}
                while(QBuffer.hasRemaining()){temp.putInt(QBuffer.getInt());}
                PBuffer.force();
                QBuffer.force();
            }

            currentIndex = 0;
            subsetSize = subsetSize * 2; //merge done, repeat for larger subset sizes until all merges are done.
            //Everything from P and Q were merged into temp, now I need to setup these files to merge again
            temp.force();
            temp.position(0);
            tempFileChannel.position(0);
            this.edgesFileChannel.position(0);
            this.edgesFileChannel.transferFrom(tempFileChannel, 0, tempFileChannel.size());
            this.edgesFileChannel.force(true);

            rTempFile.seek(0);
            rTempFileP.seek(0);
            rTempFileQ.seek(0);
            P.position(0);
            Q.position(0);

        }

    //tempFile.delete();
    //save everything to new file.
    tempFileChannel.transferTo(0,this.edgesFileChannel.size(),this.edgesFileChannel);
}   }

