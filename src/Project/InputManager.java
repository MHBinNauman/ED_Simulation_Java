package Project;

import java.io.*;
import java.util.ArrayList;

public class InputManager {
    private int lengthOfRun; 				// Simulation duration in seconds
    private ArrayList<String[]> records; 	// Stores each line of the CSV file as an array of strings.

    // Constructor to initialize lengthOfRun and record list
    public InputManager(int hours) {
        this.lengthOfRun = hours; 	
        this.records = new ArrayList<String[]>(); 	// Initialize the records list
    }
    
    // Getter for records
    public final ArrayList<String[]> getRecords(){
    	return this.records;
    }
    
    // Getter for lengthOfRun
    public final int getLengthOfRun() {
        return lengthOfRun;
    }

    // Method to read data from Records.csv and store it line by line
    public void getInput() {
        String fileName = "Patients.csv"; 	// File name
        String line; 						// To store each line read from the file
        try {
        	File f = new File(fileName);
        	BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null) {
                // line is split for readability, comma delimiter
            	String [] data = line.split(",");
            	if (validateRecord(data)) {
            		records.add(data);
            	}
            }
            System.out.println("Data successfully fetched from CSV file.");
            br.close();
        } catch (IOException e) {
            System.err.println("Unable to locate CSV: " + e.getMessage());
        }
    }
    
    private boolean validateRecord(String [] data) {
		int triageCategory = Integer.parseInt(data[2]);
		if (data.length != 4) {
			return false;
		}
		else if (data[1].isBlank()) {														// Validating condition field
			return false;
		}
		else if (triageCategory < 1 || triageCategory > 5) {								// Validating Triage Category 
			return false;
		}
		else if (!data[3].equalsIgnoreCase("true") && !data[3].equalsIgnoreCase("false")) {	// Validating admissionStatus
			return false;
		}
		return true;
	}
}