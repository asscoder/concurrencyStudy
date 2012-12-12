package ch3;

import java.util.concurrent.TimeUnit;

/** Only able to reproduce non visibility with non volatile variables. This is not easy to  reproduce */

public class NonVisibility {
	
	static class Modifier extends Thread {
		int value = 0;
		boolean ready = false;
		
		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (Exception e) {
				
			}
			value = 100;
			ready = true;
		}
		
		
	}
	

	public static void main(String [] args) {
		
		final int TESTS = 1000;
		int errors = 0;
		int count = 0;
		
		for (int i = 0; i < TESTS; ++i) {
			
			Modifier m = new Modifier();
			
			m.start();
			
			while (!m.ready) {
				for (int k = 0; k < 1; ++k);
			}
		
			++count;
			if (m.value != 100) {
				errors++;
			}
		}
		
		System.out.println((errors > 0 ? "Errors" : "OK") + ": " + errors + "/" + count + " errors");
		
		
	}
}
