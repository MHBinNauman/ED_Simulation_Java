package Project;

public class EDQ5 extends EDQueue{
	public EDQ5() {
		super();
	}
	
	// Add a patient to the patientArr
    public void addPatient(Patient patient) {
    	if (patient.getTriageCategory() == 5) {
    		patientArr.add(patient); 				// Add patient to the corresponding queue
    		System.out.println("Patient " +  patient.getPatientID() + " added to the Queue" );
    	}
        System.out.println("Patient " +  patient.getPatientID() + " belongs to Triage " + patient.getTriageCategory() + ".\nIncorrect Queue.");
    }
}
