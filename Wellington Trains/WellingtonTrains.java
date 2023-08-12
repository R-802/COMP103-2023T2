// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 2
 * Name: Shemaiah Rangitaawa
 * Username: rangitshem
 * ID: 300601564
 */

import ecs100.UI;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * WellingtonTrains
 * A program to answer queries about Wellington train lines and timetables for
 * the train services on those train lines.
 * <p>
 * See the assignment page for a description of the program and what you have to do.
 */

public class WellingtonTrains {
    private static boolean loadedData = false;  // used to ensure that the program is called from main.
    // Holds the name of the line as key and the TrainLine object as value
    private final HashMap<String, TrainLine> trainLineMap = new HashMap<>();
    // Holds the name of the station as key and Station object as value
    private final HashMap<String, Station> stationsMap = new HashMap<>();
    //Fields to store the collections of Stations and Lines
    Station station;
    TrainLine trainLine;
    TrainService trainService;
    // Fields for the suggested GUI.
    private String stationName;        // station to get info about, or to start journey from
    private String lineName;           // train line to get info about.
    private String destinationName;
    private int startTime = 0;         // time for enquiring about

    /**
     * main method:  load the data and set up the user interface
     */
    public static void main(String[] args) {
        WellingtonTrains wel = new WellingtonTrains();
        wel.loadData();   // load all the data
        wel.setupGUI();   // set up the interface
    }

    /**
     * Load data files
     */
    public void loadData() {
        loadStationData();
        UI.println("Loaded Stations");
        loadTrainLineData();
        UI.println("Loaded Train Lines");
        // The following is only needed for the Completion and Challenge
        loadTrainServicesData();
        UI.println("Loaded Train Services");
        loadedData = true;
    }

    /**
     * User interface has buttons for the queries and text fields to enter stations and train line
     * You will need to implement the methods here.
     */
    public void setupGUI() {
        UI.addButton("All Stations", this::listAllStations);
        UI.addButton("Stations by name", this::listStationsByName);
        UI.addButton("All Lines", this::listAllTrainLines);
        UI.addTextField("Station", (String name) -> {
            this.stationName = name;
        });
        UI.addTextField("Train Line", (String name) -> {
            this.lineName = name;
        });
        UI.addTextField("Destination", (String name) -> {
            this.destinationName = name;
        });
        UI.addTextField("Time (24hr)", (String time) -> {
            try {
                this.startTime = Integer.parseInt(time);
            } catch (Exception e) {
                UI.println("Enter four digits");
            }
        });
        UI.addButton("Lines through Station", () -> {
            listLinesOfStation(this.stationName);
        });
        UI.addButton("Stations on Line", () -> {
            listStationsOnLine(this.lineName);
        });
        UI.addButton("Stations connected?", () -> {
            checkConnected(this.stationName, this.destinationName);
        });
        UI.addButton("Next Services", () -> {
            findNextServices(this.stationName, this.startTime);
        });

        UI.addButton("Quit", UI::quit);
        UI.setMouseListener(this::doMouse);

        UI.setWindowSize(900, 400);
        UI.setDivider(0.2);
        // this is just to remind you to start the program using main!
        if (!loadedData) {
            UI.setFontSize(36);
            UI.drawString("Start the program from main", 2, 36);
            UI.drawString("in order to load the data", 2, 80);
            UI.sleep(2000);
            UI.quit();
        } else {
            UI.drawImage("data/geographic-map.png", 0, 0);
            UI.drawString("Click to list closest stations", 2, 12);
        }
    }

    public void doMouse(String action, double x, double y) {
        if (action.equals("released")) {
            // Tree map of the distances between stations with distance as
            // key and station as value
            Map<Double, Station> stationDistances = new TreeMap<>();
            for (String key : stationsMap.keySet()) {
                Station station = stationsMap.get(key);

                double stationY = station.getYCoord();
                double stationX = station.getXCoord();

                // Calculate Euclidean distance between two points
                double distance = Math.hypot((x - stationX), (y - stationY));
                stationDistances.put(distance, station);
            }

            int count = 0;

            UI.clearText();
            UI.println("10 Closest Stations to Point on Map");
            UI.println("-----------------------------------");

            // Print the 10 closest stations to point on map
            for (Map.Entry<Double, Station> entry : stationDistances.entrySet()) {
                if (count < 10) {
                    double distance = entry.getKey();
                    Station station = entry.getValue();

                    // Print distance to two dp
                    UI.printf("%.2fkm: ", distance);
                    UI.println(station);
                    count++;
                }
            }
        }
    }

    // Methods for loading data and answering queries

    /**
     * loadStationData(), reads data from stations file, adds it to the
     * Stations and TrainLine classes, then adds station name and station object
     * to the stations HashMap field.
     */
    public void loadStationData() {
        try {
            List<String> allLines = Files.readAllLines(Path.of("data/stations.data"));
            for (String line : allLines) {
                Scanner scan = new Scanner(line);
                String name = scan.next();

                // Get zone and xy coordinates from file
                int zone = scan.nextInt();
                double x = scan.nextDouble();
                double y = scan.nextDouble();

                // Add a new station object to HashMap field
                stationsMap.put(name, new Station(name, zone, x, y));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * loadTrainLineData(), loads the train line data into hash maps
     */
    public void loadTrainLineData() {
        try {
            // Make a list of all the line names in the train lines file
            List<String> trainLines = Files.readAllLines(Path.of("data/train-lines.data"));
            // For each line name in the list
            for (String line : trainLines) {
                // Add the name of the line and the object to the hashmap
                trainLine = new TrainLine(line);
                trainLineMap.put(line, trainLine);

                // Scan all station files
                Scanner scanStationFile = new Scanner("data/" + line + "-stations.data");
                String fileNames = scanStationFile.next();

                // Create a list of the stations file names
                List<String> stationFileNames = new ArrayList<>();
                stationFileNames.add(fileNames);

                // Iterate through the list of station file name
                for (String stationName : stationFileNames) {
                    // Read each file
                    File file = new File(stationName);
                    Scanner sc = new Scanner(file);

                    while (sc.hasNext()) {
                        String str = sc.next();
                        station = stationsMap.get(str);
                        trainLine.addStation(station);
                        station.addTrainLine(trainLine);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * loadTrainServicesData(), read the train lines hash map
     * and gets the name and object of each entry. Uses each
     * name in the map to read through all the service
     * files, gets each sequence of numbers in each
     * service file and for each sequence, creates
     * a new train service and adds the time it.
     */
    public void loadTrainServicesData() {
        try {
            // For each entry in the train lines hash map
            for (Map.Entry<String, TrainLine> entry : trainLineMap.entrySet()) {
                // Get the name and train line object
                String trainLineName = entry.getKey();
                TrainLine trainline = entry.getValue();

                // Get the service file with the current train line name
                String fileName = "data/" + trainLineName + "-services.data";

                // Read the file and store the sequences of times as a list
                List<String> serviceSequences = Files.readAllLines(Path.of(fileName));
                for (String sequence : serviceSequences) {
                    // Create a new service for each sequence
                    trainService = new TrainService(trainline);

                    // Scan the sequence of strings
                    Scanner scanSequence = new Scanner(sequence);
                    while (scanSequence.hasNextInt()) {
                        // Read each time in the sequence of times
                        int serviceTime = scanSequence.nextInt();

                        // Add a time to the service
                        trainService.addTime(serviceTime);
                    }

                    trainline.addTrainService(trainService);
                }
            }
        } catch (IOException e) {
        }
    }


    /**
     * listAllStations(), prints out all  stations in the
     * stations HashMap. The map holds the station names as
     * key and station objects as value, so to list all
     * the station objects in the map, list all the
     * values.
     */
    public void listAllStations() {
        UI.clearText();
        UI.println("All Stations in the Region:");
        UI.println("---------------------------");
        for (Map.Entry<String, Station> entry : stationsMap.entrySet()) {
            UI.println(entry.getValue());
        }
    }

    /**
     * listStationsByName(), lists the station objects
     * in alphabetical order by adding the stations
     * hashmap into a tree map orders the stations in
     * natural alphabetical order.
     */
    public void listStationsByName() {
        UI.clearText();
        UI.println("Stations in the region (in alphabetical order):");
        UI.println("-----------------------------------------------");
        // Create new TreeMap, put stationsMap data into treeMap
        TreeMap<String, Station> stationsTreeMap = new TreeMap<>(stationsMap);
        for (Map.Entry<String, Station> stationEntry : stationsTreeMap.entrySet()) {
            // Print station using getValue method
            UI.println(stationEntry.getValue());
        }
    }

    /**
     * listAllTrainLines(), lists all train lines in the
     * train lines hashset.
     */
    public void listAllTrainLines() {
        UI.clearText();
        UI.println("All train lines in the region:");
        UI.println("------------------------------");
        for (Map.Entry<String, TrainLine> line : trainLineMap.entrySet()) {
            UI.println(line.getValue().toString());

        }
    }

    /**
     * listLinesOfStation(), lists the train lines that
     * go through a given station.
     *
     * @param stationName
     */
    public void listLinesOfStation(String stationName) {
        UI.clearText();
        UI.println("Train lines that go through " + stationName + " station:");
        UI.println("------------------------------------------------");
        if (stationsMap.containsKey(stationName)) {
            station = stationsMap.get(stationName);
            for (TrainLine line : station.getTrainLines()) {
                UI.println(line);
            }
        } else if (stationName == null) {
            UI.println("Please enter a station name!");
        }
    }

    /**
     * listStationsOnLine(), lists all the stations
     * along the given line.
     *
     * @param lineName
     */
    public void listStationsOnLine(String lineName) {
        UI.clearText();
        UI.println("Stations on the " + lineName + " line:");
        UI.println("----------------------------------------");
        if (trainLineMap.containsKey(lineName)) {
            trainLine = trainLineMap.get(lineName);
            for (Station station : trainLine.getStations()) {
                UI.println(station);
            }
        } else if (lineName == null) {
            UI.println("Please enter a train line!");
        }
    }

    /**
     * checkConnected(), print the name of a train line that goes from a
     * station to a destination station.
     *
     * @param stationName
     * @param destinationName
     */
    public void checkConnected(String stationName, String destinationName) {
        UI.clearText();
        UI.println("Train lines for " + stationName + " station:");
        UI.println("---------------");

        Station origin = stationsMap.get(stationName);
        Station destination = stationsMap.get(destinationName);

        for (Map.Entry<String, TrainLine> entry : trainLineMap.entrySet()) {
            TrainLine line = entry.getValue();
            List<Station> stationsOnLine = line.getStations();

            if (stationsOnLine.contains(origin) && stationsOnLine.contains(destination)) {
                if (stationsOnLine.indexOf(origin) < stationsOnLine.indexOf(destination)) {
                    UI.println("The " + line.getName() + " goes from " + stationName + " to " + destinationName);
                    UI.println("The trip goes through " + destination.getZone() + " fare zones.");
                }
            }
        }
    }

    /**
     * Find the next train service for each line at a station after the specified time
     *
     * @param stationName
     * @param startTime
     */
    public void findNextServices(String stationName, int startTime) {
        UI.clearText();
        UI.println("Next services from " + stationName + " station:");
        UI.println("------------------------------");

        // TreeMap containing next services with station/line name as key and time as value
        TreeMap<String, Integer> nextServices = new TreeMap<>();
        Station station = stationsMap.get(stationName);
        Set<TrainLine> trainlines = station.getTrainLines();

        // Iterate through the set of train lines
        for (TrainLine line : trainlines) {
            int time = -1;

            // Get the index of the current station in the
            // list of stations on the line
            int index = line.getStations().indexOf(station);

            // Used to check if a time has been located
            boolean timeLoc = false;

            // List containing the services on the current line
            List<TrainService> services = line.getTrainServices();

            // Iterate through the list of services
            for (TrainService service : services) {
                int timeAtStation = service.getTimes().get(index);

                // Ensure the time is after the specified start time
                if (timeAtStation >= startTime) {
                    if (!timeLoc) {
                        time = timeAtStation;
                        timeLoc = true;
                    }
                }
            }
            nextServices.put(line.getName(), time);
        }

        for (Map.Entry<String, Integer> entry : nextServices.entrySet()) {
            String line = entry.getKey();
            int time = entry.getValue();
            UI.println("On " + line + " line at " + time);
        }
    }
}
