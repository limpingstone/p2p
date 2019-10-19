import java.net.*;
import java.io.*;
import java.util.*;

public class TcpSocket extends ServerSocket {
    // Port number of the socket
    private int port;

    // A unique ID to identify the individual sockets from one another
    private int id;

    // The field for storing the accepted socket
    private Socket connectionSocket;

    // Handles inbound data stream
    private BufferedReader incomingFromPeer;

    // Handles outbound data stream
    private DataOutputStream outboundToPeer;

    public TcpSocket(int port, int id) throws IOException {
        super(port);
        this.port = port;
        this.id = id;

        // Make the socket readily available in the background
        new Thread(new Runnable() {
            public void run() {
                connectedToPeer();
            }
        }).start();
    }

    /**
     * The method that initializes the socket connection with a peer
     * @param ipAddrStr hostname of the peer in the format of string
     * @param port port number of the peer in the format of int
     */
    public void connectToPeer(String ipAddrStr, int port) {

    }

    /**
     * The method that handles the established connection with a peer
     */
    public void connectedToPeer() {
        try {
            // Mark the socket as connected with the socket controller
            connectionSocket = this.accept();
            TcpSocketController.socketConnected(this.getId());
            System.out.println("Available Sockets:");
            for (TcpSocket socket : TcpSocketController.availableTcpSockets) {
                System.out.println(socket.getId());
            }
            System.out.println("Connected Sockets:");
            for (TcpSocket socket : TcpSocketController.connectedTcpSockets) {
                System.out.println(socket.getId());
            }
            System.out.println();

            // Set up the incoming and outbound stream of data
            incomingFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outboundToPeer = new DataOutputStream(connectionSocket.getOutputStream());

            // Wait for incoming stream from the peer
            while (parseStream(readFromPeer()) != null);
        }
        catch (IOException e) {
            System.out.println("TCP connection failed");
        }
    }

    /**
     * The method that handles the disconnection from the peer
     * @return true if the socket is disconnected without error
     */
    public boolean disconnectedFromPeer() {
        try {
            // Close the data stream
            incomingFromPeer.close();
            outboundToPeer.close();

            // Close the connection
            connectionSocket.close();
            System.out.println("Socket closed");

            // Remove the socket from the list of connected sockets
            TcpSocketController.socketDisconnected(this.getId());

            // Return true if the above process is handled successfully
            return true;
        }
        catch (IOException e) {
            System.out.println("Error closing socket");
        }
        return false;
    }

    public String readFromPeer() {
        try {
            return incomingFromPeer.readLine();
        }
        catch (IOException e) {
            System.out.println("Error reading from socket");
        }
        return null;
    }

    /**
     * The method that writes the message to peer
     * @param message the message in the format of string to be delivered to the peer
     */
    public void writeToPeer(String message) {
        try {
            outboundToPeer.writeBytes(message);
        }
        catch (IOException e) {
            System.out.println("Error writing to socket (ID " + getId() + ")" );
        }
    }

    /**
     * The method that processes the string received from the peer
     * @param streamStr a string received from the peer
     */
    public String parseStream(String streamStr) {
        System.out.println("Message from socket: " + streamStr);
        return streamStr;
    }

    public int getPort() {
        return port;
    }

    public int getId() {
        return id;
    }
}
