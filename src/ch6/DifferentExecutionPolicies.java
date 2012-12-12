package ch6;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

public class DifferentExecutionPolicies {

	
	@Test
	public void testSequential() {
		long delayMillis = testExecutor(Executors.newSingleThreadExecutor(), 100);
		
		System.out.println("Single thread: "+  delayMillis);
		Assert.assertTrue("Delays is not in [400, 500]" + delayMillis, delayMillis > 400 && delayMillis < 500);
	}

	@Test
	public void testFixed() {
		long delayMillis = testExecutor(Executors.newFixedThreadPool(5), 500);
		
		System.out.println("Fixed threads: "+  delayMillis);
		Assert.assertTrue("Delays is not in [200, 300]" + delayMillis, delayMillis > 200 && delayMillis < 300);
	}

	
	@Test
	public void testCached() {
		long delayMillis = testExecutor(Executors.newCachedThreadPool(), 500);
		
		System.out.println ("Cached thread: "+  delayMillis);
		Assert.assertTrue("Delays is not in [0, 100]" + delayMillis, delayMillis > 0 && delayMillis < 100);
	}

	
	

	/** Submit ten tasks and see how they are executed */
	public static long testExecutor(ExecutorService service, final long tasksExecutionDelayMillis) {
		final long startTime = System.currentTimeMillis();
		final AtomicLong sumOfStartDelays = new AtomicLong(0);
		
		for (int i = 0; i < 10; ++i) {
			service.execute(new Runnable() {
				@Override
				public void run() {
					sumOfStartDelays.addAndGet(System.currentTimeMillis() - startTime);
					try {
						TimeUnit.MILLISECONDS.sleep(tasksExecutionDelayMillis);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			});
		}

		service.shutdown();
		try {
			boolean terminatedOk = service.awaitTermination(10, TimeUnit.SECONDS);
			
			Assert.assertTrue(terminatedOk);
		} catch (InterruptedException e) {
			Assert.fail("Inerrupted exception");
		}
		
		return sumOfStartDelays.get() / 10;
	}
	
}
