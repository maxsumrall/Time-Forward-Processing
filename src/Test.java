import java.util.*;

public class Test {
	public static void main(String[] args) throws Exception {
		long initialTime = System.currentTimeMillis();
        int testNum = 0;
        
        switch (args[0]) {
        case "t1":
        	testNum = 1;
        	runTest1();
        	break;
        case "t2":
        	testNum = 2;
        	runTest2();
        	break;
        case "t3":
        	testNum = 3;
        	runTest3();
        	break;
        case "t4":
        	testNum = 4;
        	runTest4();
        	break;
        }
        
        System.out.println("Test #" + testNum + ": " + (System.currentTimeMillis() - initialTime) + " ms");
	}
	
	/**
	 * Test with random numbers and several arrays
	 * @throws Exception
	 */
	public static void runTest1() throws Exception {
		int B = 32;
		int N = 8000000;
		
		SuperArray[] buffers = new SuperArray[B];
        int[] counter = new int[B];
        
        for (int i = 0; i < B; ++i)
            buffers[i] = new SuperArray(4 * N);
        
        Random r = new Random();
        
        // List of non-empty buffers, when one becomes full, move it to the end and decrease list size
        int[] noEmpty = new int[B];
        int last = B - 1;
        for (int i = 0; i < B; ++i)
        	noEmpty[i] = i;
        
        int number = 42;
        
        for (int i = 0; i < B * N; ++i) {
        	// Random non-empty buffer
        	int ind = r.nextInt(last + 1);
        	int bufferIndex = noEmpty[ind];
        	// Add new number to selected buffer
        	buffers[bufferIndex].putInt(counter[bufferIndex], number);
        	++counter[bufferIndex];
        	
        	// If buffer becomes full, move to the end
        	if (counter[bufferIndex] >= N) {
        		int t = noEmpty[last];
        		noEmpty[last] = noEmpty[ind];
        		noEmpty[ind] = t;
        		--last;
        	}
        }
	}
	
	/**
	 * Test with no random numbers and several arrays
	 * @throws Exception
	 */
	public static void runTest2() throws Exception {
		int B = 32;
		int N = 8000000;
		
		SuperArray[] buffers = new SuperArray[B];
        int[] counter = new int[B];
        
        for (int i = 0; i < B; ++i)
            buffers[i] = new SuperArray(4 * N);

        int number = 42;
        
        for (int i = 0; i < B * N; ++i) {
        	int bufferIndex = i / N;
        	// Add new number to selected buffer
        	buffers[bufferIndex].putInt(counter[bufferIndex], number);
        	++counter[bufferIndex];
        }
	}
	
	/**
	 * Test with no random numbers and one big array
	 * @throws Exception
	 */
	public static void runTest3() throws Exception {
		int B = 32;
		int N = 8000000;
		
		SuperArray bigBuffer = new SuperArray(4 * B * N);
        int[] counter = new int[B];

        int number = 42;
        
        for (int i = 0; i < B * N; ++i) {
        	int bufferIndex = i / N;
        	// Add new number to selected buffer
        	bigBuffer.putInt(bufferIndex * N + counter[bufferIndex], number);
        	++counter[bufferIndex];
        }
	}
	
	/**
	 * Test with random numbers and one big array
	 * @throws Exception
	 */
	public static void runTest4() throws Exception {
		int B = 32;
		int N = 8000000;
		
		SuperArray bigBuffer = new SuperArray(4 * B * N);
        int[] counter = new int[B];

        Random r = new Random();
        
        // List of non-empty buffers, when one becomes full, move it to the end and decrease list size
        int[] noEmpty = new int[B];
        int last = B - 1;
        for (int i = 0; i < B; ++i)
        	noEmpty[i] = i;
        
        int number = 42;
        
        for (int i = 0; i < B * N; ++i) {
        	// Random non-empty buffer
        	int ind = r.nextInt(last + 1);
        	int bufferIndex = noEmpty[ind];
        	// Add new number to selected buffer
        	bigBuffer.putInt(bufferIndex * N + counter[bufferIndex], number);
        	++counter[bufferIndex];
        	
        	// If buffer becomes full, move to the end
        	if (counter[bufferIndex] >= N) {
        		int t = noEmpty[last];
        		noEmpty[last] = noEmpty[ind];
        		noEmpty[ind] = t;
        		--last;
        	}
        }
	}
}
