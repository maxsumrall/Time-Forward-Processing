import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by max on 3/13/14.
 */
public class SortByDestination {
    int n;
    public SortByDestination(int n){
         this.n = n;
    }

    public void swap(File edgesFile) throws Exception{
        //Open this file, and swap the order of the edge ID and TO fields
        RandomAccessFile raFile = new RandomAccessFile(edgesFile,"rw");
        FileChannel fc = raFile.getChannel();
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE,0,fc.size());
        int ID;
        int TO;
        while(buffer.hasRemaining()){
            buffer.mark();
            ID = buffer.getInt();
            TO = buffer.getInt();
            buffer.reset();
            buffer.putInt(TO);
            buffer.putInt(ID);
        }
        buffer.force();
    }

    public void sort(File edgesFile,String fileName) throws Exception{
        this.swap(edgesFile);
        IOSort originSorter = new IOSort(edgesFile,n,"destinSorted");
        originSorter.sortSegments();
        originSorter.mergeSort();
        this.swap(new File("destinSorted"));
        new File("destinSorted").renameTo(new File(fileName + ".DestSorted"));

    }
}
