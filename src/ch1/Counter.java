package ch1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import common.GuardedBy;
import common.NotThreadSafe;

@NotThreadSafe
public class Counter {
//Wrong
//	private int i = 0;
//	
//	public int getNext() {
//		return ++i;
//	}
	

	//Still wrong
//	private volatile int i = 0;
//	
//	public int getNext() {
//		return ++i;
//	}

	
	//Correct
	@GuardedBy("this")
	private int i = 0;
	
	public synchronized int getNext() {
		return ++i;
	}

	/*--------------------------------------------------------------------
	 * TEST CODE
	 --------------------------------------------------------------------*/
	
	private static final int POOL_SIZE = 10;
	private static final int EXEC_SIZE = POOL_SIZE * 1000;
	
	public static void main(String[] args) throws Exception {
		ExecutorService pool =  Executors.newFixedThreadPool(POOL_SIZE);
		
		final Counter counter = new Counter();
		
		for (int i = 0; i < EXEC_SIZE; ++i) {
			pool.submit(new Runnable() {
				
				@Override
				public void run() {
					counter.getNext();
				}
			});
		}
		
		
		pool.shutdown();
		pool.awaitTermination(5, TimeUnit.SECONDS);
		
		int c = counter.getNext();
		
		System.out.println((c == EXEC_SIZE + 1 ? "Ok" : "Not OK") + ": " + c + ", Expected " + (EXEC_SIZE + 1));
		
		
	}
	
	
	
}
