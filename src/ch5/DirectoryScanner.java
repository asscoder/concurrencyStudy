package ch5;

import java.util.Collection;

public interface DirectoryScanner {

	/** Async call */
	void scanFile(String absolutePath) throws InterruptedException ;

	/** Get currently scanned files */
	Collection<String> getScannedFiles();

	/** Will be accurate only if there is no parallel thread adding requests by calling scanFile() */
	boolean isFinished();
	
	void stop();
}
