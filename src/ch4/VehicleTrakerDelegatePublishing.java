package ch4;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Same stuff, but also publishing internal state. For this it is necessary to ensure that the published objects (deep) are thread safe */

public class VehicleTrakerDelegatePublishing { 

	private final ConcurrentHashMap<String, SafePoint> positions = new ConcurrentHashMap<String, SafePoint>();
	private final Map<String, SafePoint> unmoadifiable = Collections.unmodifiableMap(positions);
	
	
	/** true if new */
	public boolean updatePosition(String id, int x, int y) {
		return positions.put(id, new SafePoint(x, y)) == null;
	}
	
	public boolean removePosition(String id) {
		return positions.remove(id) != null;
		
	}
	
	public Map<String, SafePoint> getVehiclesInfo() {
		return unmoadifiable;
	}
	
	
	static class SafePoint implements Cloneable {
		private final int[] xy = new int[2];;
		
		public SafePoint(int x, int y) {
			setXY(x, y);
		}
		
		public SafePoint(SafePoint point) {
			int[] xy = point.getXY();
			
			this.xy[0] = xy[0];
			this.xy[1] = xy[1];
			
		}
		
		public synchronized int[] getXY() {
			return Arrays.copyOf(xy, 2);
		}
			
		public final synchronized void setXY(int x, int y) {
			xy[0] = x;
			xy[1] = y;
		}
		
		@Override
		public synchronized SafePoint clone() throws CloneNotSupportedException {
			return (SafePoint)super.clone();
		}
		
	}
	
	
}


