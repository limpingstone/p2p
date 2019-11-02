import java.io.*;
import java.util.*;

public class TcpSocketController {
    // The array list of available TCP sockets that has not yet being connected
    public static ArrayList<TcpSocket> availableTcpSockets = new ArrayList<TcpSocket>();

    // The array list of TCP sockets that are in active connection with a peer
    public static ArrayList<TcpSocket> connectedTcpSockets = new ArrayList<TcpSocket>();

    // The array containing the list of query ID's that has been processed and passed on
    public static ArrayList<String> queryIdList = new ArrayList<String>();

    // The array containing the list of response ID's that has been processed and passed on
    public static ArrayList<String> responseIdList = new ArrayList<String>();

    // The array containing the query ID sent from this peer
    public static ArrayList<Integer> sentQueries = new ArrayList<Integer>();

    /**
     * The static method that initializes all the TCP sockets and add them into the array list of available TCP sockets
     * The IDs are for the TCP sockets to self identify its position in the array list
     */
    public static void initTcpSockets() {
        ParseFile.loadConfigPeerFile();
    }

    /**
     * The method that returns the next available socket in the list of available sockets
     * @return a TCP socket available for connection, or null if the list is empty
     */
    public static TcpSocket getNextAvailableSocket() {
        try {
            return availableTcpSockets.get(0);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * The method that returns the next connected socket in the list of connected sockets
     * @return a TCP socket in connection, or null if the list is empty
     */
    public static TcpSocket getNextConnectedSocket() {
        try {
            return connectedTcpSockets.get(0);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * The static method called when a socket is connected
     * This process will include the removing of the socket from the list of available TCP sockets and into the list of connected TCP sockets
     * @param socketId the id of the TCP socket in the format of int
     */

    public static void socketConnected(int socketId) {
        int socketIndex = 0;
        TcpSocket connectedSocket = null;

        // Search for the ID of the socket and remove it
        for (TcpSocket socket : availableTcpSockets) {
            if (socket.getId() == socketId) {
                connectedSocket = availableTcpSockets.remove(socketIndex);
                break;
            }
            ++socketIndex;
        }

        // Add the socket to the list of connected sockets
        if (connectedSocket != null)
            connectedTcpSockets.add(connectedSocket);

        // Updates the config_peer.txt and config_neighbors.txt files
        ParseFile.writeConfigPeerFile();
        ParseFile.writeConfigNeighborsFile();
    }

    /**
     * The static method called when a socket is disconnected
     * This process will include the removing of the socket from the list of connected TCP sockets and into the list of available TCP sockets
     * @param socketId the id of the TCP socket in the format of int
     */

    public static void socketDisconnected(int socketId) {
        int socketIndex = 0;
        TcpSocket availableSocket = null;

        // Search for the ID of the socket and remove it
        for (TcpSocket socket : connectedTcpSockets) {
            if (socket.getId() == socketId) {
                availableSocket = connectedTcpSockets.remove(socketIndex);
                break;
            }
            ++socketIndex;
        }

        // Add the socket to the list of available sockets
        if (availableSocket != null)
            availableTcpSockets.add(availableSocket);

        // Updates the config_peer.txt and config_neighbors.txt files
        ParseFile.writeConfigPeerFile();
        ParseFile.writeConfigNeighborsFile();
    }

    /**
     * The static method that disconnects all connections in the list of connected sockets
     * This is done when 'leave' or 'exit' is called from the prompt
     */
    public static void disconnectAllSockets() {
        // Disconnect all sockets and add them to the list of available sockets
        while (getNextConnectedSocket() != null)
            availableTcpSockets.add(connectedTcpSockets.remove(0));

        for (TcpSocket socket : availableTcpSockets)
            socket.disconnectedFromPeer();

        // Empty the list of connected sockets
        connectedTcpSockets = new ArrayList<TcpSocket>();

        // Updates the config_peer.txt and config_neighbors.txt files
        ParseFile.writeConfigPeerFile();
        ParseFile.writeConfigNeighborsFile();
    }

    /**
     * The static method that passes on the query to the neighboring peer
     * @param queryId the query ID in the form of string
     * @param queryFile the filename in the form of string
     * @param socketId the ID of the socket to prevent the method from passing the query back to the source
     */
    public static void passOnQuery(String queryId, String queryFile, int socketId) {
        // If the query is already processed, skip
        for (String id : queryIdList) {
            if (id.equals(queryId))
                return;
        }

        for (TcpSocket socket : connectedTcpSockets) {

            // sends the response to every neighboring peer except the source peer
            if (socketId != socket.getId()) {
                socket.writeToPeer("Q:" + queryId +";" + queryFile + '\n');
            }

            // Generates the response if this peer has the file
            else if (ParseFile.fileFound(queryFile)) {
                for (String match : ParseFile.getExactNames(queryFile))
                    socket.writeToPeer("R:" + queryId + ";" + NetworkUtil.getOwnExternalIp() + ":" + getNextAvailableSocket().getLocalPort() + ";" + match + '\n');
            }

            // Mark the query as processed
            queryIdList.add(queryId);
        }
    }

    /**
     * The static method that passes on the response to the neighboring peer
     * @param queryId the response ID in the form of string
     * @param queryFile the filename in the form of string
     * @param socketId the ID of the socket to prevent the method from passing the query back to the source
     */
    public static void passOnResponse(String queryId, String ipAndPort, String queryFile, int socketId) {
        // If the response is already processed, skip
        for (String id : responseIdList) {
            if (id.equals(queryId))
                return;
        }

        // Passes on the response to the neighboring peers except the source
        for (TcpSocket socket : connectedTcpSockets) {
            if (socketId != socket.getId()) {
                socket.writeToPeer("R:" + queryId + ";" + ipAndPort + ";" + queryFile + '\n');
            }
        }

        // Mark the response as processed
        responseIdList.add(queryId);
    }

    /**
     * The static method that generates a new ID for the query
     * @return a 5-digit integer representing the query ID
     */
    public static int getNewId() {
        int randomId =  new Random().nextInt(90000) + 10000;
        sentQueries.add(randomId);
        return randomId;
    }
}

