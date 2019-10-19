import java.io.*;

public class p2p {
    public static void main(String[] args) throws InterruptedException {

        // Define the UDP socket port number
        int udpPort = 52322;

        // Greeting message
        System.out.println();
        System.out.println("Welcome to the Decentralized P2P Network!");
        System.out.println("Your current external IP Address is: " + NetworkUtil.getOwnExternalIp());
        System.out.println("UDP Port number: " + udpPort + "\n");

        // Starts the peer discovery protocol
        PeerTopology discoveryServer = new PeerTopology(udpPort);

        // Waits for 3 seconds for the response from the discovery protocol
        Thread.sleep(3000);

        // Meanwhile initializes the TCP sockets
        TcpSocketController.initTcpSockets();
        for (TcpSocket socket : TcpSocketController.availableTcpSockets) {
            System.out.println(socket.getPort());
        }

        // List the available peers obtained from the discovery protocol
        System.out.println();
        int peerCounter = 0;
        System.out.println("Available peers online: ");
        for (String availablePeerIPAddr : discoveryServer.getPeer()) {
            System.out.println(++peerCounter + ": " + availablePeerIPAddr);
        }

        // Command prompt below:
        System.out.println("\nEnter a command in the prompt below, or type \'help\': \n");
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("p2p> ");
            try {
                String command = input.readLine();
                Command.parseCommand(command);
            }
            catch (IOException e) {
                // An IOException is thrown when the input has no content. Therefore, the prompt is reprinted.
                continue;
            }
        }

    }
}
