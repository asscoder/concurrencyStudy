package ch6;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.junit.Test;
import static org.junit.Assert.*;

public class FutureRunnablePlay {

	@Test
	public void testDifferentFutures() throws Exception {
		
		Runnable runnable1 = new Runnable() {
			@Override
			public void run() {
				System.out.println("Runnable has run");
			}
		};

		Callable<Integer> callable1 = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				System.out.println("Callable has run");
				return 5;
			}
		};
		
		
		FutureTask<Void> t1 = new FutureTask<Void>(runnable1, null);
		
		FutureTask<Integer> t2 = new FutureTask<Integer>(runnable1, 5);
		
		FutureTask<Integer> t3 = new FutureTask<Integer>(callable1);
		
		assertFalse(t1.isDone());
		assertFalse(t2.isDone());
		assertFalse(t3.isDone());
		
		t1.run();
		t2.run();
		t3.run();

		
		assertTrue(t1.isDone());
		assertTrue(t2.isDone());
		assertTrue(t3.isDone());
		
		
		assertEquals(Integer.valueOf(5), t2.get());;
		assertEquals(Integer.valueOf(5), t3.get());;
		
		
		FutureTask<Integer> t4 = new FutureTask<Integer>(callable1);
		
		t4.cancel(false);
		
		
		assertTrue(t4.isDone());
		assertTrue(t4.isCancelled());
		
		
		assertEquals(Integer.valueOf(5), t3.get());;
		
		

	}
	
}
