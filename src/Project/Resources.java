package Project;

import java.util.UUID;

public abstract class Resources {

	protected UUID patientAssigned; 		// patientID
	
	public Resources() {
		this.patientAssigned = null;		// No patient assigned // Resource available
	}

	public final UUID getPatientAssigned() {
		return patientAssigned;
	}
	
	public abstract String toString();
	public abstract void releaseResource();
	public abstract void AssignPatient(UUID patientID);	// Assigns Patient to Resources
	
}
