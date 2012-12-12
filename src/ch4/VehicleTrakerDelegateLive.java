package ch4;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleTrakerDelegateLive implements VehicleTraker {

	private final ConcurrentHashMap<String, Point> positions = new ConcurrentHashMap<String, Point>();
	private final Map<String, Point> unmoadifiable = Collections.unmodifiableMap(positions);
	
	
	/** true if new */
	@Override
	public boolean updatePosition(String id, int x, int y) {
		return positions.put(id, new ImmutablePoint(x, y)) == null;
	}
	
	@Override
	public boolean removePosition(String id) {
		return positions.remove(id) != null;
		
	}
	
	@Override
	public Map<String, Point>getVehiclesInfo() {
		return unmoadifiable;
	}
	
	
	static class ImmutablePoint implements Cloneable, Point {
		private int x;
		private int y;
		
		public ImmutablePoint(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return x;
		}
		 
		public int getY() {
			return y;
		}
		
		@Override
		public ImmutablePoint clone() throws CloneNotSupportedException {
			return (ImmutablePoint)super.clone();
		}
		
	}

	
}


