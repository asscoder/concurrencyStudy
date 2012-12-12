package ch4;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ch4.VehicleTrakerDelegatePublishing.SafePoint;
import ch4.VehicleTrakerSynchronized.MutablePoint;

public class VehicleTrackerTester {
	
	@Test
	public void testSynchronized() throws Exception  {
		testImplementation(new VehicleTrakerSynchronized());
	}
	
	@Test
	public void testDelegate() throws Exception  {
		testImplementation(new VehicleTrakerDelegate());
	}

	@Test
	public void testDelegateLive() throws Exception  {
		testImplementation(new VehicleTrakerDelegateLive());
	}
	
	public void testImplementation(final VehicleTraker tracker) throws Exception {
		final int THREADS = 100;
		
		final AtomicInteger count = new AtomicInteger(0);
		final AtomicInteger errors = new AtomicInteger(0);
		
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		
		for (int i = 0; i < THREADS* 1000; ++i) {
			pool.submit(new Runnable() {
				
				@Override
				public void run() {
					//Check consistency
					Map<String, Point> points = tracker.getVehiclesInfo();

					try {
						checkConsistency(points);
					} catch (Exception e) {
						errors.incrementAndGet();
					}
					
					//Modify entries
					boolean add = (Math.random() * 2) >= 1;
					int x = (int)(Math.random() * 100);
					int y = x * 10 - 3;
					
					if (add) {
						int id = count.incrementAndGet();
						if (!tracker.updatePosition("Car " + id, x, y)) {
							count.decrementAndGet();
						}
					} else {
						String idToRemove = "Car " +  (1 + (int)(count.get() * Math.random()));
						boolean removed = tracker.removePosition(idToRemove);
						if (removed) {
							count.decrementAndGet();
						}
						
					}
					
					//Modify data
					for (Map.Entry<String, Point> entry : points.entrySet()) {
						if (entry.getValue() instanceof MutablePoint) {
							((MutablePoint)entry.getValue()).setY(1);
						}
					}
				}
				
			});
		}
		
		
		pool.shutdown();
		pool.awaitTermination(10,  TimeUnit.SECONDS);
		
		//Check consistency and count
		Map<String, Point> points = tracker.getVehiclesInfo();
		
		assertEquals(count.get(), points.size());
		
		checkConsistency(points);
		
		assertEquals(0, errors.get());
		
		System.out.println(points.size());
	}

	
	
	@Test
	public void testPublishing() throws Exception {
		
		final VehicleTrakerDelegatePublishing tracker = new VehicleTrakerDelegatePublishing();
		
		final int THREADS = 100;
		
		final AtomicInteger count = new AtomicInteger(0);
		final AtomicInteger errors = new AtomicInteger(0);
		
		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		
		for (int i = 0; i < THREADS* 1000; ++i) {
			pool.submit(new Runnable() {
				
				@Override
				public void run() {
					//Check consistency
					Map<String, SafePoint> points = tracker.getVehiclesInfo();

					try {
						checkConsistencySafe(points);
					} catch (Exception e) {
						errors.incrementAndGet();
					}
					
					//Modify entries
					boolean add = (Math.random() * 2) >= 1;
					int x = (int)(Math.random() * 100);
					int y = x * 10 - 3;
					
					if (add) {
						int id = count.incrementAndGet();
						if (!tracker.updatePosition("Car " + id, x, y)) {
							count.decrementAndGet();
						}
					} else {
						String idToRemove = "Car " +  (1 + (int)(count.get() * Math.random()));
						boolean removed = tracker.removePosition(idToRemove);
						if (removed) {
							count.decrementAndGet();
						}
						
					}
					
					//Modify data
					for (Map.Entry<String, SafePoint> entry : points.entrySet()) {
						if (entry.getValue() instanceof SafePoint) {
							((SafePoint)entry.getValue()).setXY(1, 7);
						}
					}
				}
				
			});
		}
		
		
		pool.shutdown();
		pool.awaitTermination(10,  TimeUnit.SECONDS);
		
		//Check consistency and count
		Map<String, SafePoint> points = tracker.getVehiclesInfo();
		
		assertEquals(count.get(), points.size());
		
		checkConsistencySafe(points);
		
		assertEquals(0, errors.get());
		
		System.out.println(points.size());
	}

	
	
	private static final void checkConsistency(Map<String, Point> points) {
		for (Map.Entry<String, Point> entry : points.entrySet()) {
			assertEquals(entry.getValue().getY(), entry.getValue().getX() * 10 - 3);
		}
		
	}

	private static final void checkConsistencySafe(Map<String, SafePoint> points) {
		for (Map.Entry<String, SafePoint> entry : points.entrySet()) {
			int[] xy = entry.getValue().getXY();
			assertEquals(xy[1], xy[0] * 10 - 3);
		}
		
	}

}
