package Project;

public class PatientGeneratorThread extends Thread{
	private EDSimulation Simulation;
	
	public PatientGeneratorThread(EDSimulation simulation) {
		this.Simulation = simulation;
	}
	
	public void run() {
		int counter = 0;
		Patient patient = null;
        // Calculate end time in simulated time
        while (counter < Simulation.getLengthOfRun()) {
        	
        	patient = Simulation.generatePatientsForSimulation();
        	
            try {
                // Simulate a short delay between patient generation to mimic real-time
                Thread.sleep(1000); // Adjust the delay as needed
            } 
            catch (InterruptedException e) {
                System.out.println("Patient generation interrupted: " + e.getMessage());
                break;
            }
            
            Simulation.handlePatientDeparture(patient);
            counter++;
        }
	}

}
