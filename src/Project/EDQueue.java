package Project;

import java.util.ArrayList;
import java.util.UUID;

public abstract class EDQueue {
    protected ArrayList<Patient> patientArr; 	// Array of lists to store addresses of Patient objects

    public EDQueue() {
        patientArr = new ArrayList<Patient>(); 	// Initialize patientArr as an ArrayList
    }
    
    // This function will be made abstract and overridden in EDQ1, EDQ2, EDQ3, EDQ4 and EDQ5
    public abstract void addPatient(Patient patient);

    // Get the patient from the Queue
    public Patient getPatient(UUID PatientID) {
    	Patient p;
        for (int i = 0; i < this.patientArr.size(); i++) {
           p = this.patientArr.get(i);
           if (p.getPatientID() == PatientID) {
        	   return p;
           }
           p = null;
        }
        System.out.println("All queues are empty. No patients to serve.");
        return null;
    }

    // Check if all queues are empty
    public boolean isEmpty() {
    	if (this.patientArr.isEmpty()) {
    		return true;
    	}
        return false;
    }
}
