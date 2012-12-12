package ch4;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import common.GuardedBy;

public class VehicleTrakerSynchronized implements VehicleTraker {
	@GuardedBy("this.lock")
	private Map<String, Point> positions = new HashMap<String, Point>();
	
	private Object lock = new Object();
	
	/** true if new */
	@Override
	public boolean updatePosition(String id, int x, int y) {
		synchronized(lock) {
			MutablePoint point = (MutablePoint)positions.get(id);
			
			if (null == point) {
				point = new MutablePoint(x, y);
				positions.put(id,  point);
				return true;
			}
			
			point.setX(x);
			point.setY(y);
			
			return  false;
			
		}
		
	}
	
	@Override
	public boolean removePosition(String id) {
		synchronized(lock) {
			Point point = positions.get(id);
			
			if (null == point) return false;

			positions.remove(id);
			
			return true;
		}
		
	}
	
	@Override
	public Map<String, Point>getVehiclesInfo() {
		HashMap<String, Point> ret = new HashMap<String, Point>();
		
		synchronized(lock) {
			for (Map.Entry<String, Point> entry : positions.entrySet()) {
				try {
					ret.put(entry.getKey(), ((MutablePoint)entry.getValue()).clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return Collections.unmodifiableMap((Map<String, Point>)ret);
	}
	
	

	
	
	
	static class MutablePoint implements Cloneable, Point {
		private int x;
		private int y;
		
		public MutablePoint(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int getX() {
			return x;
		}
		 
		public int getY() {
			return y;
		}
		
		public void setX(int x) {
			this.x = x;
		}
		 
		public void setY(int y) {
			this.y = y;
		}
		
		@Override
		public MutablePoint clone() throws CloneNotSupportedException {
			return (MutablePoint)super.clone();
		}
		
	}

	
}


