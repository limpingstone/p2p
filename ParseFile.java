import java.io.*;
import java.util.*;

public class ParseFile {

    // Initializes the array list of ports by reading from the file config_peer.txt
    public static void loadConfigPeerFile() {
        try {
            File configPeerFile = new File("config_peer.txt");
            BufferedReader br = new BufferedReader(new FileReader(configPeerFile));

            String portStr;
            int id = 1000;
            while ((portStr = br.readLine()) != null)
                TcpSocketController.availableTcpSockets.add(new TcpSocket(Integer.parseInt(portStr), id++));
        }
        catch (IOException e) {
            System.out.println("Error reading from config_peer.txt");
        }
    }

    /**
     * The static method that updates the config_peer.txt file with the available sockets
     * This will be called every time there is a modification to the list of available sockets
     */
    public static void writeConfigPeerFile() {
        try {
            FileWriter configPeerWriter = new FileWriter("config_peer.txt", false);
            for (TcpSocket socket : TcpSocketController.availableTcpSockets) {
                configPeerWriter.write(socket.getPort() + "\n");
            }

            configPeerWriter.flush();
            configPeerWriter.close();
        }
        catch (IOException e) {
            System.out.println("Error writing to config_peer.txt");
        }
    }
}
