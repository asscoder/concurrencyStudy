package ch5;

import static common.Utils.p;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import static org.junit.Assert.*;

/** Ensure that initialisation has been performed, before executing code */

public class Latch {
	private static final int LATCH_INIT_MILLIS = 500;
	
	private static class LateInit {
		private final CountDownLatch latch = new CountDownLatch(1);

		public void init() {
			try {
				TimeUnit.MILLISECONDS.sleep(LATCH_INIT_MILLIS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			latch.countDown();
		}
		
		public void doStuff() {
			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	@Test
	public  void testLateInit() throws Exception {
		final LateInit lateInit = new LateInit();
		final AtomicInteger minDelay = new AtomicInteger(Integer.MAX_VALUE);

		final AtomicInteger maxDelay = new AtomicInteger(0);
		
		final long startTime = System.currentTimeMillis(); 
				
		ExecutorService pool = Executors.newFixedThreadPool(10);
		
		for (int i = 0; i < 1000; ++i) {
			pool.submit(new Runnable() {
				@Override
				public void run() {
					p("Before doing stuff!");

					lateInit.doStuff();

					
					long delay = System.currentTimeMillis() - startTime;

					
					synchronized (minDelay) {
						if (minDelay.get() > delay) {
							minDelay.set((int)delay);
						}
						
						if (maxDelay.get() < delay) {
							maxDelay.set((int)delay);
						}
					}

					p("Done stuff!");
					
				}
			});
		}
		
		
		lateInit.init();
		
		
		pool.shutdown();
		assertTrue(pool.awaitTermination(100, TimeUnit.SECONDS));
		
		assertTrue("Expected minDelay to NOT be: " + minDelay.get(), minDelay.get() > LATCH_INIT_MILLIS && minDelay.get() < Integer.MAX_VALUE);
		
		p("Min delay=" + minDelay.get() + ", maxDelay=" + maxDelay);
		
		
		
		
		
		
		
		
		
	}
	
	
}
