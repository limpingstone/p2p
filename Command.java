import java.net.*;
import java.io.*;

public class Command {
    private static String[] param;

    public static void parseCommand(String command) {
        parseArguments(command);
        switch (param[0]) {
            case "":
                return;
            case "Connect":
            case "connect":
                try {
                    connect(param[1], param[2]);
                }
                catch (IndexOutOfBoundsException e) {
                    help(true);
                }
                break;

            case "Get":
            case "get":
                break;

            case "Leave":
            case "leave":
                TcpSocketController.disconnectAllSockets();
                break;

            case "Exit":
            case "exit":
                TcpSocketController.disconnectAllSockets();
                System.out.println("Have a great day. Bye!");
                System.exit(0);

            case "Help":
            case "help":
                help(false);
                break;

            default:
                System.out.println("Error: " + command + ": command not found");
        }
    }

    public static void parseArguments(String args) {
        param = args.split(" ");
    }

    public static void help(boolean plainHelp) {
        System.out.println();
        if (!plainHelp) {
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

    public static void connect(String ipAddrStr, String portStr) {
        //try {
            System.out.println("Connecting to peer...");
            TcpSocket peerSocket = TcpSocketController.getNextAvailableSocket();
            peerSocket.connectToPeer(ipAddrStr, Integer.parseInt(portStr));
            System.out.println("Connected!!!");
        /*}
        catch (UnknownHostException e) {
            System.out.println("Error: unknown host " + ipAddrStr + " on port " + portStr);
        }
        catch (IOException e) {
            System.out.println("Error: Socket connection failed");
        }
        */
    }
}
