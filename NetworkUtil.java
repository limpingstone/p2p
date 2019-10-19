import java.net.*;
import java.io.*;

public class NetworkUtil {

    private static URL checkIpUrl;

    /**
     * The static method that retieves the external IP of the local machine
     * This has to be done remotely, and in this case, we default the remote server to checkip.amazonaws.com
     * @return the external IP address of the local machine in the form of String
     */
    public static String getOwnExternalIp() {
        try {
            checkIpUrl = new URL("http://checkip.amazonaws.com");
            BufferedReader ipReader = new BufferedReader(new InputStreamReader(checkIpUrl.openStream()));

            return ipReader.readLine();
        }
        catch (UnknownHostException | MalformedURLException e) {
            getOwnExternalIpError();
        }
        catch (IOException e) {
            getOwnExternalIpError();
        }
        return null;
    }

    /**
     * The static method that is called when the above method fails to retrieve the external IP address
     * @return the fallback IP address (Possibly local/internal) of the local machine in the form of String
     */
    public static String getOwnExternalIpError() {
        try {
            return InetAddress.getLocalHost().getAddress().toString();
        }
        catch (Exception e) {
            System.out.println("Error: Please check your internet connection.");
            System.exit(0);
        }
        return null;
    }
}
