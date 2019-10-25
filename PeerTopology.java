import java.util.*;
import java.net.*;
import java.io.*;

public class PeerTopology {

    // N = 16
    // port ranges from 52320 to 52339
    // Using a universal port 52322 among the peers
    private int port;

    // The UDP Socket for the protocol
    private DatagramSocket udpSocket;

    // A buffered list of packets listing available hosts to be processed
    private ArrayList<DatagramPacket> peerPacketBuffer;

    // A string of available hosts
    private ArrayList<String> availablePeers = new ArrayList<String>();

    // A string of available hosts with port numbers
    private ArrayList<String> availablePeerPorts = new ArrayList<String>();

    public PeerTopology(int port) {
        this.port = port;
        this.peerPacketBuffer = new ArrayList<DatagramPacket>();
        this.availablePeers = new ArrayList<String>();
        this.availablePeerPorts = new ArrayList<String>();

        // Leave the UDP peer discovery channel available in the background
        new Thread(new Runnable() {
            public void run() {
                socketInit();
            }
        }).start();

        // Send out the UDP discovery packet to the broadcast address
        broadcast();
    }

    /**
     * The function that initializes the UDP socket
     */
    public void socketInit() {
        try {
            // Creating the UDP socket
            udpSocket = new DatagramSocket(port);
            udpSocket.setBroadcast(true);

            // Set up the socket readily available for peer discovery
            // If the request buffer has packets, the program parses the packets
            System.out.println("UDP Socket Initialized, waiting for UDP response from other peers...");
            while (receivedPacket());
        }
        catch (SocketException e) {
            System.out.println("Error accessing socket on port " + port);
        }
        catch (IOException e) {
            System.out.println("Error receiving incoming message from socket on port " + port);
        }
    }

    /**
     * The function that broadcasts the discovery message
     */
    public void broadcast() {
        try {
            // Wait 0.5 sec to initialize the server
            Thread.sleep(500);

            // Define the broadcast IP address
            InetAddress broadcastIP = InetAddress.getByName("255.255.255.255");

            // Compose the peer discovery message and create the packet
            byte[] peerDiscoveryReq = "Discovery Msg".getBytes();
            DatagramPacket peerDiscoveryPacket = new DatagramPacket(peerDiscoveryReq, peerDiscoveryReq.length, broadcastIP, port);

            // Send out the peer discovery packet to the broadcast IP address
            System.out.println("Broadcasting UDP discovery message...");
            udpSocket.send(peerDiscoveryPacket);
        }
        catch (UnknownHostException e) {
            System.out.println("Error: failed to broadcast to destination 255.255.255.255");
        }
        catch (IOException e) {
            System.out.println("Error receiving incoming message from socket on port " + port);
        }
        catch (InterruptedException e) {
            System.out.println("Error: Failed to initialize thread");
        }
        catch (NullPointerException e) {
            System.out.println("Error: Check if the socket is already in use");
        }
    }

    /**
     * The function that receives the incoming packet from the socket
     * @return true when the packet properly received and queued in the appropriate buffer
     * @throws IOException when the socket cannot properly receive the packet data
     * @throws SocketException when the socket cannot be accessed
     */
    public boolean receivedPacket() throws IOException, SocketException {

        // Create the packet for the incoming message and receive the packet
        byte[] rxData = new byte[2048];
        DatagramPacket incomingPacket = new DatagramPacket(rxData, rxData.length);
        udpSocket.receive(incomingPacket);

        // Parse the packet if it is a request for ACK, or queue the ACK into the buffer
        if (new String(incomingPacket.getData()).charAt(0) == 'D')
            parseRequestPacket(incomingPacket);
        else
            peerPacketBuffer.add(incomingPacket);

        // returns true if the above process is successful
        return true;
    }

    /**
     * Parse the packets in the request buffer by sending out an acknowledgment
     * @param packet the packet received from the socket with the discovery message
     */
    public void parseRequestPacket(DatagramPacket packet) {
        sendAck(packet.getAddress(), packet.getPort());
    }

    /**
     * Parse the packets in the peer buffer by translating the addresses and ports into array lists of String
     */
    public void parseAllPeerPackets() {
        for (DatagramPacket packet : peerPacketBuffer) {

            // The IP address of the sender who sent the ACK
            availablePeers.add(packet.getAddress().toString().substring(1));

            // The port number is included in the data portion of the ACK
            availablePeerPorts.add(new String(packet.getData()));
        }
    }

    /**
     * The method that sends an acknowledgment back to the sender
     * @param ipAddr the IP address of the sender in the format of InetAddress
     * @param udpPort the port number used by the sender in the format of int
     */
    public void sendAck(InetAddress ipAddr, int udpPort) {
        try {
            // Update the current list of available ports from the TCP socket controller
            //ParseFile.updateAvailablePorts();

            // For each available port on the local machine, the protocol sends out an acknowledgment back to the remote peer
            for (TcpSocket tcpPort : TcpSocketController.availableTcpSockets) {

                // The data sent contains the available port number
                byte[] peerData = Integer.toString(tcpPort.getPort()).getBytes();
                DatagramPacket availablePeerPacket = new DatagramPacket(peerData, peerData.length, ipAddr, udpPort);
                udpSocket.send(availablePeerPacket);
            }
        }
        catch (UnknownHostException e) {
            System.out.println("Error: failed to send packet to destination " + ipAddr.toString().substring(1));
        }
        catch (IOException e) {
            System.out.println("Error receiving incoming message from socket on port " + port);
        }
        catch (NullPointerException e) {
            System.out.println("Error: Check if the socket is already in use");
        }
    }

    /**
     * The method that returns an array list of formatted IP addresses and ports of available peers
     * @return an array list of string that contains the information of available peers
     */
    public ArrayList<String> getPeer() {
        parseAllPeerPackets();
        ArrayList<String> availablePeersFormatted = new ArrayList<String>();
        for (int i = 0; i < peerPacketBuffer.size(); i++) {
            String ipAddrPortFormatted = "";

            // Remove the packet sent back to oneself by the broadcast
            // The string format is as follows: "<IP address>:<port>"
            if (!availablePeers.get(i).equals(NetworkUtil.getOwnExternalIp())) {
                ipAddrPortFormatted += availablePeers.get(i) + ":" + availablePeerPorts.get(i);
                availablePeersFormatted.add(ipAddrPortFormatted);
            }
        }
        return availablePeersFormatted;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
