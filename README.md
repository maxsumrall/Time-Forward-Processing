# Time Forward Processing

In this project we will explore the power of time-forward processing. The idea of time-forward processing is the following: we process vertices of a directed acyclic graph at times t1,...,tn in topological order. So, if there is an edge (u,v) and u and v are processed at times tu, tv, respectively, then we have tu < tv. When we process a vertex, we put messages for its out- neighbours in a data structure, such that those messages come out at specified times in the future, namely when those out-neighbours will be processed. Thus, whenever we are about to process a vertex v, messages from all in-neighbours of v come out of the data structure right at that moment, and the information in those messages can be used to compute something about v.

Now, if the input is not too large, we can take the following approach. We divide the time into several periods T1,...,Tk, such that the number of messages that is received in each period is O(M). For each period Ti, we create an unordered file Fi in which we will store the messages from the past that need to be processed in period Ti. When we are about to enter a period Ti, we read all messages from the past from Fi and put them in an internal-memory priority queue. During period Ti, we maintain this priority queue for messages that need to come out during the same period, whereas messages for future periods are written to the respective files. If the number of periods is small enough and the memory is big enough, we can keep a buffer of one block in memory for each file, so that the writing is really efficient. In this project you will experiment with this approach and try to optimize it.

You will apply your implementation of time-forward processing to graphs similar to those that are used to model mountainous landscapes, to compute where large rivers will form when it starts raining. To be able to process these graphs, you will first need to put them into topological order.

## 2.2 Optimizing the delivery of messages
Input Each input for your time-forward processing algorithm will consist of a directed acyclic graph of which the vertices are numbered consecutively and in topological order from 0 to n − 1. Such an input is given as a file with one edge on each line. Each line contains two numbers: each number identifies a vertex; the edge is oriented from the first vertex to the second. The file is not necessarily sorted in any way. You may simply assume that the number of vertices n is the highest number in the file plus one.
In this setting, we can define the span of an edge as the difference between the identifiers of its destination and its origin. You should generate random inputs of various sizes and span distributions, to see how this affects the efficiency of your algorithms. Generating the input should be done with the following algorithm, which generates a graph in which the vertices have degree six on average.

5
In this algorithm, randomFraction is a function that returns a random number that is at least zero and less than 1. You should try different values of α. With α = 1/2 you get a completely random graph. For more smaller spans, lower α (but keep it larger than zero); for more larger spans, raise α. The largest generated files should have n ≥ 40 000 000. Instead of ASCII files, you may also produce binary files and use those as input.
Output The output of your time-forward processing algorithm should be a file of numbers that gives, for each vertex, in order, the length of the longest path in the graph that ends in that vertex. The output file can be a binary file.
Algorithms You should compare at least two algorithms:
• A na ̈ıve dynamic programming algorithm, in which you compute the longest path lengths vertex by vertex in topological order, simply reading the longest-path lengths of the in-neighbours from the binary output file.
• The time-forward processing algorithm as explained above.
You should fill in the details of the time-forward processing algorithm yourself.
The algorithm would probably run most efficiently if you make each period as long as possible, that is, each period should be just short enough so that the priority queue that is used during that period fits in memory. Try to find a way to preprocess the input so that you can choose optimal starting times of the periods. In addition to running times, your report should mention, for each input file, how many periods you needed to make.
You will also need to experiment with finding the best choice for how much memory you use for the priority queue and how much memory you use for buffers. Your report should describe the experiments you did to make these choices.
6
2.3 Graph landscapes
Input The input for your algorithm will be directed acyclic graphs that are also triangu- lations; each graph has exactly one vertex without any incoming edges. You will find such inputs of different sizes on pc2il75 in /scratch2/randomgraphs.The numbers of vertices and edges per test set are:
￼

Each graph comes in two text files. One file contains vertex information, one vertex per line, with on each line three integers separated by spaces: the vertex ID, the x-coordinate, and the y-coordinate. The other file contains edge information, one edge per line, with on each line the vertex ID of the tail (origin) of the edge, a space, and the vertex ID of the head (destination) of the edge. The graphs are provided as text files for technical reasons; you may want to convert these to binary files before running your algorithms. Figure 1 shows a small example; of course the graphs provided for experiments are much larger.
Output You should compute a representation of the path structure in a directed graph, somewhat similar to Figure 1. Of course, with graphs of millions of edges, you cannot show every single edge in the figure anymore, so you have to come up with some other representation that gives a good impression of the whole “landscape”.
Algorithms Your challenge is to find a way to process a graph of 50 million vertices (and almost 150 million edges) on pc2il75 with only 256 MB of main memory. You can do this with time-forward processing, but for that, you first need to sort and relabel the input graph into topological order.
To obtain a topological ordering, you could try the following approaches:
• Exploit the fact that the finishing times of vertices in a depth-first search constitute a reverse topological order of the graph (see also [1]).
• Make an I/O-efficient version of the following algorithm: initialize an array in which you store the out-degree of each vertex, and a list of vertices of out-degree zero; then repeat the following until the list is empty: remove a vertex v from the list and output v, lower
￼￼￼￼￼￼￼￼￼￼￼test50M... test100M... 100
50 000 000 ￼ ≈150 000 000 ￼ ≈300
￼7
￼Figure 1: Example of an input graph. The edges are directed, but the directions are not shown in the figure. The thickness of an edge e indicates, more or less, how many paths from the starting vertex u to another vertex v pass through e. More precisely: suppose it starts “raining” on this graph, and one unit of rain falls on every vertex. Think of the edges as being directed uphill; the water now starts flowing downhill along the edges, against the direction of the edges. In every vertex, the water that arrives from uphill is distributed evenly over the edges that come in from below. Thus we get a certain amount of water flowing along every edge; in the figure, the thickness of each edge is proportional to the logarithm of the amount of water.
8
the out-degree of all of in-neighbours of v by one, and add any in-neighbours of v that now have out-degree zero to the list.
Your algorithms should be I/O-efficient (in practice, not necessarily in the theoretical worst case) on graphs similar to the one that is given to you. Some ideas that may help:
• you may use the x- and y-coordinates of the vertices to organize them into well-shaped clusters;
• you may be able to maintain the out-degrees of all vertices on the boundaries of clusters in memory;
• you may be able to improve I/O-efficiency by always continuing to work on the same cluster as long as possible.
