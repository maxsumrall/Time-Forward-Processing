import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.PriorityQueue;

public class LongestPath {
	
	static final int FIELD_SIZE = 4; // 4 bytes
	
	/**
	 * Non I/O efficient implementation of the dynamic programming algorithm.
	 * @param G: object representation of the graph
	 * @return
	 */
	/*public static int[] LongestPathDP(Graph G) {
		ArrayList<Vertex> topsort = TopologicalSorting.TopologicalSortBFS(G);

		int[] dist = new int[G.getSize()];
		for (Vertex v : topsort)
			for (Edge e : v.getEdges()) {
				Vertex w = e.getTo();
				dist[w.getId()] = Math.max(dist[w.getId()], dist[v.getId()] + 1);
			}

		return dist;
	}
     */
	/**
	 * The graph is assumed to be in topological order.
	 * 
	 * @param G: topologically sorted graph represented with buffers.
	 */
    public static void IOLongestPathDP(IOGraph G) throws IOException {
		int N = G.getSize();
		RandomAccessFile raf = new RandomAccessFile(new File( N + "outputDP.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);
	    
	    buffer.position(0);
	    for (int i = 0; i < N; ++i)
	    	buffer.putInt(0);
	    buffer.position(0);

        int e = 0;
		for (int i = 0; i < N; ++i) {
			int distU = buffer.getInt(FIELD_SIZE * i);
			//System.out.println(i + ": " + distU);
			for (int to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
				int distV = buffer.getInt(FIELD_SIZE * to);
				int newDist = Math.max(distV, distU + 1);
				buffer.putInt(FIELD_SIZE * to, newDist);
			}
            ++e;
		}

		buffer.force();
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
        File outfile = new File(N+"DPUnsafeOutput.dat");
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
    */
	/**
	 * I/O efficient implementation of the time forward processing algorithm.
	 * It uses a MappedByteBuffer on top of a random access file.
	 *
	 * @param G: topologically sorted graph represented with buffers.
	 * M: number of vertices per period.
      */
	public static void IOLongestPathTimeForwardOriginal(IOGraph G, int M) throws IOException {
		int N = G.getSize();

		// Buffer that stores the longest path lengths
		RandomAccessFile raf = new RandomAccessFile(new File(N+"."+M+"."+"outputTF.dat"), "rw");
		FileChannel fc = raf.getChannel();
	    MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, FIELD_SIZE * N);
		int B = (int)Math.ceil((double)N / M);

		// Temporary random access file for the files corresponding to the periods.
		// The temporary files are in consecutive blocks
		File fileTf = new File("tf.tmp");
		RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
		FileChannel fcTf = rafTf.getChannel();

		MappedByteBuffer[] buffers = new MappedByteBuffer[B];
		int[] counter = new int[B]; // Counts how many edges per buffer

		int maxIndegree = 20;

		int nBytes = FIELD_SIZE * 2 * maxIndegree * M; // 4 bytes * <id, dist> * max_indegree * M
		for (int i = 0; i < B; ++i) {
			// starting position for each buffer in the file is i*nBytes
			buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
		}

		int currentPeriod = -1;
        int e = 0;
        PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
        for (int i = 0; i < N; ++i) {
            if (i % M == 0) {
                ++currentPeriod;
                assert (Q.isEmpty());
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
            //System.out.println(i + ": " + maxDistance);
            distBuffer.putInt(FIELD_SIZE * i, maxDistance);

            // Put information of neighbors in data structure
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
        distBuffer.force();
        fc.close();
		raf.close();
		fcTf.close();
		rafTf.close();
		
		if (fileTf.exists())
			fileTf.delete();
	}


    public static void IOLongestPathTimeForwardPQJava(IOGraph G, int M) throws Exception {
        int N = G.getSize();
        File outputTF = new File(N+"."+M+"."+"outputTF.dat");
        RandomAccessFile raf = new RandomAccessFile(outputTF, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);

        int B = (int)Math.ceil((double)N / M);

        File fileTf = new File("tf.tmp");
        RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
        FileChannel rafChannel = raf.getChannel();

        SuperArray[] buffers = new SuperArray[B];
        int[] counter = new int[B]; // Counts how many edges per buffer

        int maxIndegree = 20;

        long nBytes = FIELD_SIZE * 2 * maxIndegree * M; // 4 bytes * <id, dist> * max_indegree * M
        for (int i = 0; i < B; ++i) {
            buffers[i] = new SuperArray(nBytes);
        }

        int currentPeriod = -1;
        /* Avoid many object creations*/
        int id;
        int dist;
        int period;
        int maxDistance = 0;
        int to;
        QueueItem newItem;
        SuperArray buf;
        QueueItem top;

        int e = 0;
        PriorityQueue<QueueItem> Q = new PriorityQueue<QueueItem>();
        for (int i = 0; i < N; ++i) {
            if (i % M == 0) {
                ++currentPeriod;
                Q.clear();
                if (currentPeriod >= 1){buffers[currentPeriod-1].discard();}
                buf = buffers[currentPeriod];
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    id = buf.getInt(2 * k);
                    dist = buf.getInt((2 * k) + 1);
                   Q.offer(new QueueItem(id, dist));
                }
            }

            // Process current vertex
            maxDistance = 0;
            while (!Q.isEmpty()) {
                top = Q.peek();
                if (top.id != i)
                    break;
                Q.poll();
                maxDistance = Math.max(maxDistance, top.distance + 1);
            }

            distBuffer.putInt(FIELD_SIZE * i, maxDistance);

            // Put information of neighbors in data structure
            for (to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
                period = to / M;
                if (period == currentPeriod) {
                    newItem = new QueueItem(to, maxDistance);
                    Q.offer(newItem);
                } else {
                    buffers[period].putInt(2 * counter[period], to);
                    buffers[period].putInt(2 * counter[period] + 1, maxDistance);
                    ++counter[period];
                }
            }
            ++e;
        }
        fc.close();
        raf.close();
        //fcTf.close();
       // rafTf.close();

        if (fileTf.exists())
            fileTf.delete();
    }
    
    public static void IOLongestPathTimeForwardHeap(IOGraph G, int M) throws Exception {
        int N = G.getSize();
        File outputTF = new File(N+"."+M+"."+"outputTF.dat");
        RandomAccessFile raf = new RandomAccessFile(outputTF, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);

        int B = (int)Math.ceil((double)N / M);

        File fileTf = new File("tf.tmp");
        RandomAccessFile rafTf = new RandomAccessFile(fileTf, "rw");
        FileChannel rafChannel = raf.getChannel();

        SuperArray[] buffers = new SuperArray[B];
        int[] counter = new int[B]; // Counts how many edges per buffer

        int maxIndegree = 20;

        long nBytes = FIELD_SIZE * 2 * maxIndegree * M; // 4 bytes * <id, dist> * max_indegree * M
        for (int i = 0; i < B; ++i) {
            buffers[i] = new SuperArray(nBytes);
        }

        int currentPeriod = -1;
        /* Avoid many object creations*/
        int id;
        int dist;
        int period;
        int maxDistance = 0;
        int to;
        int[] newItem = new int[2];
        SuperArray buf;
        int[] top = new int[2];

        int e = 0;
        Heap Q = new Heap(maxIndegree * M + 5);
        for (int i = 0; i < N; ++i) {
            if (i % M == 0) {
                ++currentPeriod;
                Q.clear();
                //if (currentPeriod >= 1){buffers[currentPeriod-1].discard();}
                buf = buffers[currentPeriod];
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    id = buf.getInt(2 * k);
                    dist = buf.getInt((2 * k) + 1);
                    
                    newItem[0] = id;
                    newItem[1] = dist;
                   Q.insert(newItem);
                }
            }

            // Process current vertex
            maxDistance = 0;
            while (!Q.isEmpty()) {
                top = Q.minimum();
                if (top[0] != i)
                    break;
                Q.extractMin();
                maxDistance = Math.max(maxDistance, top[1] + 1);
            }

            distBuffer.putInt(FIELD_SIZE * i, maxDistance);

            // Put information of neighbors in data structure
            for (to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
                period = to / M;
                if (period == currentPeriod) {
                	newItem[0] = to;
                	newItem[1] = maxDistance;
                    Q.insert(newItem);
                } else {
                    buffers[period].putInt(2 * counter[period], to);
                    buffers[period].putInt(2 * counter[period] + 1, maxDistance);
                    ++counter[period];
                }
            }
            ++e;
        }
        fc.close();
        raf.close();
        //fcTf.close();
       // rafTf.close();

        if (fileTf.exists())
            fileTf.delete();
    }
    
    public static void IOLongestPathTimeForward(IOGraph G, int M) throws Exception {
        int N = G.getSize();
        
        File outputTF = new File(N+"."+M+"."+"outputTF.dat");
        RandomAccessFile raf = new RandomAccessFile(outputTF, "rw");
        FileChannel fc = raf.getChannel();
        MappedByteBuffer distBuffer = fc.map(FileChannel.MapMode.READ_WRITE,0, FIELD_SIZE * N);

        int B = (int)Math.ceil((double)N / M);

        int maxIndegree = 20;
        long periodSize = 2 * maxIndegree * M;
        long nBytes = FIELD_SIZE * periodSize; // 4 bytes * <id, dist> * max_indegree * M
        SuperArray bigBuffer = new SuperArray(B * nBytes);
        int[] counter = new int[B]; // Counts how many edges per buffer

        int currentPeriod = -1;
        /* Avoid many object creations*/
        int id, dist, period, maxDistance, to, distTo;

        int e = 0;
        for (int i = 0; i < N; ++i) {
            if (i % M == 0) {
                ++currentPeriod;
                for (int k = 0; k < counter[currentPeriod]; ++k) {
                    id = bigBuffer.getInt(currentPeriod * periodSize + 2 * k);
                    dist = bigBuffer.getInt(currentPeriod * periodSize + 2 * k + 1);
                    
                    distTo = distBuffer.getInt(FIELD_SIZE * id);
                    distBuffer.putInt(FIELD_SIZE * id, Math.max(distTo, dist + 1));
                }
            }
            
            maxDistance = distBuffer.getInt(FIELD_SIZE * i);
            
            // Put information of neighbors in data structure
            for (to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
                period = to / M;
                if (period == currentPeriod) {
                	distTo = distBuffer.getInt(FIELD_SIZE * to);
                	distBuffer.putInt(FIELD_SIZE * to, Math.max(distTo, maxDistance + 1));
                } else {
                    bigBuffer.putInt(period * periodSize + 2 * counter[period], to);
                    bigBuffer.putInt(period * periodSize + 2 * counter[period] + 1, maxDistance);
                    ++counter[period];
                }
            }
            ++e;
        }
        fc.close();
        raf.close();
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

        int maxIndegree = 20;

        int nBytes = FIELD_SIZE * 2 * maxIndegree * M; // 4 bytes * <id, dist> * max_indegree * M
        for (int i = 0; i < B; ++i) {
            // starting position for each buffer in the file is i*nBytes
            buffers[i] = fcTf.map(FileChannel.MapMode.READ_WRITE, i * nBytes, nBytes);
        }

        int currentPeriod = -1;

        int e = 0;
        int e2 = 0;
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
            int maxDistance = 1000;
            while (!Q.isEmpty()) {
                QueueItem top = Q.peek();
                if (top.id != i)
                    break;
                Q.poll();
                maxDistance += top.distance;
            }

            distBuffer.putInt(FIELD_SIZE * i, maxDistance);

            // Put information of neighbors in data structure
            int magic = 0;
            for (int to = 0; (to = G.getEdges().getEdge(e2)) != -1; ++e2) {
                magic++;
            }
            ++e2;


            for (int to = 0; (to = G.getEdges().getEdge(e)) != -1; ++e) {
                int period = to / M;
                QueueItem newItem = new QueueItem(to, maxDistance/magic);
                distWater.putInt(i);
                distWater.putInt(to);
                distWater.putInt(maxDistance/magic);
                if (period == currentPeriod) {
                    Q.offer(newItem);
                } else {
                    buffers[period].putInt(2 * FIELD_SIZE * counter[period], to);
                    buffers[period].putInt(2 * FIELD_SIZE * counter[period] + 1 * FIELD_SIZE, maxDistance/magic);
                    ++counter[period];
                }
            }
            ++e;
        }
        File output = new File("water.txt");
        FileWriter fw = new FileWriter(output);

        distWater.position(0);
        while(distWater.hasRemaining()){
            String outline = (distWater.getInt() + 1) + " " + (distWater.getInt() + 1) + " " + distWater.getInt() + "\n";
            if(!outline.equals("1 1 0 \n")){fw.write(outline);}
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
