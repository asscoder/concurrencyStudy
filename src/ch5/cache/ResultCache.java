package ch5.cache;

import java.nio.channels.CancelledKeyException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public abstract class ResultCache<K, V> {
	private final ConcurrentMap<K, FutureTask<V>> cache = new ConcurrentHashMap<K, FutureTask<V>>(); 
	
	
	public V get(final K k) throws Exception {
		
		FutureTask<V> future = cache.get(k);
		
		if (null == future) {
		
			future = new FutureTask<V>(new Callable<V>() {
				@Override
				public V call() throws Exception {
					return process(k);
				}
			});
			
			FutureTask<V> oldFuture = cache.putIfAbsent(k, future);
			
			if (null != oldFuture) {
				future = oldFuture;
			} else {
				future.run();
			}
		}
		
		try {
			return future.get();
		} catch (InterruptedException e) {
			cache.remove(k);
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		} catch (CancelledKeyException e) {
			cache.remove(k);
			throw new IllegalStateException(e);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException)e.getCause();
			} else if (e.getCause() instanceof Error) {
				throw (Error)e.getCause();
			} else throw (Exception)e.getCause();
		}
		
		
		
	}

	protected abstract V process(K k);
	
	
	
	
	public static void main(String[] args) throws Exception  {
		final long startTime = System.currentTimeMillis();
		
		final ResultCache<Integer, String> c = new ResultCache<Integer, String>() {
			@Override
			protected String process(Integer k) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				return Integer.toString(k);
			}
		};
		
		ExecutorService fixedTp = Executors.newFixedThreadPool(20);
		
		for (int i = 0; i < 100; ++i) {
			fixedTp.submit(new Runnable() {
				
				@Override
				public void run() {
					int i = (int)(Math.random() * 10);
					try {
						String result = c.get(i);
						System.out.println((System.currentTimeMillis() - startTime) + ": " + i + "=" + result);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			});
		}
		
		fixedTp.shutdown();
		fixedTp.awaitTermination(10, TimeUnit.SECONDS);
		
		
	}
	
}
