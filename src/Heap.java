
	public class Heap {
		
		private QueueItem[] A;
		private int size;
		private int N;
		
		public Heap(int _N) {
			this.size = 0;
			this.N = _N;
			A = new QueueItem[N];
		}
		private void swap(int i, int j) {
			QueueItem t = A[i];
			A[i] = A[j];
			A[j] = t;
		}
		public QueueItem minimum() {
			return A[1];
		}
		public QueueItem extractMin() {
			QueueItem ans = A[1];
			A[1] = A[size--];
			int p = 1;
			while (true) {
				int min = p;
				int l = p << 1;
				int r = l + 1;
				if (l <= size && A[l].id < A[min].id)
					min = l;
				if (r <= size && A[r].id < A[min].id)
					min = r;
				if (min == p) break;
				
				swap(p, min);
				p = min;
			}
			return ans;
		}
		public void insert(QueueItem x) {
			A[++size] = x;
			int i = size;
			while (i > 1 && A[i >> 1].id > A[i].id) {
				swap(i, i >> 1);
				i >>= 1;
			}
		}
		public void clear() {
			this.size = 0;
		}
		public int size() {
			return this.size;
		}
		public boolean isEmpty() {
			return size == 0;
		}
	}