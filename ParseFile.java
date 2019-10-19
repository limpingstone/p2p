import java.io.*;
import java.util.*;

public class ParseFile {

    // Define the fallback port for connection
    private static int defaultPort = 52322;

    // The array list of integers containing the available ports on the local machine
    private static ArrayList<Integer> availableLocalPorts = new ArrayList<Integer>();

    // Initializes the array list of ports by reading from the file config_peer.txt
    static {
        try {
            File configPeerFile = new File("config_peer.txt");
            BufferedReader br = new BufferedReader(new FileReader(configPeerFile));

            String portStr;
            while ((portStr = br.readLine()) != null)
                availableLocalPorts.add(Integer.parseInt(portStr));
        }
        catch (IOException e) {

            // Falls back to the default port
            System.out.println("Error reading from config_peer.txt, default to port " + defaultPort);
            availableLocalPorts.add(new Integer(defaultPort));
        }
    }

    /**
     * The static method that returns the list of ports in the format of an array list
     * @return an array list of object Integer containing the value of ports
     */
    public static ArrayList<Integer> getConfigPorts() {
        return availableLocalPorts;
    }

    /**
     * The static method that removes the port number that is being used, in which the file config_peer.txt is rewritten
     * @return the port number that is being used in the format of int
     */
    public static int getNextConfigPort() {
        try {
            FileWriter configPeerWriter = new FileWriter("config_peer.txt", false);
            Integer nextPort = availableLocalPorts.remove(0);

            for (Integer portNumber : availableLocalPorts) {
                configPeerWriter.write(portNumber.toString() + "\n");
            }
            configPeerWriter.flush();
            configPeerWriter.close();

            return nextPort.intValue();
        }
        catch (IOException e) {
            System.out.println("Error reading from config_peer.txt, default to port " + defaultPort);
            return defaultPort;
        }
        catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }


    public static void writeToConfigNeighbors() {
        /*
        try {
            File configNeighborsFile = new File("config_neighbors.txt");

            BufferedReader br = new BufferedReader(new FileReader(configPeerFile));
            String portStr = br.readLine();

            return Integer.parseInt(portStr);
        }
        catch (IOException e) {
            int defaultPort = 52322;
            System.out.println("Error reading from config_peer.txt, default to port " + defaultPort);

            return defaultPort;
        }
        */
    }
}
