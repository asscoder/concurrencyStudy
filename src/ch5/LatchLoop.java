package ch5;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import static org.junit.Assert.*;

/** Use countdownlatch to ensure that:
 * - many threads do some processing
 * - then we process the combined results
 * - and continue the loop
 * 
 * 
 * Measure how long it takes for multiple threads to count to 100 concurrently and compare that to doing the same thing using a single thread
 * 
 * 
 * Interesting result: we have to go at pretty high duration for the multi threaded appraoch to be faster than the single threadedx
 */

public class LatchLoop {
	
	private class CountingThread extends Thread {
		private long total = 0;
		private final CountDownLatch startLatch;
		private final CountDownLatch exitLatch;
		private final int toCount;

		public CountingThread(final int toCount, final CountDownLatch startLatch, final CountDownLatch exitLatch) {
			this.startLatch = startLatch;
			this.exitLatch = exitLatch;
			this.toCount = toCount;
		}
		
		
		@Override
		public void run() {
			//Wait for start condition
			try {
				if (null != startLatch) startLatch.await();
			} catch (InterruptedException e) {
				return;
			}
			
			//Do stuff
			for (int i = 1; i <= toCount; ++i) {
				 total += i;
			 }
			
			//Notify about exit
			if (null != exitLatch) exitLatch.countDown();
		}
		
		public long getTotal() {
			return total;
		}
	}
	
	
	/** Returns the time in nanoseconds it took for the whole thing to finish. Returns null if sums were not correct */
	
	private Long countInParallel(final int toCount, final int threads) {
		
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch end = new CountDownLatch(threads);
		
		final CountingThread[] thrads = new CountingThread[threads];
		
		for (int i = 0; i < threads; ++i) {
			thrads[i] = new CountingThread(toCount, start, end);
			thrads[i].start();
		}
		
		long currentTime = System.nanoTime();
		
		start.countDown();
		
		try {
			end.await();
		}  catch (InterruptedException e) {
			
		}
		
		long endTime = System.nanoTime();
		
		//Check results
		for (CountingThread t : thrads) {
			if (t.getTotal() != ((long)toCount + 1) * toCount / 2) {
				return null;
			}
		}
		
		return endTime - currentTime;
		
	}
	

	
	private Long countSerially(final int toCount, final int threads) {
		
		final CountingThread[] thrads = new CountingThread[threads];
		
		for (int i = 0; i < threads; ++i) {
			thrads[i] = new CountingThread(toCount, null, null);
		}
		
		long currentTime = System.nanoTime();
		
		for (CountingThread t : thrads) {
			t.run();
		}
		
		long endTime = System.nanoTime();
		
		//Check results
		for (CountingThread t : thrads) {
			if (t.getTotal() != ((long)toCount + 1) * toCount / 2) {
				return null;
			}
		}
		
		return endTime - currentTime;
		
	}
	
	
	
	@Test
	public void test() {
		Long parallelNanos = countInParallel(2000000000, 8);
		
		Long sequentialNanos = countSerially(2000000000,  8);
		
		
		assertNotNull(parallelNanos);
		assertNotNull(sequentialNanos);
		
		DecimalFormat df = new DecimalFormat("0.000");
		
		System.out.println("Parallel = " + df.format(parallelNanos / 1000000.0) + "msecs, Sequential = " + df.format(sequentialNanos / 1000000.0) + "msecs");
		
		
	}

	
	
}
