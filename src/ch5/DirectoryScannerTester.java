package ch5;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static common.Utils.*;

public class DirectoryScannerTester {

	@Test 
	public void testProducerConsumer() throws Exception {
		DirectoryScanner scanner = new DirectoryScannerProducerConsumer(2,  10);
		
		scanner.scanFile("resources/ch5");
		
		while (!scanner.isFinished()) {
			TimeUnit.MILLISECONDS.sleep(100);
		}
		
		Collection<String> files = scanner.getScannedFiles();
		
		
		p("Scanned files: \n");
		
		for (String f : files) {
			p("\t " + f);
		}
	}
	

	@Test 
	public void testWorkStealing() throws Exception {
		DirectoryScanner scanner = new DirectoryScannerWorkStealing(10);
		
		scanner.scanFile("resources/ch5");
		
		while (!scanner.isFinished()) {
			TimeUnit.MILLISECONDS.sleep(10);
		}
		
		Collection<String> files = scanner.getScannedFiles();
		
		
		p("Scanned files: \n");
		
		for (String f : files) {
			p("\t " + f);
		}
	}

}
