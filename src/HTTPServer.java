import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class HTTPServer {
    static String APIhostname = "cloudometer-api.herokuapp.com";
    static int APIport = 80;

    private static byte[] ComposeGET(int status, String data) {
        StringBuilder s = new StringBuilder();
        switch (status) {
            case 1:
                s.append("GET /new?value=");
                break;
            case 2:
                s.append("GET /");
                break;
            default:
                break;
        }
        s.append(data);
        s.append(" HTTP/1.1\r\nHost: ");
        s.append(APIhostname);
        s.append("\r\nConnection: close\r\n\r\n");

        return s.toString().getBytes();
    }

    public static void main( String[] args) throws IOException {
        int bufferSize = 24;
        int serverPort = Integer.parseInt(args[0]);
        String ok = "OK\r\n";

        ServerSocket welcome = new ServerSocket(serverPort);

        while (true) {
            System.out.println("Ready to receive temperature data...");
            Socket connection = welcome.accept();

            byte[] receiveBuffer = new byte[bufferSize];

            int receivedDataLength = 0;

            receivedDataLength = connection.getInputStream().read(receiveBuffer); // Read from input stream to receive buffer and store length of data

            String tempValue = new String(receiveBuffer,0,receivedDataLength); // Save data as String
            System.out.println("Received data: " + tempValue); // Print data to terminal

            connection.getOutputStream().write(ok.getBytes(StandardCharsets.UTF_8)); // Return OK to ESP-01

            TCPClient callAPI = new TCPClient(false,0,0); // Create new TCP Client

            byte[] GETmsg = ComposeGET(2,tempValue);
            String GETstring = new String(GETmsg,0,GETmsg.length - 1);
            System.out.println(GETstring);
            byte[] responseBytes = callAPI.askServer(APIhostname, APIport, GETmsg); // Send GET to API host and store response
            String APIresponse = new String(responseBytes,0,responseBytes.length);
            System.out.println("API response: " + APIresponse);
            System.out.println("Closing connection");
            System.out.println();
            connection.close();
        }
    }
}

