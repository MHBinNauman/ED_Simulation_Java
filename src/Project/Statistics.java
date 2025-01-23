package Project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;

public class Statistics {

    private int avgWaitingTime;
    private int bedUtilization;	 	// Bed utilization percentage
    private BusyTime busierTimes;

    // Default Constructor
    public Statistics() {
        avgWaitingTime = 0;
        bedUtilization = 0;
        busierTimes = null;
    }
    
    // Getters
    public int getAWT() {
    	return this.avgWaitingTime;
    }
    
    public int getBU() {
    	return this.bedUtilization;
    }

    ////// Generator Function: Calculates and assigns values to attributes ///////
    public void generateStatistics(int totalPatients, int totalWaitingTime, int busyPeriodPatients, BusyTime BT) {
    	int totalBeds = 24 + 13 + 3;
        if (totalPatients > 0) {
            avgWaitingTime = totalWaitingTime / totalPatients;   		// Calculate Avg Time 
        } 
        else {
            avgWaitingTime = 0;
        }

        if (busyPeriodPatients < totalBeds) {
            bedUtilization = (busyPeriodPatients * 100) / totalBeds; 	// Bed percentage utilization // BusyPeriodPatients == Patients when standardBedArr.size() > 24
        } 
        else {
            bedUtilization = 100;
        }

        busierTimes = BT;
    }

    // Export Function: writes  the results to Reports.csv file
    public void exportResults() {
        try {
        	BufferedWriter writer = new BufferedWriter(new FileWriter("Reports.csv", true));
           
            writer.append("AvgWaitingTime,BedUtilization,BusierTimes\n");

            writer.append(avgWaitingTime + ",")
                  .append(bedUtilization + ",");
            
            // Writing busy times and patient counts
            if (busierTimes != null && !busierTimes.getTime().isEmpty()) {
                ArrayList<Instant> times = busierTimes.getTime();
                int[] patientCounts = busierTimes.getPatientCount();
                String patientCount = null;

                for (int i = 0; i < times.size(); i++) {
                	if (i < patientCounts.length) {
                		patientCount = Integer.toString(patientCounts[i]);
                	}
                	else {
                		patientCount = "N/A";
                	}
                    writer.append(times.get(i) + " (" + patientCount + " patients)");
                }
            } else {
                writer.append("No busy times recorded");
            }

            writer.append("\n"); // Newline after the record
            
            System.out.println("Statistics exported successfully to Reports.csv");
            writer.close();
        } 
        catch (IOException e) {
            System.out.println("Error exporting statistics: " + e.getMessage());
        }
    }  
}
