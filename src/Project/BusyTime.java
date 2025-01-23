package Project;

import java.time.Instant;
import java.util.ArrayList;

public class BusyTime {
    private ArrayList<Instant> time;
    private int [] patientCount; 		// Number of patients during the busy times

    public BusyTime() {
        this.time = new ArrayList<Instant>();
        this.patientCount = new int [1000];
    }

    public ArrayList<Instant> getTime() {
        return time;
    }

    public int[] getPatientCount() {
        return patientCount;
    }
    
    public void setTimeAndPatientCount(Instant t, int pc) {
    	time.add(t);
    	patientCount[time.size()] = pc;
    }

    public String toString() {
    	String result = "";

        for (int i = 0; i < time.size(); i++) {
            result += "Time: " + time.get(i) + " - Total Patients: " + (i < patientCount.length ? patientCount[i] : "N/A") + "\n";
        }
        return result;
    }
}

