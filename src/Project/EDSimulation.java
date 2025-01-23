package Project;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class EDSimulation {
	private double interArrivalTime;
	private ArrayList<Doctor> consul_regArr;	// 4 consultants and registrars
	private ArrayList<Doctor> seniorArr;		// 5 Senior Residents
	private ArrayList<Doctor> juniorArr;		// 8 Junior Residents
	private ArrayList<Doctor> internArr;		// 12 Interns
	private ArrayList<Bed> standardBedArr;		// 24 Standard Treatment Areas
	private ArrayList<Bed> corridorBedArr;		// 13 Corridor Areas
	private ArrayList<Bed> reclinerChair;		// 3 Recliner Chairs
	private ArrayList<Bed> inPatientBeds;		// Assigned Bed when Admission Status is true 
	private EDQ1 edq1;
	private EDQ2 edq2;
	private EDQ3 edq3;
	private EDQ4 edq4;
	private EDQ5 edq5;
	private Statistics Stats;
	private InputManager im;
	private SimulationClock sc;
	private int totalPatients;
	private int busyPeriodPatients;
	private int totalWaitingTime; // In seconds
	private BusyTime BT;
	
	// The Simulation starts when this class' constructor is called
	public EDSimulation(int hours) {
		this.interArrivalTime = -1;
		this.consul_regArr = new ArrayList<Doctor>();
		this.seniorArr = new ArrayList<Doctor>();
		this.juniorArr = new ArrayList<Doctor>();
		this.internArr = new ArrayList<Doctor>();
		this.standardBedArr = new ArrayList<Bed>();		// 24 Standard Beds
		this.corridorBedArr = new ArrayList<Bed>();		// 13 Corridor beds
		this.reclinerChair = new ArrayList<Bed>();		// 3 Recliner Chairs
		this.inPatientBeds = new ArrayList<Bed>();		// In-patient Beds
		this.edq1 = new EDQ1();
		this.edq2 = new EDQ2();
		this.edq3 = new EDQ3();
		this.edq4 = new EDQ4();
		this.edq5 = new EDQ5();
		Stats = new Statistics();
		im = new InputManager(hours);
		sc = new SimulationClock();
		this.totalPatients = 0;
		this.busyPeriodPatients = 0;
		this.totalWaitingTime = 0;
		this.BT = new BusyTime();
		
		// Initialize doctors
		this.initializeConsultant_RegistrarArray();
		this.initializeSeniorArray();
		this.initializeJuniorArray();
		this.initializeInternArray();
	}
	
	// Getter for Stats Class
	public Statistics getStats() {
		return this.Stats;
	}
	
	// Getters for metrics
	public int getTotalPatients() {
		return this.totalPatients;
	}
	
	public int getBusyPeriodPatients() {
		return this.busyPeriodPatients;
	}
	
	public int getTotalWaitingTime() {
		return this.totalWaitingTime;
	}
	
	public BusyTime getBusyTimes() {
		return this.BT;
	}
	
	// Getter for lengthOfRun
	public synchronized int getLengthOfRun() {
		return im.getLengthOfRun();
	}
	
	// Getter for Simulation Clock
	public synchronized SimulationClock getSimulationClock() {
		return sc;
	}
	
	// Generate Patients
	public synchronized Patient generatePatientsForSimulation() {
		// Run until simulation clock reaches the end
		Patient patient = new Patient();
			
		// Handle the arrival of the newly generated patient
		handlePatientArrivals(patient);
		// Increment the patient counter
		this.totalPatients++;
		
		return patient;
	}
	
	// Initializes Patients from File
	public synchronized ArrayList<Patient> initializePatientsFromFile() {
		ArrayList<Patient> patientsFromFile = new ArrayList<Patient>();
		im.getInput();
		UUID patientID = null;
		String[] patientRecord;
		String condition;
		int triageCategory;
		boolean admissionStatus;
		for (int i = 0; i < im.getRecords().size(); i++) {
			patientRecord = im.getRecords().get(i);
			patientID = UUID.fromString(patientRecord[0]);
			condition = patientRecord[1];
			triageCategory = Integer.parseInt(patientRecord[2]);
			admissionStatus = Boolean.parseBoolean(patientRecord[3]);
			Patient patient = new Patient(patientID, condition, triageCategory, admissionStatus);
			patientsFromFile.add(patient);
		}
		return patientsFromFile;
	}
	
	// Handles the patient's arrival into the EDQueue
	public synchronized void handlePatientArrivals(Patient patient) {
		this.totalPatients++;
		
		System.out.println("=== Patient Details ===");
		System.out.println("Patient ID: " + patient.getPatientID());
		
		// Assign Arrival Time
		patient.assignArrivalTime(sc);
		// Assign condition
		patient.assignCondition();
		// Assign Triage Category and waiting time
		patient.determineTriage();
		
		System.out.println("Condition: " + patient.getCondition());
		System.out.println("Triage Category: " + patient.getTriageCategory());
		System.out.println("Waiting Time: " + patient.getWaitingTime() + " seconds");
		
		this.totalWaitingTime += patient.getWaitingTime();
		
		// Adding to EDQueue
		int triageCategory = patient.getTriageCategory();
		/*Before adding patient to the EDQueue cross-check the inter-arrival time*/
		this.determineInterArrivalTime(patient);
		
		/*Hold the simulation for the inter-arrival time*/
		long timeElapsed = 0;
		if(patient.getArrivalTime() != null) {
			timeElapsed = Duration.between(patient.getArrivalTime(), sc.instant()).getSeconds();
		}
		if (timeElapsed < this.interArrivalTime) {
		    try {
		        // Calculate the remaining time to wait
		        long remainingTime = (long) (this.interArrivalTime - timeElapsed); 
		        Thread.sleep(remainingTime);
		    } 
		    catch (InterruptedException e) {
		        e.printStackTrace(); 
		    }
		}
		else {
			if (triageCategory == 1) {
				edq1.patientArr.add(patient);
			}
			else if (triageCategory == 2) {
				edq2.patientArr.add(patient);
			}
			else if (triageCategory == 3) {
				edq3.patientArr.add(patient);
			}
			else if (triageCategory == 4) {
				edq4.patientArr.add(patient);
			}
			else if (triageCategory == 5){
				edq5.patientArr.add(patient);
			}
			else {
				System.out.println("Invalid Triage Category: Unable to add patient to EDQueue.");
			}
		}
		
		// Assign Bed
		this.assignBed(patient);
		// Assign Doctor
		this.assignDoctor(patient);
		// Assign treatment time
		patient.determineTreatmentTime();
		// Assign admission status
		patient.determineAdmissionStatus();
		this.assignInPatientBed(patient);
		// Assign Post-Discharge Decision Time and the Discharge Time
		patient.determineDischargeTime();
		
		// Busy Time when all the standard beds are occupied
		if (this.standardBedArr.size() >= 24) {
			this.busyPeriodPatients++;
			this.BT.setTimeAndPatientCount(sc.instant(), busyPeriodPatients);
		}
		
		/*After assigning them Bed and Doctor release them from the EDQueue*/ 
		this.releasePatientFromEDQueue(patient);
		
		// Patient Details
		System.out.println("Treatment Time: " + patient.getTreatmentTime() + " seconds");
		System.out.println("Admission Status: " + patient.getAdmissionStatus());
		System.out.println("Assigned Bed: " + patient.getBedAssigned());
		System.out.println("Assigned Doctor: " + patient.getDoctorAssigned());
		System.out.println("Post-Discharge Decision Time: " + patient.getPDDT() + " seconds");
		System.out.println("Discharge Time: " + patient.getDischargeTime());
		System.out.println("=========================");

	}
	
	public synchronized void releasePatientFromEDQueue(Patient patient) {
	    int triageCategory = patient.getTriageCategory();

	    if(triageCategory == 1) {
	    	edq1.patientArr.remove(patient);
	    	System.out.println("Patient " + patient.getPatientID() + " released from EDQueue for triage category: " + triageCategory);
	    }
	    else if (triageCategory == 2) {
	    	edq2.patientArr.remove(patient);
	    	System.out.println("Patient " + patient.getPatientID() + " released from EDQueue for triage category: " + triageCategory);
	    }
	    else if (triageCategory == 3) {
	    	edq3.patientArr.remove(patient);
	    	System.out.println("Patient " + patient.getPatientID() + " released from EDQueue for triage category: " + triageCategory);
	    }
	    else if (triageCategory == 4) {
	    	edq4.patientArr.remove(patient);
	    	System.out.println("Patient " + patient.getPatientID() + " released from EDQueue for triage category: " + triageCategory);
	    }
	    else if (triageCategory == 5) {
	    	edq5.patientArr.remove(patient);
	    	System.out.println("Patient " + patient.getPatientID() + " released from EDQueue for triage category: " + triageCategory);
	    }
	    else {
	    	System.out.println("Invalid Triage Category: Unable to release patient from EDQueue.");
	    }
	}
	
	// Handle Patient's Departure when the patient's dischargeTime == sc.Instant() 
	// Release resources
	public synchronized void handlePatientDeparture(Patient patient) {
	    try {
	        // Finding the bed assigned to the patient
	        Bed bedToRelease = null;
	        for (Bed bed : standardBedArr) {
	            if (bed.getPatientAssigned() != null && bed.getPatientAssigned().equals(patient.getPatientID())) {
	                bedToRelease = bed;
	                break;
	            }
	        }
	        for (Bed bed : corridorBedArr) {
	            if (bed.getPatientAssigned() != null && bed.getPatientAssigned().equals(patient.getPatientID())) {
	                bedToRelease = bed;
	                break;
	            }
	        }
	        for (Bed bed : reclinerChair) {
	            if (bed.getPatientAssigned() != null && bed.getPatientAssigned().equals(patient.getPatientID())) {
	                bedToRelease = bed;
	                break;
	            }
	        }

	        // Releasing the bed assigned
	        if (bedToRelease != null) {
	        	patient.releaseBed();
	            bedToRelease.releaseResource();
	            System.out.println("Released bed for patient: " + patient.getPatientID());
	        } else {
	            System.out.println("No bed assigned to patient: " + patient.getPatientID());
	        }
	        
	        // Releasing In-patient Bed
	        this.releaseInPatientBed(patient); 

	        // Finding the doctor assigned to the patient
	        Doctor doctorToRelease = null;
	        for (Doctor doctor : consul_regArr) {
	            if (doctor.getPatientAssigned() != null && doctor.getPatientAssigned().equals(patient.getPatientID())) {
	                doctorToRelease = doctor;
	                break;
	            }
	        }
	        for (Doctor doctor : seniorArr) {
	            if (doctor.getPatientAssigned() != null && doctor.getPatientAssigned().equals(patient.getPatientID())) {
	                doctorToRelease = doctor;
	                break;
	            }
	        }
	        for (Doctor doctor : juniorArr) {
	            if (doctor.getPatientAssigned() != null && doctor.getPatientAssigned().equals(patient.getPatientID())) {
	                doctorToRelease = doctor;
	                break;
	            }
	        }
	        for (Doctor doctor : internArr) {
	            if (doctor.getPatientAssigned() != null && doctor.getPatientAssigned().equals(patient.getPatientID())) {
	                doctorToRelease = doctor;
	                break;
	            }
	        }
	        
	        // Releasing the doctor
	        if (doctorToRelease != null) {
	        	patient.releaseDoc();
	            doctorToRelease.releaseResource();
	            System.out.println("Released doctor for patient: " + patient.getPatientID());
	        } 
	        else {
	            System.out.println("No doctor assigned to patient: " + patient.getPatientID());
	        }
	    } 
	    catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	}

	
	
	// Assigning Bed
	public synchronized void assignBed(Patient patient) {
		/*Assign the first 24 standard beds first, if not available then assign the next 13 corridor stretchers
		  and then finally, assign the last 3 recliner chairs*/
		int triageCategory = patient.getTriageCategory();
		try {
			if (this.standardBedArr.size() < 24) {
				if (triageCategory == 1) {
					Bed resuscitationBed = new Bed();
					resuscitationBed.AssignPatient(patient.getPatientID());
					patient.assignBed(resuscitationBed.getBedID());
					resuscitationBed.setBedType("Resuscitation Bed");
					this.standardBedArr.add(resuscitationBed);
				}
				else if (triageCategory == 2) {
					Bed acuteBed = new Bed();
					acuteBed.AssignPatient(patient.getPatientID());
					patient.assignBed(acuteBed.getBedID());
					acuteBed.setBedType("Acute Bed");
					this.standardBedArr.add(acuteBed);
				}
				else if (triageCategory == 3) {
					Bed subacuteBed_3 = new Bed();
					subacuteBed_3.AssignPatient(patient.getPatientID());
					patient.assignBed(subacuteBed_3.getBedID());
					subacuteBed_3.setBedType("Sub-acute Bed");
					this.standardBedArr.add(subacuteBed_3);
				}
				else if (triageCategory == 4) {
					Bed subacuteBed_4 = new Bed();
					subacuteBed_4.AssignPatient(patient.getPatientID());
					patient.assignBed(subacuteBed_4.getBedID());
					subacuteBed_4.setBedType("Sub-acute Bed");
					this.standardBedArr.add(subacuteBed_4);
				}
				else if (triageCategory == 5) {
					Bed minorProcedureRoom = new Bed();
					minorProcedureRoom.AssignPatient(patient.getPatientID());
					patient.assignBed(minorProcedureRoom.getBedID());
					minorProcedureRoom.setBedType("Minor Procedure Room");
					this.standardBedArr.add(minorProcedureRoom);
				}
			}
			else if (this.corridorBedArr.size() < 13) {
				if (triageCategory == 1) {
					Bed resuscitationBed = new Bed();
					resuscitationBed.AssignPatient(patient.getPatientID());
					patient.assignBed(resuscitationBed.getBedID());
					resuscitationBed.setBedType("Resuscitation Bed");
					this.corridorBedArr.add(resuscitationBed);
				}
				else if (triageCategory == 2) {
					Bed acuteBed = new Bed();
					acuteBed.AssignPatient(patient.getPatientID());
					patient.assignBed(acuteBed.getBedID());
					acuteBed.setBedType("Acute Bed");
					this.corridorBedArr.add(acuteBed);
				}
				else if (triageCategory == 3) {
					Bed subacuteBed_3 = new Bed();
					subacuteBed_3.AssignPatient(patient.getPatientID());
					patient.assignBed(subacuteBed_3.getBedID());
					subacuteBed_3.setBedType("Sub-acute Bed");
					this.corridorBedArr.add(subacuteBed_3);
				}
				else if (triageCategory == 4) {
					Bed subacuteBed_4 = new Bed();
					subacuteBed_4.AssignPatient(patient.getPatientID());
					patient.assignBed(subacuteBed_4.getBedID());
					subacuteBed_4.setBedType("Sub-acute Bed");
					this.corridorBedArr.add(subacuteBed_4);
				}
				else if (triageCategory == 5) {
					Bed minorProcedureRoom = new Bed();
					minorProcedureRoom.AssignPatient(patient.getPatientID());
					patient.assignBed(minorProcedureRoom.getBedID());
					minorProcedureRoom.setBedType("Minor Procedure Room");
					this.corridorBedArr.add(minorProcedureRoom);
				}
			}
			else if (this.reclinerChair.size() < 3) {
				if (triageCategory == 1) {
					Bed resuscitationBed = new Bed();
					resuscitationBed.AssignPatient(patient.getPatientID());
					patient.assignBed(resuscitationBed.getBedID());
					resuscitationBed.setBedType("Resuscitation Bed");
					this.reclinerChair.add(resuscitationBed);
				}
				else if (triageCategory == 2) {
					Bed acuteBed = new Bed();
					acuteBed.AssignPatient(patient.getPatientID());
					patient.assignBed(acuteBed.getBedID());
					acuteBed.setBedType("Acute Bed");
					this.reclinerChair.add(acuteBed);
				}
				else if (triageCategory == 3) {
					Bed subacuteBed_3 = new Bed();
					subacuteBed_3.AssignPatient(patient.getPatientID());
					patient.assignBed(subacuteBed_3.getBedID());
					subacuteBed_3.setBedType("Sub-acute Bed");
					this.reclinerChair.add(subacuteBed_3);
				}
				else if (triageCategory == 4) {
					Bed subacuteBed_4 = new Bed();
					subacuteBed_4.AssignPatient(patient.getPatientID());
					patient.assignBed(subacuteBed_4.getBedID());
					subacuteBed_4.setBedType("Sub-acute Bed");
					this.reclinerChair.add(subacuteBed_4);
				}
				else if (triageCategory == 5) {
					Bed minorProcedureRoom = new Bed();
					minorProcedureRoom.AssignPatient(patient.getPatientID());
					patient.assignBed(minorProcedureRoom.getBedID());
					minorProcedureRoom.setBedType("Minor Procedure Room");
					this.reclinerChair.add(minorProcedureRoom);
				}
				else {
					throw new Exception("All beds are occupied. Please wait.");
				}
			}
			else {
				throw new Exception("ED out of Beds. Please Wait");
			}
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	// Assigning Doctor 
	public synchronized Doctor assignDoctor(Patient patient) {
		Doctor assignedDoctor = null;
	    try {
	        // Check for available doctors based on their levels and patient's triage category
	    	// Registrars and Consultants are always available for Category 1 and 2
	        if (patient.getTriageCategory() == 1 || patient.getTriageCategory() == 2) {
	            for (Doctor doctor : consul_regArr) {
	                assignedDoctor = doctor; 
	                break; 
	            }
	        } 
	        else if (patient.getTriageCategory() >= 3 && patient.getTriageCategory() <= 5) {
	            for (Doctor doctor : internArr) {
	                if (doctor.getPatientCount() < 2) { // Interns can treat a maximum of 2 patients
	                    assignedDoctor = doctor;
	                    break;
	                }
	            }
	            // If no intern is available, check junior residents
	            if (assignedDoctor == null) { 
	                for (Doctor doctor : juniorArr) {
	                    if (doctor.getPatientCount() < 3) { // Junior Residents can see a maximum of 3 patients
	                        assignedDoctor = doctor;
	                        break;
	                    }
	                }
	            }
	            // If no junior resident is available, check senior residents
	            if (assignedDoctor == null) {
	                for (Doctor doctor : seniorArr) {
	                    if (doctor.getPatientCount() < 4) { // Senior Residents can see a maximum of 4 patients
	                        assignedDoctor = doctor;
	                        break;
	                    }
	                }
	            }
	        }
	        
	        if (assignedDoctor != null) {
	            assignedDoctor.AssignPatient(patient.getPatientID());
	            patient.assignDoc(assignedDoctor.getDocID());
	            System.out.println("Assigned " + assignedDoctor.getLevel() + " to patient: " + patient.getPatientID());
	        } else {
	            throw new Exception("No available doctors for the patient's triage category.");
	        }

	    } 
	    catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
	    
	    return assignedDoctor;
	}

	public synchronized void assignInPatientBed(Patient patient) {
		if (patient.getAdmissionStatus()) {
			Bed inPatientBed = new Bed();
			inPatientBed.AssignPatient(patient.getPatientID());
			patient.assignBed(inPatientBed.getBedID());
			inPatientBed.setBedType("In-patient Bed");
			this.inPatientBeds.add(inPatientBed);
		}
		else {
			System.out.println("Cannot assign In-patient Bed if the patient is not admitted");
		}
	}
	
	public synchronized void releaseInPatientBed(Patient patient) {
		Bed bedToRelease = null;
		if (patient.getAdmissionStatus()) {
			for (Bed bed : this.inPatientBeds) {
	            if (bed.getPatientAssigned() != null && bed.getPatientAssigned().equals(patient.getPatientID())) {
	                bedToRelease = bed;
	                break;
	            }
	        }

	        // Releasing the In-patient bed assigned
	        if (bedToRelease != null) {
	        	patient.releaseBed();
	            bedToRelease.releaseResource();
	            System.out.println("Released bed for patient: " + patient.getPatientID());
	        } else {
	            System.out.println("No bed assigned to patient: " + patient.getPatientID());
	        }
			
		}
		else {
			System.out.println("Cannot release In-patient Bed, if the patient is not admitted");
		}
	}
	
	// Determine Inter-arrival Time using Weibull Distribution
	public synchronized void determineInterArrivalTime(Patient patient) {
		// Scale parameter = Beta (B) and Shape parameter = Alpha (a)
		double a, B;
		
		// Assigning Scale and Shape Parameters
		if (patient.getCondition().equalsIgnoreCase("Multitrauma")) {
		    B = 240;
		    a = 1.0;
		} else if (patient.getCondition().equalsIgnoreCase("Blood/Immune")) {
		    B = 120;
		    a = 1.1;
		} else if (patient.getCondition().equalsIgnoreCase("Cardiac/Vascular")) {
		    B = 200;
		    a = 0.9;
		} else if (patient.getCondition().equalsIgnoreCase("Diabetes/Endocrine")) {
		    B = 150;
		    a = 1.0;
		} else if (patient.getCondition().equalsIgnoreCase("Drug/Alcohol/Poisoning")) {
		    B = 100;
		    a = 1.2;
		} else if (patient.getCondition().equalsIgnoreCase("ENT")) {
		    B = 80;
		    a = 1.3;
		} else if (patient.getCondition().equalsIgnoreCase("Temperature/Misc")) {
		    B = 90;
		    a = 1.2;
		} else if (patient.getCondition().equalsIgnoreCase("Gastrointestinal")) {
		    B = 180;
		    a = 0.914;
		} else if (patient.getCondition().equalsIgnoreCase("Neurological")) {
		    B = 220;
		    a = 0.8;
		} else if (patient.getCondition().equalsIgnoreCase("Eye")) {
		    B = 70;
		    a = 1.4;
		} else if (patient.getCondition().equalsIgnoreCase("Gynaecology")) {
		    B = 140;
		    a = 1.0;
		} else if (patient.getCondition().equalsIgnoreCase("Paediatric")) {
		    B = 100;
		    a = 1.2;
		} else if (patient.getCondition().equalsIgnoreCase("Pain")) {
		    B = 80;
		    a = 1.3;
		} else if (patient.getCondition().equalsIgnoreCase("Psychiatric")) {
		    B = 160;
		    a = 1.0;
		} else if (patient.getCondition().equalsIgnoreCase("Respiratory")) {
		    B = 120;
		    a = 1.1;
		} else if (patient.getCondition().equalsIgnoreCase("Renal/Endocrine")) {
		    B = 200;
		    a = 0.9;
		} else if (patient.getCondition().equalsIgnoreCase("Urinary/Reproductive")) {
		    B = 150;
		    a = 1.0;
		} else {
		    // Default values if the condition does not match any case
		    B = 100;  // Default scale parameter
		    a = 1.0;  // Default shape parameter
		}
		
		// Generate Random Numbers
		Random rand = new Random();
		double x = rand.nextDouble(0, 2*B);
		
		double intermediate = x/B;
		this.interArrivalTime = (a/B)*(Math.pow((intermediate), (a-1)))*Math.exp(Math.pow((-intermediate), a));
	}
	
	private synchronized void initializeConsultant_RegistrarArray() {
		for (int i = 0; i < 5; i++) {
			Doctor c_r = new Doctor();
			c_r.setLevel("Consultant/Registrar");
			this.consul_regArr.add(c_r);
		}
	}
	
	private synchronized void initializeSeniorArray() {
		for (int i = 0; i < 5; i++) {
			Doctor senior = new Doctor();
			senior.setLevel("Senior");
			this.seniorArr.add(senior);
		}
	}
	
	private synchronized void initializeJuniorArray() {
		for (int i = 0; i < 8; i++) {
			Doctor junior = new Doctor();
			junior.setLevel("Junior");
			this.juniorArr.add(junior);
		}
	}
	
	private synchronized void initializeInternArray() {
		for (int i = 0; i < 12; i++) {
			Doctor intern = new Doctor();
			intern.setLevel("Intern");
			this.internArr.add(intern);
			// Under supervision of Senior 
			Doctor senior = new Doctor();
			senior.setLevel("Senior");
			this.seniorArr.add(senior);
		}
	}
	
	public synchronized void collectStats() {
		this.Stats.generateStatistics(this.totalPatients, this.totalWaitingTime, this.busyPeriodPatients, this.BT);
		// Print Statistics
		System.out.println("================== Statistics ==================");
	    System.out.println("Total Patients: " + this.totalPatients);
	    System.out.println("Total Waiting Time: " + this.totalWaitingTime);
	    System.out.println("Busy Period Patients: " + this.busyPeriodPatients);

	    if (this.BT != null) {
	        System.out.println("Busy Times: \n" + this.BT.toString());
	    } else {
	        System.out.println("Busy Times: No busy periods recorded.");
	    }
	    
	    System.out.println("==================================================");
	    
	    // Export Results
	    this.Stats.exportResults();
	}
}
