package ch3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import common.ThreadSafe;

@ThreadSafe
public class VolatileCachingStuff {
	private volatile ImmutablePair<Integer, String>  previous = null;
	
	private class ImmutablePair<T, K> {
		private final T t;
		private final K k;
		
		public ImmutablePair (T t, K k) {
			this.t = t;
			this.k = k;
		}
		
		public T getT() { return t; }
		public K getK() { return k; }
		
	}
	
	//Wrong
	public String getIntegerString(int i) {
		ImmutablePair<Integer, String> prev = previous;
		
		if (prev != null && i == prev.getT()) {
			return prev.getK();
		}
		
		String ret = assignStringToInteger(i);

		previous = new ImmutablePair<Integer, String>(i,  ret);
		
		return ret;
	}
	
	
	private String assignStringToInteger(int i) {
		try {
			TimeUnit.MILLISECONDS.sleep(1);
		} catch (Exception e) {
			
		}
		return "This is integer " + i;
	}
	
	
	
	/*------------------------------------------------------------------------------------------------
	 * TEST CODE
	 ------------------------------------------------------------------------------------------------*/
	
	
	private static final int POOL_SIZE = 10;
	private static final int EXEC_SIZE = POOL_SIZE * 1000;
	
	private static volatile boolean error = false;
	
	public static void main(String[] args) throws Exception {

		final VolatileCachingStuff stuff = new VolatileCachingStuff();
		
		long startTime = System.currentTimeMillis();
		
		ExecutorService pool =  Executors.newFixedThreadPool(POOL_SIZE);
		
		for (int i = 0; i < EXEC_SIZE; ++i) {
			pool.submit(new Runnable() {
				
				@Override
				public void run() {
					int i = (int)Math.floor(Math.random() * 2);
					String reply = stuff.getIntegerString(i);
					
					if (!reply.equals("This is integer " + i)) {
						error = true;
					}
				}
			});
		}
		
		
		pool.shutdown();
		pool.awaitTermination(5, TimeUnit.SECONDS);
		
		
		double secs = (System.currentTimeMillis() - startTime) / 1000.0;
		
		System.out.println((!error  ? "Ok" : "Not OK") + " in  " + secs + "secs");
		
		
	}

	
}
