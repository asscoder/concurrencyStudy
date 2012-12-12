package ch5;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static common.Utils.*;
import static org.junit.Assert.*;

public class InterruptionHandling {

	
	//Case 1 a: just throw
	public static void waitForSomeTime() throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(100);
	}
	
	//Case 1 b: process and throw
	public static void waitForSomeTime2() throws InterruptedException {
		try {
			TimeUnit.MILLISECONDS.sleep(100);
		} catch (InterruptedException e) {
			p("Interrupted");
			throw e;
		}
	}
	
	//Case 2: Process and restore status. When for some reason we don't expect to be able to throw the exception
	public static void waitUntilInterrupted()  {
		try {
			while (true) {
			TimeUnit.DAYS.	sleep(2);
			}
		} catch (InterruptedException e) {
			//Did something: broke the looop
			
			//Restore interrupted status so that caller nows
			Thread.currentThread().interrupt();
		}
	}
	
	
	//Case 3: Do nothing
	private static class MyThread extends Thread {
		@Override
		public void run() {
			try {
				waitForSomeTime();
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	
	@Test
	public void testJustThrow() {
		Thread.currentThread().interrupt();
		
		try {
			waitForSomeTime();
			fail();
		} catch (InterruptedException e) {
			//Interrrupted status should be reset to false noe
			assertFalse(Thread.interrupted());
		}
	}

	
	@Test
	public void testProcessAndThrow() {
		Thread.currentThread().interrupt();
		
		try {
			waitForSomeTime2();
			fail();
		} catch (InterruptedException e) {
			//Interrrupted status should be reset to false noe
			assertFalse(Thread.interrupted());
		}
	}

	@Test
	public void testRestoringStatus() {
		Thread.currentThread().interrupt();
		
		waitUntilInterrupted();
		
		assertTrue(Thread.interrupted());
		Thread.currentThread().interrupt();
		
		try {
			waitForSomeTime();
			fail();
		} catch (InterruptedException e) {
			//Interrrupted status should be reset to false noe
			assertFalse(Thread.interrupted());
		}
		
	}
	
	@Test 
	public void doNothing() {
		MyThread t = new MyThread();
		
		t.start();
		
		t.interrupt();
		
		try {
			t.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			fail();
		}
		

		
		
	}
	
	
}
