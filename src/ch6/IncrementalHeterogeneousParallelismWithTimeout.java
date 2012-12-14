package ch6;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Incremental rendedring of slow results using CompletionService */

public class IncrementalHeterogeneousParallelismWithTimeout {

	private final String MATCHER_PATTERN = "\\bRandom[0-9]*\\b";
	private final ExecutorService pool = Executors.newFixedThreadPool(5);

	public void renderPage(String inputString) throws MyError {
		
		//Get stuff stuff to replace
		List<StringLocation> locations = extractStringLocations(inputString);
	
		List<Callable<String>> tasks = new ArrayList<Callable<String>>(locations.size());
		
		for (StringLocation location : locations) {
			tasks.add(new StringRandomizerCallable(location));
		}

		//Do some other processing
		System.out.println("Input String: " + inputString);
		try {
			List<Future<String>> results = pool.invokeAll(tasks, 1, TimeUnit.SECONDS);
			
			
			
			for (Future<String> result : results) {
				try {
					if (result.isCancelled()) {
						System.out.println("Result cancelled");
					} else {
						System.out.println("R: " + result.get());
					}
				} catch (ExecutionException e) {
					if (e.getCause() instanceof MyError) {
						throw (MyError)e.getCause();
					} else if (e.getCause() != null){
						System.out.println("Execution error: " + e.getCause().getMessage());
					} else {
						System.out.println("Execution error: " + e.getMessage());
					}
				}
			}
		} catch (InterruptedException e) {
			//Restore intrrupted status and exit
			System.out.println("Interrupted!!!!");
			Thread.currentThread().interrupt();
			return;
		} finally {
			pool.shutdown(); //Important: Ohterwise, in case of interrupted exception the pool will never be stopped
		}
	}
	
	
	
	private List<StringLocation> extractStringLocations(final String inputString) throws MyError {
		
		List<StringLocation> ret = new LinkedList<StringLocation>();
		
		Pattern p = Pattern.compile(MATCHER_PATTERN);
		
		Matcher m = p.matcher(inputString);
		
		while (m.find()) {
			ret.add(new StringLocation(m.start(), m.end(), m.group()));
		}
		
		return ret;
	}
	
	public static class MyError extends Exception {

		public MyError() {
			super();
			// TODO Auto-generated constructor stub
		}

		public MyError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
			// TODO Auto-generated constructor stub
		}

		public MyError(String message, Throwable cause) {
			super(message, cause);
			// TODO Auto-generated constructor stub
		}

		public MyError(String message) {
			super(message);
			// TODO Auto-generated constructor stub
		}

		public MyError(Throwable cause) {
			super(cause);
			// TODO Auto-generated constructor stub
		}

	}
	
	private static class StringLocation {
		final int start;
		final int end;
		final String text;
		
		public StringLocation(int start, int end, String text) {
			this.start = start;
			this.end = end;
			this.text = text;
		}
		
		public int getEnd() {
			return end;
		}
		public int getStart() {
			return start;
		}
		public String getText() {
			return text;
		}
	
	}
	
	
	private static class StringRandomizerCallable implements Callable<String> {
		StringLocation location;
		
		public StringRandomizerCallable(StringLocation location) {
			this.location = location;
		}
		
		@Override
		public String call() throws Exception {

			System.out.println("\t\t" + location.getText() + ": Started");
			int random = (int)(Math.random() * 10);

			if (random == 1) {
				throw new Exception("I was going to be one and I don't like 1");
			}
			
			TimeUnit.MILLISECONDS.sleep(500);
			
			System.out.println("\t\t" + location.getText() + ": Finished");

			return location.getStart() + ", " + location.getEnd()+ ": " + location.getText() + " = " + random;
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		try {
			IncrementalHeterogeneousParallelismWithTimeout parallelism = new IncrementalHeterogeneousParallelismWithTimeout();
			parallelism.renderPage("This is a Random1 string with some more Random2 stuff. And more: " +
					"Random3 Random4 Random5 Random6 Random7 Random8 Random9 Random10 Random11");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}