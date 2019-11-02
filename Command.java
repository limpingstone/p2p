import java.net.*;
import java.io.*;

public class Command {

    // The static field containing the current user input
    private static String[] param;

    /**
     * The static method that parses the current command input by the user
     * @param command the command in the form of a string
     */
    public static void parseCommand(String command) {
        parseArguments(command);
        switch (param[0]) {
            // returns back to the prompt
            case "":
                return;

            // list the available sockets and connected sockets based on the config ports
            case "list":
                System.out.println("Available Sockets:");
                for (TcpSocket socket : TcpSocketController.availableTcpSockets) {
                    System.out.println(socket.getId());
                }
                System.out.println("Connected Sockets:");
                for (TcpSocket socket : TcpSocketController.connectedTcpSockets) {
                    System.out.println(socket.getId());
                }
                break;

            // find file in obtained folder
            case "findfile":
                ParseFile.fileFound("send_me.txt");
                break;

            // connect to a peer with the provided IP address and port
            case "Connect":
            case "connect":
                try {
                    connect(param[1], param[2]);
                }
                catch (IndexOutOfBoundsException e) {
                    help(true);
                }
                break;

            // send to all neighboring peers a query of the filename
            case "Get":
            case "get":
                if (param.length > 1)
                    getFile(param[1]);
                else
                    help(true);
                break;

            // disconnects all connection with peers
            case "Leave":
            case "leave":
                TcpSocketController.disconnectAllSockets();
                System.out.println("Closed all active TCP connection");
                break;

            // disconnects all connection with peers and exit the program
            case "Exit":
            case "exit":
                TcpSocketController.disconnectAllSockets();
                System.out.println("Have a great day. Bye!");
                System.exit(0);

            // prints the help section
            case "Help":
            case "help":
                help(false);
                break;

            // when a command is not found
            default:
                System.out.println("Error: " + command + ": command not found");
        }
    }

    /**
     * The static method that processes the arguments and stores them into an array of strings
     * @param args the user input to be split into an array
     */
    public static void parseArguments(String args) {
        param = args.split(" ");
    }

    /**
     * The static method that prints out the help section
     * @param versionHelp the boolean parameter that indicates whether to print the version information
     */
    public static void help(boolean versionHelp) {
        System.out.println();
        if (!versionHelp) {
            System.out.println("P2P Network - version 0.1");
            System.out.println("Developed by Steven Chen Hao Nyeo");
            System.out.println("Case Western Reserve University - 2019\n");
        }
        System.out.println("List of commands: ");
        System.out.println("Connect [ip-address] [port] - connect to the peer with the designated IP address and port number");
        System.out.println("Get - ");
        System.out.println("Leave - close all TCP connections with neighboring peers");
        System.out.println("Help - print this help menu");
        System.out.println("Exit - close all TCP connections and terminates the program\n");
    }

    /**
     * The static method that connects an available socket to the peer socket with the provided IP address and port
     * @param ipAddrStr the IP address in the format of a string
     * @param portStr the port number in the format of a string
     */
    public static void connect(String ipAddrStr, String portStr) {
        System.out.println("Connecting to peer...");
        TcpSocket peerSocket = TcpSocketController.getNextAvailableSocket();
        peerSocket.connectToPeer(ipAddrStr, Integer.parseInt(portStr));
    }

    /**
     * The static method that handles with sending queries for file transfer
     * @param filename a string representing the filename of the requested file
     */
    public static void getFile(String filename) {
        for (TcpSocket activeSocket : TcpSocketController.connectedTcpSockets) {
            System.out.println("Sent query to " + activeSocket.getLocalPort() + ". Waiting for response...");
            activeSocket.writeToPeer("Q:" + TcpSocketController.getNewId() + ";" + filename + '\n');
        }
    }
}

