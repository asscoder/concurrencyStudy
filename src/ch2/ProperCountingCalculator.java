package ch2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProperCountingCalculator {
	public static final ProperCountingCalculator INSTANCE = new ProperCountingCalculator();

	private int counter = 0;
	private int cacheHits = 0;
	private Double lastResult = null;
	private Double lastInput = null;
	
	
	private ProperCountingCalculator() { };
	
	
	public synchronized int getInvocations() {
		return counter;
	}
	
	public synchronized double getHitRatio() {
		return counter > 0 ? (double)cacheHits/counter : 0;
	}
	
	public double getResult(double input) {
		synchronized (this) {
			++counter;
			if (null != lastInput && lastInput.equals(input)) {
				++cacheHits;
				return lastResult;
			}
		}
		
		double result = expensiveOperation(input);
		
		synchronized(this) {
			lastInput=input;
			lastResult = result;

			return result;
		}
		
	}
	
	
	private double expensiveOperation(double input) {
		//try {
			for (int i =0; i < 10000000; ++i) { i++; i -= 1; i = (int)Math.floor(i + 0.00001); }
		//} catch (InterruptedException e) {
			
		//}
		return input / 3;
	}
	
	
	
	public static void main(String[] args) {
		ExecutorService pool = Executors.newFixedThreadPool(10);
		
		long time = System.currentTimeMillis();
		
		for (int i = 0; i < 50; ++i)  {
			pool.execute(new Runnable() {
				
				@Override
				public void run() {
					double in = Math.floor(Math.random() * 3);
					
					double result = ProperCountingCalculator.INSTANCE.getResult(in);
					
					if (Math.abs(result - in/3) > 0.00001) {
						System.out.println("ERROR found: " + in + " " + result);
					}
				}
			});
		}
		
	
		pool.shutdown();
		
		try {
			pool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			
		}
		
		
		System.out.println("Finished in " + (System.currentTimeMillis() - time) / 1000.0 + " seconds, " +
				           " had "  + INSTANCE.getInvocations() + " invocations and a ratio of " + INSTANCE.getHitRatio());
				
		
		
	}
	
	
}
