package ch1;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import common.NotThreadSafe;

@NotThreadSafe
public class Singleton {

	//Not correct
//	private static Singleton instance = null;
//	
//	public static Singleton getInstance() {
//		if (null == instance) {
//			try {
//				TimeUnit.MILLISECONDS.sleep(10);
//			} catch (Exception e) {
//				
//			}
//			
//			instance = new Singleton();
//		}
//	
//		return instance;
//	}

		
//	//Correct 1
//	@GuardedBy("class")
//	private static Singleton instance = null;
//
//	public synchronized static Singleton getInstance() {
//			if (null == instance) {
//			try {
//				TimeUnit.MILLISECONDS.sleep(10);
//			} catch (Exception e) {
//				
//			}
//			
//			instance = new Singleton();
//		}
//		
//		return instance;
//	}

	
	//Correct 2
	private static AtomicReference<Singleton> instance = new AtomicReference<Singleton>();

	public synchronized static Singleton getInstance() {

		if (instance.get() == null) {
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (Exception e) {
			}
			
			Singleton temp = new Singleton();
			
			instance.compareAndSet(null,  temp);
			
		}
		
		return instance.get();
	}

	/*--------------------------------------------------------------------
	 * TEST CODE
	 --------------------------------------------------------------------*/
	
	private static final int POOL_SIZE = 1000;
	private static final int EXEC_SIZE = POOL_SIZE * 1;
	
	public static void main(String[] args) throws Exception {
		ExecutorService pool =  Executors.newFixedThreadPool(POOL_SIZE);
		
		final ConcurrentHashMap<String, Boolean> refs = new ConcurrentHashMap<String, Boolean>();
		
		for (int i = 0; i < EXEC_SIZE; ++i) {
			pool.submit(new Runnable() {
				
				@Override
				public void run() {
					refs.put(Singleton.getInstance().toString(), true);
				}
			});
		}
		
		
		pool.shutdown();
		pool.awaitTermination(5, TimeUnit.SECONDS);
		
		int c = refs.keySet().size();
		
		System.out.println((c == 1 ? "Ok" : "Not OK") + ": " + c + ", Expected 1");
		
		
	}
	
	
	
}
