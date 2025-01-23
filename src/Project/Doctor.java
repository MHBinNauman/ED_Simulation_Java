package Project;

import java.util.ArrayList;
import java.util.UUID;

public class Doctor extends Resources{
	
	// Extended patients for doctor class only
	private ArrayList<UUID> ext_patientAssigned;
	//Counter for counting patients
	private int patientCount;
	
	private UUID docID;
	private String level;
	
	// Default Constructor
	public Doctor() {
		super();
		this.ext_patientAssigned = new ArrayList<>();
		this.patientCount = 0;
	    this.docID = UUID.randomUUID();
	    this.level = "Undeclared";
	}
	
	// Getter for ID
	public final UUID getDocID() {
	    return this.docID;
	}
	
	// Getter for level
	public String getLevel() {
	    return level;
	}
	
	// Getter for Patient Count
	public final int getPatientCount() {
		return this.patientCount;
	}
	
	// Setter for level
	public void setLevel(String newLevel) {
	    this.level = newLevel;
	}
	
	public void releaseResource() {
		this.patientAssigned = null;
	}
	
	public String toString() {
	     if (this.patientAssigned == null) {
	         return "Doctor is available.";
	     } 
	     else {
	         return "Doctor is treating patient: " + this.patientAssigned;
	     }
	 }
	
	// Interns can deal a maximum of two patients
	public void AssignPatient(UUID patientID) {
		try {
			if (this.patientAssigned == null) {
		        this.patientAssigned = patientID;
		        this.patientCount++;
		    } 
			else {
				if (this.ext_patientAssigned.size() == 0) {
					this.ext_patientAssigned.add(patientID);
					this.patientCount++;
				}
				else {
					if (this.ext_patientAssigned.size() == 1 && this.level.equalsIgnoreCase("Intern")) {
						this.ext_patientAssigned.add(patientID);
						this.patientCount++;
					}
					else {
						if (this.ext_patientAssigned.size() == 2 && this.level.equalsIgnoreCase("Junior")) {
							this.ext_patientAssigned.add(patientID);
							this.patientCount++;
						}
						else {
							if (this.ext_patientAssigned.size() > 2  && this.level.equalsIgnoreCase("Consultant/Registrar")) {
								this.ext_patientAssigned.add(patientID);
								this.patientCount++;
							}
							else {
								throw new IllegalStateException("Patient Assignment Unsucessful");
							}
						}
					}
				}
		    }
		}
		catch(IllegalStateException e) {
			e.getMessage();
		}
	}
	
}
