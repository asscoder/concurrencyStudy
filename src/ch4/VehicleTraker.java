package ch4;

import java.util.Map;

public interface VehicleTraker {
	/** true if new */
	public boolean updatePosition(String id, int x, int y);

	public boolean removePosition(String id);

	public Map<String, Point> getVehiclesInfo();
		
	
}


