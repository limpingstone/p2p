import java.io.*;
import java.util.*;

public class TcpSocketController {
    // The array list of available TCP sockets that has not yet being connected
    public static ArrayList<TcpSocket> availableTcpSockets = new ArrayList<TcpSocket>();

    // The array list of TCP sockets that are in active connection with a peer
    public static ArrayList<TcpSocket> connectedTcpSockets = new ArrayList<TcpSocket>();

    /**
     * The static method that initializes all the TCP sockets and add them into the array list of available TCP sockets
     * The IDs are for the TCP sockets to self identify its position in the array list
     */
    public static void initTcpSockets() {
        try {
            int id = 1000;
            for (Integer portNum : ParseFile.getConfigPorts()) {
                availableTcpSockets.add(new TcpSocket(portNum.intValue(), id++));
            }
        }
        catch (IOException e) {
            System.out.println("TCP Socket Error");
        }
    }

    /**
     * The method that returns the next available socket in the list of available sockets
     * @return a TCP socket available for connection
     */
    public static TcpSocket getNextAvailableSocket() {
        return availableTcpSockets.get(0);
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
        connectedTcpSockets.add(connectedSocket);
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
        availableTcpSockets.add(availableSocket);
    }

    /**
     * The static method that disconnects all connections in the list of connected sockets
     * This is done when 'leave' or 'exit' is called from the prompt
     */
    public static void disconnectAllSockets() {
        // Disconnect all sockets and add them to the list of available sockets
        for (TcpSocket socket : connectedTcpSockets) {
            socket.disconnectedFromPeer();
            availableTcpSockets.add(socket);
        }

        // Empty the list of connected sockets
        connectedTcpSockets = new ArrayList<TcpSocket>();
    }
}
