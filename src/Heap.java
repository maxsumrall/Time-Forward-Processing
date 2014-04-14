
	public class Heap {
		
		private int[] id;
		private int[] distance;
		private int size;
		private int N, p, l, r, t, min, i;
		private int[] ans;
		
		public Heap(int _N) {
			this.size = 0;
			this.N = _N;
			id = new int[N];
			distance = new int[N];
			ans = new int[2];
		}
		private void swap(int i, int j) {
			t = id[i];
			id[i] = id[j];
			id[j] = t;
			
			t = distance[i];
			distance[i] = distance[j];
			distance[j] = t;
		}
		public int[] minimum() {
			ans[0] = id[1];
			ans[1] = distance[1];
			
			return ans;
		}
		public int[] extractMin() {
			ans[0] = id[1];
			ans[1] = distance[1];
			
			id[1] = id[size];
			distance[1] = distance[size];
			--size;
			
			p = 1;
			while (true) {
				min = p;
				l = p << 1;
				r = l + 1;
				if (l <= size && id[l] < id[min])
					min = l;
				if (r <= size && id[r] < id[min])
					min = r;
				if (min == p) break;
				
				swap(p, min);
				p = min;
			}
			return ans;
		}
		public void insert(int[] x) {
			++size;
			id[size] = x[0];
			distance[size] = x[1];
			
			i = size;
			while (i > 1 && id[i >> 1] > id[i]) {
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