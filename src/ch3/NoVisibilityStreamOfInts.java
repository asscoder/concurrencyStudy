package ch3;


/** Again: Only able to reproduce non visibility with non volatile variables. This is not easy to  reproduce */

public class NoVisibilityStreamOfInts {
	private static final int NUMBERS = 1000;
	
	static class Modifier extends Thread {
		int value = 0;
		volatile boolean ready = false;
		
		@Override
		public void run() {
			for (int i = 0; i < NUMBERS; ++i) {
				++value;
				ready = true;
				
				while (ready) {
					for (int k = 0; k < 1; ++k);
	
				}
			}
		}
	}
	

	public static void main(String [] args) {
		
		int errors = 0;
		int count = 0;
		
		Modifier m = new Modifier();
		m.start();
		try {
			for (int i = 1; i <= NUMBERS; ++i) {
				
				long time = System.currentTimeMillis();
				while (!m.ready ) {
					for (int k = 0; k < 1; ++k);
	
					if (System.currentTimeMillis() - time > 1000) {
						System.out.println("Timeout!!!");
						throw new Exception();
					}
				}
				
				count++;
				int value = m.value;
				
				if (value != i) {
					errors++;
				}
				//System.out.println(value);
				if (value >= NUMBERS) break;
				
				m.ready = false;
				
			}
		} catch (Exception e) {
			
		}
		
		System.out.println((errors > 0 ? "Errors" : "OK") + ": " + errors + "/" + count + " errors");
		
		System.exit(1);
	}
}
