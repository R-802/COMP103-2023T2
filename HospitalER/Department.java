// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 3
 * Name:
 * Username:
 * ID:
 */

import ecs100.UI;

import java.util.*;

/**
 * A treatment Department (ER, X-Ray, MRI, ER, UltraSound, Surgery)
 * Each department will need
 * - A name,
 * - A maximum number of patients that can be treated at the same time
 * - A Set of Patients that are currently being treated
 * - A Queue of Patients waiting to be treated.
 * (ordinary queue, or priority queue, depending on argument to constructor)
 */

public class Department {

    private String name;
    private int maxPatients; // maximum number of patients receiving treatment at one time.

    // Fields for the statistics
    private int totalPatients = 0;
    private int waitTimes = 0;
    private int totalP1PatientStat = 0;
    private int waitTimeP1Stat = 0;

    private Set<Patient> treatmentRoom = new HashSet<>(); // the patients receiving treatment
    private Queue<Patient> waitingRoom = new PriorityQueue<>(); // the patients waiting for treatment

    /**
     * Construct a new Department object
     * Initialise the waiting queue and the current Set.
     */
    public Department(String name, int maxPatients, boolean usePriQueue) {
        this.maxPatients = maxPatients;
        this.name = name;
        if (usePriQueue) {
            this.waitingRoom = new PriorityQueue<>();
        } else {
            this.waitingRoom = new ArrayDeque<>();
        }
    }

    //-----------------------------------//
    //           COMPLETION              //
    //-----------------------------------//

    /**
     * Processes patients in the treatment room, identifying patients who have completed
     * their current treatment and either discharging them if all treatments are done,
     * or advancing their treatment if more treatments are needed. Discharged patients
     * are removed from the treatment room, while patients with ongoing treatments are
     * updated and retained in the room.
     *
     * @return List of patients with ongoing treatment.
     */
    /**
     * Identifies and processes patients who have finished their current treatment.
     * Patients who have completed all their treatments are discharged,
     * while patients with ongoing treatments are retained for further treatment.
     *
     * @return List of patients with ongoing treatment.
     */
    public ArrayList<Patient> removeDischarged() {
        // List to hold patients with ongoing treatments
        ArrayList<Patient> treatmentList = new ArrayList<>();

        // Using iterator for safe removal
        Iterator<Patient> iterator = treatmentRoom.iterator();

        while (iterator.hasNext()) {
            Patient patient = iterator.next();

            // Check if the patient's current treatment is finished
            if (patient.currentTreatmentFinished()) {
                // If all treatments are completed, discharge the patient
                if (patient.allTreatmentsCompleted()) {
                    discharge(patient); // Directly call the existing discharge method
                    iterator.remove(); // Remove discharged patients
                } else {
                    treatmentList.add(patient); // Add patient to treatment list
                }
            }
        }

        // Return the list of patients with ongoing treatments
        return treatmentList;
    }

    /**
     * Discharge a patient from the department, removing them from the treatment room
     * and updating relevant statistics.
     *
     * @param patient The patient to be discharged.
     */
    public void discharge(Patient patient) {
        if (treatmentRoom.contains(patient)) {
            removeFromTreatmentRoom(patient); // Remove the patient from the treatment room
            updateStats(patient); // Update relevant statistics
            UI.println("Discharged: " + patient);
        } else {
            UI.println("Patient not found in treatment room: " + patient);
        }
    }

    /**
     * Removes the specified list of patients from the treatment room and updates
     * relevant statistics based on the discharge status.
     *
     * @param patientList     The list of patients to be removed from the treatment room.
     * @param isFullDischarge A flag indicating whether the patients are fully discharged.
     */
    private void removeFromTreatmentRoom(List<Patient> patientList, boolean isFullDischarge) {
        for (Patient patient : patientList) {
            removeFromTreatmentRoom(patient); // Remove the patient from the treatment room

            // If patients are fully discharged, update the statistics
            if (isFullDischarge) {
                updateStats(patient);
            }
        }
    }

    /**
     * Safely removes a patient from the treatment room using an iterator.
     *
     * @param patient The patient to be removed.
     */
    private void removeFromTreatmentRoom(Patient patient) {
        Iterator<Patient> iterator = treatmentRoom.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == patient) {
                iterator.remove(); // Remove the patient from the treatment room
                break; // No need to continue iterating once patient is found and removed
            }
        }
    }


    /**
     * Updates relevant statistics based on the discharged patient.
     *
     * @param patient The discharged patient.
     */
    private void updateStats(Patient patient) {
        this.totalPatients++; // Increment the total patient count
        this.waitTimes += patient.getTotalWaitingTime(); // Add patient's waiting time to cumulative wait times

        // Update priority-specific statistics for high priority patients (priority 1)
        if (patient.getPriority() == 1) {
            this.totalP1PatientStat++; // Increment the count of priority 1 patients
            this.waitTimeP1Stat += patient.getTotalWaitingTime(); // Add waiting time of priority 1 patients to their cumulative wait times
        }
    }

    /**
     * Add a new patient to the department waiting room priority queue.
     *
     * @param patient The patient to be added to the waiting room.
     */
    public void addNewPatient(Patient patient) {
        waitingRoom.offer(patient);
    }

    /**
     * @return set of patients in the treatment room
     */
    public Set<Patient> getTreatmentRoom() {
        return this.treatmentRoom;
    }

    /**
     * @return queue of patients in the waiting room
     */
    public Queue<Patient> getWaitingRoom() {
        return this.waitingRoom;
    }

    /**
     * @return the maximum number of patients in a department
     */
    public int getMaxPatients() {
        return this.maxPatients;
    }


    /**
     * Move patients from the waiting room to the treatment room.
     */
    public void moveToTreatment() {
        // If there is space in the treatment room
        if (treatmentRoom.size() < maxPatients) {
            // Retrive the first patient from the waiting room priority queue
            Patient transfer = waitingRoom.poll();
            // Ensure the patient exists
            if (transfer != null) {
                treatmentRoom.add(transfer);
            }
        }
    }

    /**
     * Removes the specified patient from the waiting room.
     * If the patient is found and removed, it returns true.
     *
     * @param patient The patient to be removed.
     * @return True if the patient was found and removed, false otherwise.
     */
    public boolean removeFromWaitingRoom(Patient patient) {
        return waitingRoom.remove(patient);
    }

    /**
     * Generates and displays statistical information about the patient treatment department.
     * This includes the total number of patients treated, average wait times, and specific
     * statistics for priority one patients.
     */
    public void reportDepartmentStats() {
        UI.println("\n"); // Print a new line for separation
        UI.println("---------------------------------------------------------");
        UI.println("Department: " + this.name); // Display the department name
        UI.println("---------------------------------------------------------");

        // Check if there's enough data to calculate and display overall statistics
        if (totalPatients > 0 && waitTimes > 0) {
            UI.println("---------------------------");
            UI.println("Total patients treated: " + totalPatients);
            UI.println("Average wait time: " + waitTimes / totalPatients); // Calculate and display average wait time
        } else {
            UI.println("Not enough data!"); // Display a message if data is insufficient
        }

        // Check if there's enough data to calculate and display priority one statistics
        if (totalP1PatientStat > 0 && waitTimeP1Stat > 0) {
            UI.println("-------PRIORITY ONE--------");
            UI.println("Total priority one patients treated: " + totalP1PatientStat);
            UI.println("Average priority one wait time: " + waitTimeP1Stat / totalP1PatientStat); // Calculate and display average wait time for priority one patients
        } else {
            UI.println("Not enough data!"); // Display a message if data is insufficient
        }
    }

    public void resetStats() {
        this.totalPatients = 0;
        this.waitTimes = 0;
        this.totalP1PatientStat = 0;
        this.waitTimeP1Stat = 0;
    }

    //-----------------------------------//
    //          COMPLETION END           //
    //-----------------------------------//

    /**
     * Draw the department: the patients being treated and the patients waiting
     * You may need to change the names if your fields had different names
     */
    public void redraw(double y) {
        UI.setFontSize(14);
        UI.drawString(name, 0, y - 35);
        double x = 10;
        UI.drawRect(x - 5, y - 30, maxPatients * 10, 30);  // box to show max number of patients
        for (Patient p : treatmentRoom) {
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for (Patient p : waitingRoom) {
            p.redraw(x, y);
            x += 10;
        }
    }
}
