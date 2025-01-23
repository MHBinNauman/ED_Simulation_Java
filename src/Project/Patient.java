package Project;

import java.io.*;  
import java.util.Random;
import java.util.UUID;
import java.lang.Math;
import java.time.Instant;

public class Patient {
	private UUID patientID;
	private String condition;
	private int triageCategory;
	private Instant arrivalTime;	// Simulation Time of creation of Patient Class
	private float waitingTime; 	 	// Time in seconds
	private float treatmentTime; 	// Time in seconds
	private UUID bedAssigned; 	 	// bedID
	private UUID doctorAssigned;  	// docID
	private float PDDT; 			// PDDT in seconds
	private Instant dischargeTime;	// dischargeTime = arrivalTime + waitingTime + treatmentTime + PDDT
	private boolean admissionStatus;
	
	// Partially Parameterized Constructor
	public Patient(UUID patientID, String condition, int triageCategory, boolean admissionStatus) {
		this.patientID = patientID;
		this.condition = condition;
		this.triageCategory = triageCategory;
		this.arrivalTime = null;
		this.waitingTime = -1;
		this.treatmentTime = -1;
		this.bedAssigned = null;
		this.doctorAssigned = null;
		this.admissionStatus = admissionStatus;
	}
	
	// Default Constructor
	public Patient() {
		this.patientID = UUID.randomUUID();
		this.condition = null;
		this.triageCategory = -1;
		this.arrivalTime = null;
		this.waitingTime = -1;
		this.treatmentTime = -1;
		this.bedAssigned = null;
		this.doctorAssigned = null;
		this.admissionStatus = false;
	}
	
	// getters and setters
	public final UUID getPatientID() {
		return this.patientID;
	}
	
	public final String getCondition() {
		return this.condition;
	}
	
	public final int getTriageCategory() {
		return this.triageCategory;
	}
	
	public final Instant getArrivalTime() {
		return this.arrivalTime;
	}
	
	public final float getWaitingTime() {
		return this.waitingTime;
	}
	
	public final float getTreatmentTime() {
		return this.treatmentTime;
	}
	
	public final UUID getBedAssigned() {
		return this.bedAssigned;
	}
	
	public final UUID getDoctorAssigned() {
		return this.doctorAssigned;
	}
	
	public final float getPDDT() {
		return this.PDDT;
	}
	
	public final Instant getDischargeTime() {
		return this.dischargeTime;
	}
	
	public final boolean getAdmissionStatus() {
		return this.admissionStatus;
	}
	
	// class methods
	// Determines Triage and Waiting Time
	public void determineTriage() {
		// Determining Vitals using Probability distribution
		Random rand = new Random();
		
		// Heart Rate:
	    int lowerBound = 20;        
	    int upperBound = 140;       
	    int x1 = rand.nextInt(40, 130);
	    
	    int HR = ((x1 - lowerBound) / (upperBound - lowerBound)) * 200;
	    
	    // Respiratory Rate:
	    lowerBound = 12;
	    upperBound = 20;
	    double x2 = rand.nextDouble(13, 17);
	    
	    double RR = (double)((x2 - lowerBound)/(upperBound - lowerBound))*40.0;
	    
	    // SpO2:
	    lowerBound = 70;     
	    upperBound = 100;      
	    double x3 = rand.nextDouble(89, 93);
	    
	    double SpO2 = (double)((x3 - lowerBound) / (upperBound - lowerBound)) * 130;
	    
	    // Blood Pressure:
	    lowerBound = 70;        
	    upperBound = 100;       
	    double x4 = rand.nextDouble(85, 95);
	    
	    double BP = (double)((x4 - lowerBound) / (upperBound - lowerBound)) * 150;
	    
	    // Temperature:
	    lowerBound = 70;        
	    upperBound = 100;       
	    double x5 = rand.nextDouble(80, 83);
	    
	    double T = ((x5 - lowerBound) / (upperBound - lowerBound)) * 100;
	    
	    // Pain Level:
	    int painLevel = rand.nextInt(0, 10);
	    
	    // Number of Resources
	    int resourcesNo = rand.nextInt(0, 5);
	    
		int triage = 0;
	    // Decision Point A
	    if (isImmediateLifeSavingIntervention(this.condition)) {
	        triage = 1;
	    } 
	    // Decision Point B
	    else if (isHighRiskSituation(painLevel, this.condition)) {
	        triage = 2;
	    }
	    // Decision Point D
	    else if (hasHighRiskVitalSigns(HR, RR, SpO2, BP) && (condition.equalsIgnoreCase("Temperature/Misc") || condition.equalsIgnoreCase("Pain") || condition.equalsIgnoreCase("Psychiatric"))) {
	        triage = rand.nextInt(2,4);
	    }
	    else if (T < 35.0 || T > 39.0) {
	        triage = 3;
	    }
	    // Decision Point C
	    else {
	    	triage = determineBasedOnResources(resourcesNo);
	    }
	    
	    this.triageCategory = triage;
	    
	    // Assign Waiting Time
	    determineWaitingTime();
	}
	
	public void determineWaitingTime() {
	    if (this.triageCategory == 1) {
	    	this.waitingTime = 60;			// 1 minute
	    }
	    else if (this.triageCategory == 2) {
	    	this.waitingTime = 600;			// 10 minutes
	    }
	    else if (this.triageCategory == 3) {
	    	this.waitingTime = 1800;		// 30 minutes
	    }
	    else if (this.triageCategory == 4) {
	    	this.waitingTime = 3600;		// 60 minutes
	    }
	    else {
	    	this.waitingTime = 7200;		// 120 minutes
	    }
	}
	
	// Method for Triage Determination
	private boolean isImmediateLifeSavingIntervention(String condition) {
		if (condition.equalsIgnoreCase("Temperature/Misc" )|| condition.equalsIgnoreCase("Pain") || condition.equalsIgnoreCase("Psychiatric")) {
			return false;
		}
		else {
			Random rand = new Random();
		    int x = rand.nextInt(0,3);
		    if (x == 0) {
		    	return true;
		    }
		    else {
		    	return false;
		    }
		}
	}
	
	// Method for Triage Determination
	private boolean isHighRiskSituation(int painLevel, String condition) {
		if (condition.equalsIgnoreCase("Temperature/Misc") || condition.equalsIgnoreCase("Pain") || condition.equalsIgnoreCase("Psychiatric")) {
			return false;
		}
		else {
			Random rand = new Random();
		    return (rand.nextBoolean() && painLevel >= 7);
		}
	}
	
	// Method for Triage Determination
	private boolean hasHighRiskVitalSigns(int HR, double RR, double SpO2, double BP) {
	    return (HR < 40 || HR > 140) || (BP < 90) || (RR < 12 || RR > 20) || (SpO2 < 90);
	}
	
	// Method for Triage Determination
	private int determineBasedOnResources(int resourcesNo) {
	    if (resourcesNo >= 2) { 
	    	return 3;
	    }
	    else if (resourcesNo == 1) {
	    	return 4;
	    }
	    else {
	    	return 5;
	    }
	}
	
	// Assign Patient the arrival time at which it enters the EDSimulation
	public void assignArrivalTime(SimulationClock sc) {
		this.arrivalTime = sc.instant();
	}
	
	// Determine admission status based upon Triage Category
	public void determineAdmissionStatus() {
		if(this.triageCategory >= 1 && this.triageCategory <= 3) {
			this.admissionStatus = true;
		}
		else {
			this.admissionStatus = false;
		}
	}
	
	// Determine treatment time
	public void determineTreatmentTime() {
		// Applying Pearson VI distribution
		Random rand = new Random();
		double x = rand.nextDouble(100, 150);
		double scale;
		float p, q;
		if (this.triageCategory == 1) {
			scale = 500;
			p = 1.5f;
			q = 5.0f;
		}
		else if (this.triageCategory == 2) {
			scale = 450;
			p = 1.7f;
			q = 5.3f;
		}
		else if (this.triageCategory == 3) {
			scale = 400;
			p = 1.8f;
			q = 5.5f;
		}
		else if (this.triageCategory == 4) {
			scale = 355;
			p = 1.64f;
			q = 5.72f;
		}
		else {
			scale = 200;
			p = 2.2f;
			q = 6.5f;
		}
		
		double intermediate = x/scale * 100;
		float numerator = (float)((Math.pow((double)(intermediate), (double)(p-1)))*1000000);
		float denominator = (float)(Math.pow((double)(1 + intermediate), (double)((p+q)-2)));
		this.treatmentTime = (numerator/denominator) * 100 * 60; // Treatment Time in seconds
	}
	
	public void determineDischargeTime() {
		Random rand = new Random();
		int x = rand.nextInt(100);
		int mean = 1;
		if (this.admissionStatus == true) {
			// PDDT
			if (this.triageCategory == 1) {
				mean = 240;
			}
			else if (this.triageCategory == 2) {
				mean = 190;
			}
			else { 
				mean = 156;
			}
			
			this.PDDT = (float)(((1.0 / mean) * Math.exp(-(x / (float) mean))) * 1000 *60);		// PDDT in Seconds
			long preDischargeTime = (long)(this.waitingTime + this.treatmentTime + this.PDDT);
			// Assign dischargeTime
		    if (this.dischargeTime == null) {
		        this.dischargeTime = this.arrivalTime.plusSeconds(preDischargeTime);
		    } else {
		        this.dischargeTime = this.dischargeTime.plusSeconds(preDischargeTime);
		    }
		}
		else {
			this.PDDT = 0;
		}
	}
	
	public void assignCondition() {
		// Assign condition randomly from a String [] filled with pre-decided conditions
		String [] Condition = {"Multitrauma", "Blood/Immune", "Cardiac/Vascular", "Diabetes/Endocrine", "Drug/Alcohol/Poisoning", "ENT", "Temperature/Misc", "Gastrointestinal", "Neurological", "Eye", "Gynaecology", "Paediatric", "Pain", "Psychiatric", "Respiratory", "Renal/Endocrine", "Urinary/Reproductive"};
		Random rand = new Random();
		int index = rand.nextInt(17);
		this.condition = Condition[index];
	}
	
	public void assignBed(UUID BedID) {
		this.bedAssigned = BedID;
	}
	
	public void assignDoc(UUID DocID) {
		this.doctorAssigned = DocID;
	}
	
	public void releaseBed() {
		this.bedAssigned = null;
	}
	
	public void releaseDoc() {
		this.doctorAssigned = null;
	}
	
	public void exportPatientRecord() {
		// Records.csv
		String filename = "Records.csv";
		String line = this.patientID + "," + this.condition + "," + this.triageCategory + "," + this.admissionStatus + "\n";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
			writer.append(line);
			writer.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
