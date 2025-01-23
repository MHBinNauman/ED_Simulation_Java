package Project;

public class Main {

	public static void main(String[] args) {
		// Create an instance of EDSimulation for a 3-hour simulation
		/*int simulationHours = 1;
        EDSimulation simulation = new EDSimulation(simulationHours * 10);
        
        // Initialize a thread for dynamically generating patients
        PatientGeneratorThread patientGeneratorThread1 = new PatientGeneratorThread(simulation);
        PatientGeneratorThread patientGeneratorThread2 = new PatientGeneratorThread(simulation);

		// Initialize a thread for processing patients from the file
        FilePatientThread filePatientThread = new FilePatientThread(simulation);

		 // Start both threads
		 patientGeneratorThread1.start();
		 patientGeneratorThread2.start();
		 filePatientThread.start();

		 // Wait for both threads to complete
		 try {
			 patientGeneratorThread1.join();
			 filePatientThread.join();
		 } 
		 catch (InterruptedException e) {
			 System.out.println("Simulation interrupted: " + e.getMessage());
		 }

		 // Collect and display statistics
		 simulation.collectStats();
		 System.out.println("Simulation completed successfully.");*/

    }
}