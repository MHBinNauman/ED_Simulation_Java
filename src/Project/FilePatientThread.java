package Project;

import java.util.ArrayList;

public class FilePatientThread extends Thread{
	
	private EDSimulation Simulation;
	
	public FilePatientThread(EDSimulation simulation) {
		this.Simulation = simulation;
	}
	
	public void run() {
		ArrayList<Patient> filePatients = Simulation.initializePatientsFromFile();
		int counter = 0;
		
		while(counter < Simulation.getLengthOfRun()) {
			for (Patient patient : filePatients) {
				Simulation.handlePatientArrivals(patient);
				try {
	                Thread.sleep(1000); // Simulate time between patient generations
	            } 
				catch (InterruptedException e) {
	                System.out.println("Patient generation interrupted: " + e.getMessage());
	                break;
	            }
				counter++;
			}
			
			// All the patients arrive and then departure under this thread unlike the other one to ensure a randomness an overflow 
			
		    // Handle departures for file patients
			for (Patient patient : filePatients) {
				Simulation.handlePatientDeparture(patient);
			}
		}
	}
}
