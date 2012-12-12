package ch5;

import static common.Utils.p;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Work stealing based implementation. Each request goes to a random queue. When a queue discovers more work to do it adds it to itself.
 * When there is no more work to do it attempts to look in other queues for something to do.
 */

public class DirectoryScannerWorkStealing implements DirectoryScanner {
	private final AtomicInteger workToDo = new AtomicInteger(0);
	private final AtomicInteger[] dequesWork ;

	private final ProcessorDequeue[] processorDequeus;
	private final ExecutorService pool;
	
	private volatile boolean shouldTerminate = false; 

	private final ConcurrentSkipListSet<String> results = new ConcurrentSkipListSet<String>();
	
	public DirectoryScannerWorkStealing(int queues) {
		processorDequeus = new DirectoryScannerWorkStealing.ProcessorDequeue[queues];
		dequesWork = new AtomicInteger[queues];
		pool = Executors.newFixedThreadPool(queues);
		
		for (int i = 0; i < queues; ++i) {
			processorDequeus[i] = new ProcessorDequeue(i);
			dequesWork[i] = new AtomicInteger(0);
			pool.submit(processorDequeus[i]);
		}
	}

	
	private int getApproximateLeastWorkDequeue() {
		int min = Integer.MAX_VALUE;
		int minIdx = 0;
		
		for (int i = 0; i < dequesWork.length; ++i) {

			int tempWork = dequesWork[i].get();
			
			if (tempWork < min) {
				minIdx = i;
				min = tempWork;
			}
		}
		
		return minIdx;
	}
	
	private int getApproximateMaxWorkDequeue() {
		int max = -1;
		int maxIdx = 0;
		
		for (int i = 0; i < dequesWork.length; ++i) {

			int tempWork = dequesWork[i].get();
			
			if (tempWork > max) {
				maxIdx = i;
				max = tempWork;
			}
		}
		
		return maxIdx;
	}
	
	private void workSubmitted(int idx) {
		workToDo.incrementAndGet();
		dequesWork[idx].incrementAndGet();
	}
	
	private void workFinished(int idx) {
		workToDo.decrementAndGet();
		dequesWork[idx].decrementAndGet();
		
	}
	
	private void workMoved(int fromIdx, int toIdx) {
		dequesWork[fromIdx].decrementAndGet();
		dequesWork[toIdx].incrementAndGet();
		
	}
	
	
	@Override
	public void scanFile(String absolutePath) throws InterruptedException {
		int i = getApproximateLeastWorkDequeue();
		
		workSubmitted(i);
		processorDequeus[i].addWork(absolutePath);
	}

	@Override
	public boolean isFinished() {
		return workToDo.get() <= 0;
	}

	
	@Override
	public Collection<String> getScannedFiles() {
		return Collections.unmodifiableCollection(results);
	}

	//False if timeout
	@Override
	public void stop() {
		shouldTerminate = false;
		
		pool.shutdownNow();
		
		try {
			boolean ok = pool.awaitTermination(2, TimeUnit.SECONDS);;
			
			if (!ok) {
				p("Timeoud waiting for threads to die!");
			}
		} catch (InterruptedException e) {
			p("Interupted while waiting for threads to die!");
			Thread.interrupted();
			return;
		}
	}

	
	
	private  class ProcessorDequeue implements Runnable {
		private final int id;
		private final LinkedBlockingDeque<String> dequeue = new LinkedBlockingDeque<String>();

		public ProcessorDequeue(final int id) {
			this.id = id;
		}

		/** Adds a unit of work */
		public void addWork(String file)  throws InterruptedException  {
			dequeue.put(file);
		}
		
		/** Retrieves a unit of work or null if no work is available at the time */
		public String stealWork() {
			return dequeue.pollLast();
		}
		
		
		@Override
		public void run() {
			while (!shouldTerminate) {
				
				//Get work
				try {
					String work = dequeue.poll(1, TimeUnit.MILLISECONDS);
					
					if (null ==  work) { //look for other queues
						int i = getApproximateMaxWorkDequeue();
						
						work = processorDequeus[i].stealWork();
						
						if (null == work) continue;
						
						workMoved(i, id);
					}
					
					try {
						p(id + ": Picked " + work + " for indexing!");
						
						File file = new File(work);
	
						if (file.isFile()) {
							TimeUnit.MILLISECONDS.sleep(1000);
							results.add(Thread.currentThread().getName() + ": " + file.getCanonicalPath());
						} else if (file.isDirectory()) {
							File[] children = file.listFiles();
							
							for (File child : children) {
								workSubmitted(id);
								dequeue.add(child.getCanonicalPath());
							}
						}
					} finally {
						workFinished(id);
					}

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				} catch (IOException e) {
					p("Failed to process file!");
				}
					
			}
			
		}
		
	}

}
