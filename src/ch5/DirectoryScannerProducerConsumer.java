package ch5;

import static common.Utils.p;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryScannerProducerConsumer implements DirectoryScanner {

	private final ExecutorService producerPool;
	private final ExecutorService consumerPool;

	private final BlockingQueue<String> _indexingRequestsQ = new LinkedBlockingQueue<String>(); 
	private final BlockingQueue<String> _scanRequestsQ = new LinkedBlockingQueue<String>();
	
	private final Set<String> processedScannedFileRequests = new ConcurrentSkipListSet<String>();
	private final List<String> scannedFiles = new CopyOnWriteArrayList<String>();
	
	private final AtomicBoolean shouldTerminate = new AtomicBoolean(false);
	
	private final AtomicInteger workToBeDone = new AtomicInteger(0);
	
	public DirectoryScannerProducerConsumer(int producerThreads, int consumerThreads) {
		producerPool = Executors.newFixedThreadPool(producerThreads);
		consumerPool = Executors.newFixedThreadPool(consumerThreads);
		
		for (int i = 0; i < producerThreads; ++i) {
			producerPool.submit(new ProducerRunnable());
		}

		for (int i = 0; i < consumerThreads; ++i) {
			consumerPool.submit(new ConsumerRunnable());
		}

	}
	
	private final void addIndexingRequest(String s) {
		workToBeDone.incrementAndGet();

		_indexingRequestsQ.add(s);
	}
	
	private final void addScanningRequest(String s) {
		workToBeDone.incrementAndGet();
		
		_scanRequestsQ.add(s);
	}
	
	private final void finishedIndexingRequest() {
		int newWotkTOBeDone = workToBeDone.decrementAndGet();
		
		if (newWotkTOBeDone < 0) {
			p("ERROR: Work to be done cannot be negative: " + newWotkTOBeDone);
		}
	}
	
	private final void finishedScanningRequest() {
		int newWotkTOBeDone = workToBeDone.decrementAndGet();
		
		if (newWotkTOBeDone < 0) {
			p("ERROR: Work to be done cannot be negative: " + newWotkTOBeDone);
		}
		
	}
	
	
	
	private class ProducerRunnable implements Runnable {
		@Override
		public void run() {
			while (!Thread.interrupted() && !shouldTerminate.get()) {
				
				try {
					String indexingRequest = _indexingRequestsQ.take();
					
					p("Picked " + indexingRequest + " for indexing!");
					
					File file = new File(indexingRequest);

					if (processedScannedFileRequests.contains(file.getCanonicalPath())) {
						p("File " + file.getCanonicalPath() + " has be processed before. Ignoring!");
						continue;
					}
					
					processedScannedFileRequests.add(file.getCanonicalPath());

					if (file.isFile()) {
						addScanningRequest(file.getCanonicalPath());
					} else if (file.isDirectory()) {
						File[] children = file.listFiles();
						
						for (File child : children) {
							addIndexingRequest(child.getCanonicalPath());
						}
					}
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					p("Faile to process reqest: " + e.getMessage());
				} finally {
					finishedIndexingRequest();
				}
				
			}
		}
	}
	
	private class ConsumerRunnable implements Runnable {
		@Override
		public void run() {
			while (!Thread.interrupted() && !shouldTerminate.get()) {
				try {
					String scanRequest = _scanRequestsQ.take();
					
					p("Picked " + scanRequest + " for scanning!");
					TimeUnit.MILLISECONDS.sleep(1000);
					scannedFiles.add(Thread.currentThread().getName() + ": " + scanRequest);
					
					finishedScanningRequest();
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					p("Faile to process scan request: " + e.getMessage());
				}

			}
		}
		
	}
	
	@Override
	public void scanFile(String absolutePath) {
		addIndexingRequest(absolutePath);
	}

	@Override
	public Collection<String> getScannedFiles() {
		return scannedFiles;
	}

	@Override
	public boolean isFinished() {
		if (workToBeDone.get() < 0) {
			p("Work to be done cannot be negative: " + workToBeDone.get());
		}
		return workToBeDone.get() <= 0;
	}

	@Override
	public void stop() {
		consumerPool.shutdownNow();
		producerPool.shutdownNow();
		
		
		try {
			if (!consumerPool.awaitTermination(1,  TimeUnit.SECONDS)) {
				p("Consumer pool timed out");
			}
			if (!producerPool.awaitTermination(1,  TimeUnit.SECONDS)) {
				p("Producer pool timed out");
			}
		} catch (InterruptedException e) {
		}
	}
	
}
