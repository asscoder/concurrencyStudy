package ch5;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Test;


public class FutureTaskTest {


	@Test
	public  void test() {
		
		FutureTask<Integer> task = new FutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				TimeUnit.SECONDS.sleep(1);
				return 1;
			}
		});
		
		
		
		Thread t = new Thread(task);
		t.start();
		
		t.interrupt();
		

		
		
		try {
			System.out.println(task.get());
		} catch (CancellationException e) {
			System.out.println("Cancelled!");
		} catch (InterruptedException e) {
			System.out.println("Interrupted!");
		} catch (ExecutionException e) {
			e.getCause().printStackTrace();
		}
		
		
		
	}
	
}
