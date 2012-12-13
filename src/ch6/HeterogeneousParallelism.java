package ch6;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeterogeneousParallelism {
	private final String MATCHER_PATTERN = "\\bRandom[0-9]*\\b";
	private final ExecutorService pool = Executors.newFixedThreadPool(1);
	
	public void renderPage(String inputString) throws MyError {
		
		//Get stuff stuff to replace
		List<StringLocation> locations = extractStringLocations(inputString);
		
		//Submit task for processing
		Future<List<String>> result = pool.submit(new StringRandomizerCallable(locations));
		
		result.cancel(true);
		
		//Do some other processing
		System.out.println("Input String: " + inputString);
		
		try {
			List<String> stringResults = result.get();

			for (String s : stringResults) {
				System.out.println("R: " + s);
			}
		
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			result.cancel(true); //ignore task, as no longer necessary
			return;
			
		} catch (ExecutionException e) { //error during execution
			if (e.getCause() instanceof MyError) {
				throw (MyError)e.getCause();
			} else if (e.getCause() != null){
				throw new MyError("Execution error: " + e.getCause().getMessage(), e.getCause());
			} else {
				throw new MyError("Execution error: " + e.getMessage(), e);
			}
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
	
	
	private static class StringRandomizerCallable implements Callable<List<String>> {
		List<StringLocation> locations;
		
		public StringRandomizerCallable(List<StringLocation> locations) {
			this.locations = locations;
		}
		
		@Override
		public List<String> call() throws Exception {
			List<String> ret = new LinkedList<String>();
			
			for (StringLocation location : locations) {
				ret.add(location.getStart() + ", " + location.getEnd()+ ": " + location.getText());
			}
			
			//throw new MyError("Sex");
			
//			Object o = 2.0; 
//			Integer i = (int)o;
			
			return ret;
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		try {
			HeterogeneousParallelism parallelism = new HeterogeneousParallelism();
			parallelism.renderPage("This is a Random1 string with some more Random2 stuff.");
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}