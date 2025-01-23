package Project;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class SimulationClock extends Clock {
	private final long real_world_Start_Time_ms;    // Real-world start time in milliseconds
    private final Instant simulationTime; 			// Start time of the simulation

    // Constructor
    public SimulationClock() {
        this.real_world_Start_Time_ms = System.currentTimeMillis(); 
        this.simulationTime = Instant.now();              
    }
	

	@Override
	public ZoneId getZone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instant instant() {
        long simulatedElapsedSeconds = getElapsedSimulatedSeconds();

        // Adding the elapsed time to start time of simulation and returning  the simulated time
        return simulationTime.plusSeconds(simulatedElapsedSeconds);
	}
	
	public long getElapsedSimulatedSeconds() {
	    long realElapsedMillis = System.currentTimeMillis() - this.real_world_Start_Time_ms;
	    return (realElapsedMillis / 1000) * 60; // Convert to simulated seconds
	}


}
