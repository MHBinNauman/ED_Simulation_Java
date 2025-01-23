package Project;

import java.util.UUID;

public class Bed extends Resources{

	 private UUID bedID; 
	 private String bedType; 

	 public Bed() {
		 super();		// Sets patientAssigned to -1
		 
	     this.bedID = UUID.randomUUID(); 
	     this.bedType = ""; 

	 }
	 
	 public final UUID getBedID() {
		 return this.bedID;
	 }
	 
	 public void setBedType(String bType) {
		 this.bedType = bType;
	 }
	 
	 public String getBedType() {
		 return this.bedType;
	 }

	 public void releaseResource() {
	     this.patientAssigned = null; // Bed Available
	 }


	 public String toString() {
	     if (this.patientAssigned == null) {
	         return "Bed is available.";
	     } 
	     else {
	         return "Bed is occupied by patient: " + this.patientAssigned;
	     }
	 }
	 
	 public void AssignPatient(UUID patientID) {
		if (this.patientAssigned == null) {
	        this.patientAssigned = patientID;
	    } 
		else {
			System.out.println("Bed is not available.");
	    }
	}
}
