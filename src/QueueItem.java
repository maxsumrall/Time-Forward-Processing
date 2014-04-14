
	final class QueueItem implements Comparable<QueueItem> {
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