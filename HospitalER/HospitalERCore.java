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
 * Simple Simulation of a Hospital ER
 * <p>
 * The Emergency room has a waiting room and a treatment room that has a fixed
 * set of beds for examining and treating patients.
 * <p>
 * When a patient arrives at the emergency room, they are immediately assessed by the
 * triage team who determines the priority of the patient.
 * <p>
 * They then wait in the waiting room until a bed becomes free, at which point
 * they go from the waiting room to the treatment room.
 * <p>
 * When a patient has finished their treatment, they leave the treatment room and are discharged,
 * at which point information about the patient is added to the statistics.
 * <p>
 * READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCore {
    private static final int MAX_PATIENTS = 5;   // max number of patients currently being treated
    // Fields for recording the patients waiting in the waiting room and being treated in the treatment room
    private Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    private Set<Patient> treatmentRoom = new HashSet<Patient>();

    // Fields for the statistics
    private int totalPatients = 0;
    private int waitTimes = 0;
    private int totalP1PatientStat = 0;
    private int waitTimeP1Stat = 0;

    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick

    /**
     * main:  Construct a new HospitalERCore object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments) {
        HospitalERCore er = new HospitalERCore();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }

    /**
     * Reset the simulation:
     * stop any running simulation,
     * reset the waiting and treatment rooms
     * reset the statistics.
     */
    public void reset(boolean usePriorityQueue) {
        running = false;
        UI.sleep(2 * delay);  // to make sure that any running simulation has stopped
        time = 0;           // set the "tick" to zero.

        // reset the waiting room, the treatment room, and the statistics.
        treatmentRoom = new HashSet<>();
        if (usePriorityQueue) {
            waitingRoom = new PriorityQueue<Patient>();
        } else {
            waitingRoom = new ArrayDeque<>();
        }

        totalPatients = 0;
        waitTimes = 0;
        totalP1PatientStat = 0;
        waitTimeP1Stat = 0;

        UI.clearGraphics();
        UI.clearText();
    }

    // Additional methods used by run() (You can define more of your own)

    /**
     * Main loop of the simulation
     */
    public void run() {
        if (running) {
            return;
        } // don't start simulation if already running one!
        running = true;
        while (running) {
            time++; // Increment the time

            // Temporary list of patients to discharge
            List<Patient> dischargeList = new ArrayList<>();

            // For each patient in the treatment room set
            for (Patient patient : treatmentRoom) {
                // And if the patient has completed their treatment
                if (patient.currentTreatmentFinished()) {
                    // Add them to the discharge list
                    dischargeList.add(patient);
                }
            }

            // For each patient in the discharge list
            for (Patient patient : dischargeList) {
                // Remove them from the treatment room set
                treatmentRoom.remove(patient);
                totalPatients++; // increment the total number of patients
                waitTimes += time - patient.getTotalWaitingTime();
                UI.println(time + ": Discharged: " + patient);

                // If the patient is priority one, increment the priority one statistics
                if (patient.getPriority() == 1) {
                    totalP1PatientStat++;
                    waitTimeP1Stat += time - patient.getTotalWaitingTime();
                }
            }

            // For each patient in the waiting room, advance their wait time by one tick
            for (Patient patient : waitingRoom) {
                patient.waitForATick();
            }

            // For each patient in the treatment room, advance their treatment time by one tick
            for (Patient patient : treatmentRoom) {
                patient.advanceCurrentTreatmentByTick();
            }

            // If there is space in the treatment room, take the
            // highest priority patient from the waiting room
            // and move them to the treatment room.
            if (treatmentRoom.size() < MAX_PATIENTS) {
                Patient transferPatient = waitingRoom.poll();
                if (transferPatient != null) {
                    treatmentRoom.add(transferPatient);
                }
            }

            // Gets any new patient that has arrived and adds them to the waiting room
            Patient newPatient = PatientGenerator.getNextPatient(time);
            if (newPatient != null) {
                UI.println(time + ": Arrived: " + newPatient);
                waitingRoom.offer(newPatient);
            }
            redraw();
            UI.sleep(delay);
        }
        // paused, so report current statistics
        reportStatistics();
    }


    // METHODS FOR THE GUI AND VISUALISATION

    /**
     * Report summary statistics about all the patients that have been discharged.
     */
    public void reportStatistics() {
        if (totalPatients > 0 && waitTimes > 0) {
            UI.println("---------------------------");
            UI.println("Total patients treated: " + totalPatients);
            UI.println("Average wait time: " + waitTimes / totalPatients);
        } else {
            UI.println("Not enough data!");
        }
        if (totalP1PatientStat > 0 && waitTimeP1Stat > 0) {
            UI.println("-------PRIORITY ONE--------");
            UI.println("Total priority one patients treated: " + totalP1PatientStat);
            UI.println("Average priority one wait time: " + waitTimeP1Stat / totalP1PatientStat);
        } else {
            UI.println("Not enough data!");
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
     * Redraws all the patients and the state of the simulation
     */
    public void redraw() {
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0, 32, 400, 32);

        // Draw the treatment room and the waiting room:
        double y = 80;
        UI.setFontSize(14);
        UI.drawString("ER", 0, y - 35);
        double x = 10;
        UI.drawRect(x - 5, y - 30, MAX_PATIENTS * 10, 30);  // box to show max number of patients
        for (Patient p : treatmentRoom) {
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for (Patient p : waitingRoom) {
            p.redraw(x, y);
            x += 10;
        }
        UI.drawLine(0, y + 2, 400, y + 2);
    }
}
