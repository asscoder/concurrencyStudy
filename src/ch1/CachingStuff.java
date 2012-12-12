package ch1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import common.GuardedBy;
import common.ThreadSafe;

@ThreadSafe
public class CachingStuff {
	@GuardedBy("this")
	Integer previousValue = null;

	@GuardedBy("this")
	String previousString = null;
	
	
	
	//Wrong
//	public String getIntegerString(int i) {
//		if (previousValue != null && i == previousValue) {
//			return previousString;
//		}
//		
//		String ret = assignStringToInteger(i);
//
//		previousValue = i;
//		previousString = ret;
//		
//		return ret;
//	}
	
	//Correct by slow
//	public synchronized String getIntegerString(int i) {
//		if (previousValue != null && i == previousValue) {
//			return previousString;
//		}
//		
//		String ret = assignStringToInteger(i);
//
//		previousValue = i;
//		previousString = ret;
//		
//		return ret;
//	}

	
	//Correct 
	public String getIntegerString(int i) {
		synchronized(this) {
			if (previousValue != null && i == previousValue) {
				return previousString;
			}
		}
		
		String ret = assignStringToInteger(i);

		synchronized(this) {
			previousValue = i;
			previousString = ret;
		}
		
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

		final CachingStuff stuff = new CachingStuff();
		
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
