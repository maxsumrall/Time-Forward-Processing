import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class LongestPath {
	
	static final int FIELD_SIZE = 4; // 4 bytes

	static final class QueueItem implements Comparable<QueueItem> {
		int id, distance;
		public QueueItem(int id, int distance) {
			this.id = id;
			this.distance = distance;
		}
		@Override
		public int compareTo(QueueItem q) {
			return this.id - q.id;
		}
	}
	
	/**
	 * Non I/O efficient implementation of the dynamic programming algorithm.
	 * @param G: object representation of the graph
	 * @return
	 */
	public static int[] LongestPathDP(Graph G) {
		ArrayList<Vertex> topsort = TopologicalSorting.TopologicalSortBFS(G);
		
		int[] dist = new int[G.getSize()];
		for (Vertex v : topsort)
			for (Edge e : v.getEdges()) {
				Vertex w = e.getTo();
				dist[w.getId()] = Math.max(dist[w.getId()], dist[v.getId()] + 1);
			}
		
		return dist;
	}
	
	/**
	 * The graph is assumed to be in topological order.
	 * 
	 * @param G: topologically sorted graph represented with buffers.
	 */
    public static void IOLongestPathDP(IOGraph G) throws IOException {
		int N = G.getSize();
		RandomAccessFile raf = new RandomAccessFile(new File("outputDP.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);

        int e = 0;
		for (int i = 0; i < N; ++i) {
			for (int to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
				
				int distU = buffer.getInt(FIELD_SIZE * i);
				int distV = buffer.getInt(FIELD_SIZE * to);
				int newDist = Math.max(distV, distU + 1);
				buffer.putInt(FIELD_SIZE * to, newDist);
			}
            ++e;
		}

		fc.close();
		raf.close();
		
	}
    public static void IOLongestPathDPUnsafe(IOGraph G) throws Exception {
        int N = G.getSize();
        SuperArray longPath = new SuperArray((long)N);
        for (int i = 0; i < N; ++i)
            longPath.putInt(i,0);

        int e = 0;
        for (int i = 0; i < N; ++i) {
            for (int to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e)
                longPath.putInt(to, Math.max(longPath.getInt(i) + 1, (longPath.getInt(to)) ));
            ++e;
        }
        File outfile = new File("DPUnsafeOutput.dat");
        RandomAccessFile raf = new RandomAccessFile(outfile,"rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,0,N*4);
        for (int i = 0; i < N; i++){
            mbb.putInt(longPath.getInt(i));
        }
    }
	/*

    /**
	 * Non I/O efficient implementation of the time forward processing algorithm.
	 * It uses a MappedByteBuffer on top of a random access file.
	 *
	 * @param G: object representation of the graph.
	 * M: number of vertices per period.
	 */
	public static int[] LongestPathTimeForward(Graph G, int M) {
		ArrayList<Vertex> topsort = TopologicalSorting.TopologicalSortBFS(G);
		int N = G.getSize();

		int[] distance = new int[N];

		// Initialize list of "files"
		int B = (int)Math.ceil((double)N / M);
		ArrayList<ArrayList<QueueItem>> files = new ArrayList<ArrayList<QueueItem>>();
		for (int i = 0; i < B; ++i)
			files.add(new ArrayList<QueueItem>());

		int currentPeriod = -1;
		PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
		for (int i = 0; i < N; ++i) {
			Vertex u = topsort.get(i);
			// If this vertex if the first of a new period...
			if (u.getTime() % M == 0) {
				++currentPeriod;
				Q.clear();
				// Load all contents of file into the queue
				for (QueueItem x : files.get(currentPeriod))
					Q.offer(x);
			}

			// Process current vertex
			int maxDistance = 0;
			while (!Q.isEmpty()) {
				QueueItem top = Q.peek();
				if (top.id != u.getTime())
					break;
				Q.poll();
				maxDistance = Math.max(maxDistance, top.distance + 1);
			}

			distance[u.getId()] = maxDistance;

			// Put information of neighbors in data structure
			for (Edge e : u.getEdges()) {
				Vertex v = e.getTo();
				int period = v.getTime() / M;
				QueueItem newItem = new QueueItem(v.getId(), distance[u.getId()]);
				if (period == currentPeriod) {
					Q.offer(newItem);
				} else {
					files.get(period).add(newItem);
				}
			}
		}

		return distance;
	}

	/**
	 * I/O efficient implementation of the time forward processing algorithm.
	 * It uses a MappedByteBuffer on top of a random access file.
	 *
	 * @param G: topologically sorted graph represented with buffers.
	 * M: number of vertices per period.
	 */
	public static void IOLongestPathTimeForward(IOGraph G, int M) throws IOException {
		int N = G.getSize();

		// Buffer that stores the longest path lengths
		RandomAccessFile raf = new RandomAccessFile(new File("outputTF.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, FIELD_SIZE * N);

		int B = (int)Math.ceil((double)N / M);

		// Temporary random access file for the files corresponding to the periods.
		// The temporary files are in consecutive blocks
		File fileTf = new File("tf.tmp");
		RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
		FileChannel fcTf = raf.getChannel();

		MappedByteBuffer[] buffers = new MappedByteBuffer[B];
		int[] counter = new int[B]; // Counts how many edges per buffer

		int maxIndegree = 10;

		int nBytes = FIELD_SIZE * 2 * maxIndegree * M; // 4 bytes * <id, dist> * max_indegree * M
		for (int i = 0; i < B; ++i) {
			// starting position for each buffer in the file is i*nBytes
			buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
		}

		int currentPeriod = -1;

        PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
        for (int i = 0; i < N; ++i) {
            // If this vertex if the first of a new period...
            if (i % M == 0) {
                ++currentPeriod;
                Q.clear();
                // Load all contents of file into the queue
                MappedByteBuffer buf = buffers[currentPeriod];
                buf.position(0);
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    int id = buf.getInt();
                    int dist = buf.getInt();
                    Q.offer(new QueueItem(id, dist));
                }
            }

            // Process current vertex
            int maxDistance = 0;
            while (!Q.isEmpty()) {
                QueueItem top = Q.peek();
                if (top.id != i)
                    break;
                Q.poll();
                maxDistance = Math.max(maxDistance, top.distance + 1);
            }
            System.out.println(maxDistance);
            distBuffer.putInt(maxDistance);

            // Put information of neighbors in data structure
            int e = 0;
            for (int to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
                int period = to / M;
                QueueItem newItem = new QueueItem(to, maxDistance);
                if (period == currentPeriod) {
                    Q.offer(newItem);
                } else {
                    buffers[period].putInt(2 * FIELD_SIZE * counter[period], to);
                    buffers[period].putInt(2 * FIELD_SIZE * counter[period] + FIELD_SIZE, maxDistance);
                    ++counter[period];
                }
            }
            ++e;
        }
        fc.close();
		raf.close();
		fcTf.close();
		rafTf.close();
		
		if (fileTf.exists())
			fileTf.delete();
	}


    public static void IOLongestPathTimeForwardExperiment(IOGraph G, int M) throws Exception {
        int N = G.getSize();
        File outputTF = new File("outputTFUnsafe.dat");
        RandomAccessFile raf = new RandomAccessFile(outputTF, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);

        int B = (int)Math.ceil((double)N / M);

        File fileTf = new File("tf.tmp");
        //RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
        //FileChannel fcTf = raf.getChannel();

        SuperArray[] buffers = new SuperArray[B];
        int[] counter = new int[B]; // Counts how many edges per buffer

        int maxIndegree = 10;

        long nBytes = FIELD_SIZE * 2 * maxIndegree * M; // 4 bytes * <id, dist> * max_indegree * M
        for (int i = 0; i < B; ++i) {
            //buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
            //buffers[i] = new MappedFileBuffer(fileTf,0x8000000,true,nBytes);
            buffers[i] = new SuperArray(nBytes);
        }

        int currentPeriod = -1;
        /* Avoid many object creations*/
        int id;
        int t;
        int dist;
        int period;
        int d;
        int maxDistance = 0;

        PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
        for (int i = 0; i < N; ++i) {

            IOVertex u = G.getVertices().getVertexAt(i);

            if (u.getTime() % M == 0) {
                //if(currentPeriod%40 == 0){System.out.println(currentPeriod/(float)B + "%");}
                ++currentPeriod;
                Q.clear();
                if (currentPeriod >= 1){buffers[currentPeriod-1].discard();}
                SuperArray buf = buffers[currentPeriod];
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    id = buf.getInt();
                    t = buf.getInt();
                    dist = buf.getInt();
                    Q.offer(new QueueItem(id, dist));
                }
            }

            // Process current vertex
            maxDistance = 0;
            while (!Q.isEmpty()) {
                QueueItem top = Q.peek();
                if (top.id != u.getTime())
                    break;
                Q.poll();
                maxDistance = Math.max(maxDistance, top.distance + 1);
            }

            distBuffer.putInt(FIELD_SIZE * u.getId(), maxDistance);

            // Put information of neighbors in data structure
            for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
                IOVertex v = G.getVertices().getVertexAt(to);
                period = v.getTime() / M;
                d = distBuffer.getInt(FIELD_SIZE * u.getId());
                QueueItem newItem = new QueueItem(to, d);
                if (period == currentPeriod) {
                    Q.offer(newItem);
                } else {
                    buffers[period].putInt(to);
                    buffers[period].putInt(v.getTime());
                    buffers[period].putInt(d);
                    ++counter[period];
                }
            }
        }
        fc.close();
        raf.close();
       // fcTf.close();
       // rafTf.close();

        if (fileTf.exists())
            fileTf.delete();
    }


    public static void IOLongestPathTimeForwardNoVertices(IOGraph G, int M) throws Exception {
        int N = G.getSize();
        File outputTF = new File("outputTFNoV.dat");
        RandomAccessFile raf = new RandomAccessFile(outputTF, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);

        int B = (int)Math.ceil((double)N / M);

        File fileTf = new File("tf.tmp");
        //RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
        //FileChannel fcTf = raf.getChannel();

        SuperArray[] buffers = new SuperArray[B];
        int[] counter = new int[B]; // Counts how many edges per buffer

        int maxIndegree = 10;

        long nBytes = FIELD_SIZE * 3 * maxIndegree * M; // 4 bytes * <id, time, dist> * max_indegree * M
        for (int i = 0; i < B; ++i) {
            //buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
            //buffers[i] = new MappedFileBuffer(fileTf,0x8000000,true,nBytes);
            buffers[i] = new SuperArray(nBytes);
        }

        int currentPeriod = -1;
        /* Avoid many object creations*/
        int id;
        int t;
        int dist;
        int period;
        int d;
        int maxDistance = 0;

        PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
        for (int i = 0; i < N; ++i) {

            //IOVertex u = G.getVertices().getVertexAt(i);
            if (i % M == 0) {
                //if(currentPeriod%40 == 0){System.out.println(currentPeriod/(float)B + "%");}
                ++currentPeriod;
                Q.clear();
                if (currentPeriod >= 1){buffers[currentPeriod-1].discard();}
                SuperArray buf = buffers[currentPeriod];
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    id = buf.getInt();
                    t = buf.getInt();
                    dist = buf.getInt();
                    Q.offer(new QueueItem(id, dist));
                }
            }

            // Process current vertex
            maxDistance = 0;
            while (!Q.isEmpty()) {
                QueueItem top = Q.peek();
                if (top.id != i)
                    break;
                Q.poll();
                maxDistance = Math.max(maxDistance, top.distance + 1);
            }

            distBuffer.putInt(FIELD_SIZE * i, maxDistance);

            // Put information of neighbors in data structure
            for (int e = i, to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
                IOVertex v = G.getVertices().getVertexAt(to);
                period = v.getTime() / M;
                d = distBuffer.getInt(FIELD_SIZE * i);
                QueueItem newItem = new QueueItem(to, d);
                if (period == currentPeriod) {
                    Q.offer(newItem);
                } else {
                    buffers[period].putInt(to);
                    buffers[period].putInt(v.getTime());
                    buffers[period].putInt(d);
                    ++counter[period];
                }
            }
        }
        fc.close();
        raf.close();
        // fcTf.close();
        // rafTf.close();

        if (fileTf.exists())
            fileTf.delete();
    }





    public static void waterflowTFPIO(IOGraph G, int M) throws IOException {
        int N = G.getSize();

        // Buffer that stores the longest path lengths
        RandomAccessFile raf = new RandomAccessFile(new File("outputTF.dat"), "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, FIELD_SIZE * N);
        File waterTemp = new File("waterMagicTF.dat");
        RandomAccessFile rafEdges = new RandomAccessFile(waterTemp, "rw");
        FileChannel fcEdges = rafEdges.getChannel();
        MappedByteBuffer distWater = fcEdges.map(FileChannel.MapMode.READ_WRITE, 0, 3 * FIELD_SIZE * G.getEdges().size);

        int B = (int)Math.ceil((double)N / M) + 1;
        System.out.println(B);
        // Temporary random access file for the files corresponding to the periods.
        // The temporary files are in consecutive blocks
        File fileTf = new File("tf.tmp");
        RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
        FileChannel fcTf = raf.getChannel();

        MappedByteBuffer[] buffers = new MappedByteBuffer[B];
        int[] counter = new int[B]; // Counts how many edges per buffer

        int maxIndegree = 10;

        int nBytes = FIELD_SIZE * 3 * maxIndegree * M; // 4 bytes * <id, time, dist> * max_indegree * M
        for (int i = 0; i < B; ++i) {
            // starting position for each buffer in the file is i*nBytes
            buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
        }

        int currentPeriod = -1;

        PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
        for (int i = 0; i < N; ++i) {
            IOVertex u = G.getVertices().getVertexAt(i);

            // If this vertex if the first of a new period...
            if (u.getTime() % M == 0) {
                ++currentPeriod;
                System.out.println(u.getTime() + " " + currentPeriod);
                Q.clear();
                // Load all contents of file into the queue
                MappedByteBuffer buf = buffers[currentPeriod];
                buf.position(0);
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    int id = buf.getInt();
                    int t = buf.getInt();
                    int dist = buf.getInt();
                    Q.offer(new QueueItem(id, dist));
                }
            }

            // Process current vertex
            int maxDistance = 1000;
            while (!Q.isEmpty()) {
                QueueItem top = Q.peek();
                if (top.id != u.getTime())
                    break;
                Q.poll();
                maxDistance += top.distance;
            }

            distBuffer.putInt(FIELD_SIZE * u.getId(), maxDistance);

            // Put information of neighbors in data structure
            int magic = 0;
            for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
                magic++;
            }



            for (int e = u.getEdges(), to = 0; e >= 0 && (to = G.getEdges().getEdge(e)) != -1; ++e) {
                IOVertex v = G.getVertices().getVertexAt(to);
                int period = v.getTime() / M;
                int d = distBuffer.getInt(FIELD_SIZE * u.getId());
                QueueItem newItem = new QueueItem(to, d/magic);
                distWater.putInt(u.getId());
                distWater.putInt(to);
                distWater.putInt(d/magic);
                if (period == currentPeriod) {
                    Q.offer(newItem);
                } else {
                    buffers[period].putInt(3 * FIELD_SIZE * counter[period], to);
                    buffers[period].putInt(3 * FIELD_SIZE * counter[period] + FIELD_SIZE, v.getTime());
                    buffers[period].putInt(3 * FIELD_SIZE * counter[period] + 2 * FIELD_SIZE, d/magic);
                    ++counter[period];
                }
            }
        }
        File output = new File("water.txt");
        FileWriter fw = new FileWriter(output);

        distWater.position(0);
        while(distWater.hasRemaining()){
            String outline = (distWater.getInt()) + " " + (distWater.getInt()) + " " + distWater.getInt() + "\n";
            if(!outline.equals("0 0 0\n")){fw.write(outline);}
        }
        fw.close();

        fcEdges.close();
        rafEdges.close();
        waterTemp.delete();

        fc.close();
        raf.close();
        fcTf.close();
        rafTf.close();

        if (fileTf.exists())
            fileTf.delete();
    }


	/*
	public static void main(String[] args) throws IOException {
		int N = 50000;
        
        IOVertexBuffer vertices = new IOVertexBuffer(N, "vertices1.dat");
        for (int i = 0; i < N; ++i)
        	vertices.addVertex(new IOVertex(i, i, 10 * i, 10 * i, -1));
        
        try {
        	IOGraph graph = TopologicalSorting.IOTopologicalSortBFS(vertices, N);
        	System.out.println(graph.getVertices());
        	System.out.println(graph.getEdges());
        	
        	Graph G = new Graph(N);
        	for (int i = 0; i < N; ++i) {
        		IOVertex u = graph.getVertices().getVertexAt(i);
	        	for (int e = u.getEdges(), to = 0; e >= 0 && (to = graph.getEdges().getEdge(e)) != -1; ++e) {
	        		G.addEdge(u.getId(), to);
	        	}
        	}
        	
        	System.out.println("Regular DP:");
        	int[] dist = LongestPathDP(G);
        	for (int i = N - 20; i < N; ++i)
    			System.out.print(dist[i] + ", ");
        	System.out.println();
        	
        	System.out.println("IO DP:");
        	IOLongestPathDP(graph);
        	
        	System.out.println("Regular TF");
        	int[] dist2 = LongestPathTimeForward(G, 5);
        	for (int i = N - 20; i < N; ++i)
    			System.out.print(dist2[i] + ", ");
        	System.out.println();
        	
        	System.out.println("IO TF:");
        	IOLongestPathTimeForward(graph, 5);
        	
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    */
}
