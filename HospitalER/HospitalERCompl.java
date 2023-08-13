// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 3
 * Name:
 * Username:
 * ID:
 */

import ecs100.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulation of a Hospital ER
 * <p>
 * The hospital has a collection of Departments, including the ER department, each of which has
 * and a treatment room.
 * <p>
 * When patients arrive at the hospital, they are immediately assessed by the
 * triage team who determine the priority of the patient and (unrealistically) a sequence of treatments
 * that the patient will need.
 * <p>
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 * <p>
 * READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl {
    /**
     * The map of the departments.
     * The names of the departments should be "ER", "X-Ray", "MRI", "UltraSound" and "Surgery"
     * The maximum patients should be 8 for "ER", 3 for "X-Ray", 1 for "MRI", 2 for "UltraSound" and
     * 3 for "Surgery"
     */
    private Map<String, Department> departments = new HashMap<String, Department>();

    // Fields for the simulation
    private boolean running = false;
    private boolean usingPriorityQueue;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick

    /**
     * Construct a new HospitalER object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments) {
        HospitalERCompl er = new HospitalERCompl();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }

    /**
     * Stop any running simulation
     * Define the departments available and put them in the map of departments.
     * Each department needs to have a name and a maximum number of patients that
     * it can be treating at the same time.
     * Reset the statistics
     */
    public void reset(boolean usePriorityQueue) {
        this.usingPriorityQueue = usePriorityQueue;
        UI.sleep(2 * delay); // Let any running simulation finish
        running = false;
        time = 0;

        // For each department reset the waiting room, the treatment room, and the statistics.
        for (Department department : departments.values()) {
            department.getWaitingRoom().clear();
            department.getTreatmentRoom().clear();
            department.resetStats();
        }

        // Define the departments available and put them in the map of departments.
        departments = new HashMap<>();
        departments.put("ER", new Department("ER", 8, usePriorityQueue));
        departments.put("X-ray", new Department("X-ray", 3, usePriorityQueue));
        departments.put("MRI", new Department("MRI", 1, usePriorityQueue));
        departments.put("Ultrasound", new Department("Ultrasound", 2, usePriorityQueue));
        departments.put("Surgery", new Department("Surgery", 3, usePriorityQueue));

        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */
    public void run() {
        if (running) {
            return;
        }
        running = true;

        while (running) {
            time++; // Advance time by 1 tick

            // Iterate through the departments
            for (Department department : departments.values()) {
                // Find patients who have finished their current treatment in that department
                ArrayList<Patient> dischargedPatients = department.removeDischarged();

                // Remove completed treatments from their treatment plan
                for (Patient patient : dischargedPatients) {
                    patient.removeCurrentTreatment();
                }

                // Discharge patients who have completed all treatments
                for (Patient patient : dischargedPatients) {
                    if (patient.allTreatmentsCompleted()) {
                        department.discharge(patient);
                    }
                }

                // Move patients to the next department according to their treatment plan
                for (Patient patient : dischargedPatients) {
                    if (!patient.allTreatmentsCompleted()) {
                        String nextDepartment = patient.getCurrentDepartment();
                        departments.get(nextDepartment).addNewPatient(patient);
                    }
                }
            }

            // Move waiting patients to treatment rooms if there are spaces
            for (Department department : departments.values()) {
                // If the department has space, move patients from the waiting room to the treatment room
                if (department.getMaxPatients() > department.getTreatmentRoom().size()) {
                    department.moveToTreatment();
                }
            }

            advanceTreatmentTicks();

            // Gets any new patient that has arrived and adds them to the waiting room
            Patient newPatient = PatientGenerator.getNextPatient(time);

            if (newPatient != null) {
                UI.println(time + ": Arrived: " + newPatient);
                Department ERDepartment = departments.get("ER");
                ERDepartment.addNewPatient(newPatient);
            }

            redraw();
            UI.sleep(delay);
        }
        reportStatistics();
    }



    private void advanceTreatmentTicks() {
        for (Department department : departments.values()) {
            for (Patient patient : department.getWaitingRoom()) {
                patient.waitForATick();
            }
            for (Patient patient : department.getTreatmentRoom()) {
                patient.advanceCurrentTreatmentByTick();
            }
        }
    }

    // METHODS FOR THE GUI AND VISUALISATION

    /**
     * Report summary statistics about the simulation, handled
     * in the Department class
     */
    public void reportStatistics() {
        // Iterate through department objects
        for (Department department : departments.values()) {
            // Report the statistics for each department
            department.reportDepartmentStats();
        }
    }

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI() {
        UI.addButton("Reset (Queue)", () -> {
            this.reset(false);
        });
        UI.addButton("Reset (Pri Queue)", () -> {
            this.reset(true);
        });
        UI.addButton("Start", () -> {
            if (!running) {
                run();
            }
        });   //don't start if already running!
        UI.addButton("Pause & Report", () -> {
            running = false;
        });
        UI.addSlider("Speed", 1, 400, (401 - delay), (double val) -> {
            delay = (int) (401 - val);
        });
        UI.addSlider("Av arrival interval", 1, 50, PatientGenerator.getArrivalInterval(), PatientGenerator::setArrivalInterval);
        UI.addSlider("Prob of Pri 1", 1, 100, PatientGenerator.getProbPri1(), PatientGenerator::setProbPri1);
        UI.addSlider("Prob of Pri 2", 1, 100, PatientGenerator.getProbPri2(), PatientGenerator::setProbPri2);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000, 600);
        UI.setDivider(0.5);
    }

    /**
     * Redraws all the departments
     */
    public void redraw() {
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0, 32, 400, 32);
        double y = 80;
        for (Department dept : departments.values()) {
            dept.redraw(y);
            UI.drawLine(0, y + 2, 400, y + 2);
            y += 50;
        }
    }
}
