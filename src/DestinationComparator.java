import java.util.Comparator;

/**
 * Created by max on 3/22/14.
 */
class DestinationComparator implements Comparator<IOEdge> {
    public int compare(IOEdge a, IOEdge b) {
        return (a.getTo() - b.getTo());
    }
}
