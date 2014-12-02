package cz.mzk.androidzoomifyviewer.cache;

public class CacheStatistics {
	private final long totalServingTime;
	private final int hitCount;
	private final int missCount;

	public CacheStatistics(long totalServingTime, int hitCount, int missCount) {
		super();
		this.totalServingTime = totalServingTime;
		this.hitCount = hitCount;
		this.missCount = missCount;
	}

	public int getRequests() {
		return hitCount + missCount;
	}

	public float getHitRatio() {
		int requests = getRequests();
		return ((float) hitCount) / requests;
	}

	public float getMissRatio() {
		int requests = getRequests();
		return missCount / (float) requests;
	}

	public float getAverageTime() {
		return totalServingTime / (float) getRequests();
	}
}
