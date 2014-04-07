/**
 * Created by max on 3/13/14.
 */

public class IOEdge implements Comparable<IOEdge> {
    private int to = -1;
    private int ID = -1;

    public IOEdge(int ID, int to) {
        this.ID = ID;
        this.to = to;
    }

    public IOEdge clone() {
        return new IOEdge(this.ID, this.to);
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int compareTo(IOEdge e) {
        return this.ID - e.ID;
    }

    public String toString() {
        return this.ID + "," + this.to;
    }

}

