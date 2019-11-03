import java.net.*;
import java.io.*;
import java.util.*;

public class TcpSocket extends ServerSocket {
    // Port number of the socket
    private int localPort;

    // A unique ID to identify the individual sockets from one another
    private int id;

    // The field for storing the accepted socket
    private Socket connectionSocket;

    // Handles inbound data stream
    private BufferedReader incomingFromPeer;

    // Handles outbound data stream
    private DataOutputStream outboundToPeer;

    public TcpSocket(int localPort, int id) throws IOException {
        super(localPort);
        this.localPort = localPort;
        this.id = id;

        // Make the socket readily available in the background
        new Thread(new Runnable() {
            public void run() {
                connectedToPeer();
            }
        }).start();
    }

    /**
     * The method that actively initializes the socket connection with a peer
     * @param ipAddrStr hostname of the peer in the format of string
     * @param port port number of the peer in the format of int
     */
    public void connectToPeer(String ipAddrStr, int port) {
        try {
            connectionSocket = new Socket(ipAddrStr, port);
            TcpSocketController.socketConnected(this.getId());
            System.out.println("Connected to peer at " + ipAddrStr + " on port " + port + "!");

            new Thread(new Runnable() {
                public void run() {
                    setupDataTransfer();
                }
            }).start();
        }
        catch (IOException e) {
            System.out.println("TCP connection failed");
        }
    }

    /**
     * The method that handles the incoming established connection with a peer
     */
    public void connectedToPeer() {
        try {
            // Mark the socket as connected with the socket controller
            // If the TCP socket is connected by the user, the socket cannot be used by other peers
            // If the TCP socket is not connected, the socket can accept from other peers
            if (connectionSocket == null) {
                connectionSocket = this.accept();
            }
            TcpSocketController.socketConnected(this.getId());
            System.out.println("Accepted connection on port " + getLocalPort());

            setupDataTransfer();
        }
        catch (IOException e) {
            System.out.println("TCP connection failed. Please try another peer or port");
        }
    }

    public void setupDataTransfer() {
        try {
            // Set up the incoming and outbound stream of data
            incomingFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outboundToPeer = new DataOutputStream(connectionSocket.getOutputStream());

            // Wait for incoming stream from the peer
            while (parseStream(readFromPeer()) != null);

        }
        catch (IOException e) {
            System.out.println("Error setting up data transmission");
        }
    }

    /**
     * The method that handles the disconnection from the peer
     * @return true if the socket is disconnected without error
     */
    public boolean disconnectedFromPeer() {
        try {
            // If connectionSocket is linked with other peers
            if (connectionSocket != null) {

                // Close the connection
                // Has to be done before closing the data stream so the socket could send a null packet to terminate the readline method
                connectionSocket.close();

                // Close the data stream
                incomingFromPeer.close();
                outboundToPeer.close();

                // Remove the socket from the list of connected sockets
                TcpSocketController.socketDisconnected(this.getId());

                // Return true if the above process is handled successfully
                return true;
            }
        }
        catch (IOException e) {
            System.out.println("Error closing socket");
        }
        return false;
    }

    /**
     * The method that reads messages from peer
     * @return a string read from the incoming string reader, or null if the socket is closed
     */
    public String readFromPeer() {
        try {
            // Return the message sent from peer
            return incomingFromPeer.readLine();
        }
        catch (IOException e) {
            // Silently handled. After all, the user can always check with commands
            System.out.println("Peer socket on port " + getLocalPort()  + " closed");
        }

        // Return null if the socket to peer is closed
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
            System.out.println("Error writing to socket (ID " + getId() + ")");
        }
    }

    /**
     * The method that processes the string received from the peer
     * @param streamStr a string received from the peer
     */
    public String parseStream(String streamStr) {
        if (streamStr != null) {
            System.out.println("\nMessage from port " + getLocalPort() + ": " + streamStr);
            try {
                // Strings to be deployed to spawning the query flood
                String queryId = streamStr.substring(2).split(";")[0];
                String queryFile = parseFileName(streamStr);

                // Differentiating between query and response
                if (streamStr.charAt(0) == 'Q') {
                    TcpSocketController.passOnQuery(queryId, queryFile, getId());
                }
                else if (streamStr.charAt(0) == 'R') {
                    String ipAndPort = streamStr.substring(2).split(";")[1];
                    TcpSocketController.passOnResponse(queryId, ipAndPort, queryFile, getId());

                    // If the first response is found, the ID is removed and the file socket is set up
                    int index = -1;
                    for (Integer id : TcpSocketController.sentQueries) {
                        ++index;
                        if (queryId.equals(id.toString())) {
                            // Gather IP address and port information for file socket
                            String ip = ipAndPort.split(":")[0];
                            int port = Integer.parseInt(ipAndPort.split(":")[1]);
                            setupFileTransfer(ip, port, queryFile);
                        }
                    }
                    // Mark the query as done
                    TcpSocketController.sentQueries.remove(index);
                }
                else if (streamStr.charAt(0) == 'T') {
                    String filename = streamStr.split(":")[1];
                    File fileToPeer = new File("shared/" + filename);
                    InputStream fileContentStream = new FileInputStream(fileToPeer);

                    // Writing the bytes to the peer while reading the file
                    int count;
                    byte[] fileBytes = new byte[(int) fileToPeer.length()];
                    System.out.println("Writing to peer...");
                    while ((count = fileContentStream.read(fileBytes)) > 0) {
                        outboundToPeer.write(fileBytes);
                    }
                    System.out.println("File transfer DONE");

                    // Close the socket and the data streams after the file has been transferred
                    connectionSocket.close();
                    outboundToPeer.close();
                    incomingFromPeer.close();

                    // Make the connected socket available
                    TcpSocketController.socketDisconnected(this.getId());
                }
            }
            // Check for garbled queries and responses
            catch (IndexOutOfBoundsException e) {
                // Silent fail, should not be the fault of the sender
                System.out.println("1 incomplete query ignored");
            }
            catch (IOException e) {
                // Occurs during the case of T
                System.out.println("File transfer failed, please try again");
            }
        }
        return streamStr;
    }

    /**
     * The method that creates a new file socket and transfers the file according to the information provided by the peer
     * @param ipAddr the IP address in the form of a string
     * @param port the port number in the form of int
     * @param filename the name of the file in the form of a string
     */
    public void setupFileTransfer(String ipAddr, int port, String filename) {
        try {
            // Set up the socket
            Socket fileSocket = new Socket(ipAddr, port);

            // Set up the inbound and outbound data stream
            DataOutputStream fileQueryToPeer = new DataOutputStream(fileSocket.getOutputStream());
            DataInputStream fileFromPeer = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));

            // Write the file query to the peer and wait for the file
            System.out.println("Requesting peer on " + ipAddr + " for " + filename);
            String message = "T:" + filename + '\n';
            fileQueryToPeer.writeBytes(message);

            System.out.println("Reading file from port " + fileSocket.getPort() + "...");
            // Write the content to the file while reading from the incoming stream
            int count;
            byte[] fileBytes = new byte[4096];
            FileOutputStream toObtained = new FileOutputStream(new File("obtained/" + filename));
            while ((count = fileFromPeer.read(fileBytes)) > 0) {
                toObtained.write(fileBytes);
            }

            // Close the socket
            fileSocket.close();
            fileQueryToPeer.close();
            fileFromPeer.close();

            System.out.println("File successfully transferred! Press enter to continue");
        }
        catch (IOException e) {
            System.out.println("File transfer failed, please try again");
        }
    }

    /**
     * The method that parses the file name and checks for extra carriage returns
     * @param filename the last argument of a query
     * @return a string with clean file name without carriage returns
     */
    public String parseFileName(String streamStr) {
        if (streamStr.charAt(streamStr.length() - 1) == '\n')
            streamStr = streamStr.substring(0, streamStr.length() - 1);
        String[] splitStr = streamStr.split(";");
        return splitStr[splitStr.length - 1];
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getId() {
        return id;
    }

    public Socket getConnectionSocket() {
        return connectionSocket;
    }
}
