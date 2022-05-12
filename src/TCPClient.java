import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {
    int bufferSize = 1024;
    boolean shutdown;
    Integer timeout, limit;

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;
    }

    public byte[] askServer(String hostname, int port) throws IOException {
        byte[] receiveBuffer = new byte[bufferSize];  // Intermediate buffer for received data from server
        int receivedDataLength = 0;             // Length of received data from server
        int receivedDataLimit = 0;

        Socket clientSocket = new Socket(hostname, port);   // Create client socket and connect to the server
        ByteArrayOutputStream fromServerBytes = new ByteArrayOutputStream();    // Create dynamic byte array for all received data from server

        clientSocket.setKeepAlive(shutdown);        // Set SOKeepAlive to shutdown variable value

        if(timeout != null && timeout > 0) {        // Set connection timeout if a valid timeout parameter is given
            clientSocket.setSoTimeout(timeout);
        }

        if(clientSocket.getKeepAlive()) {               // If shutdown is true, the client closes the outgoing connection after sending
            clientSocket.shutdownOutput();
        }

        while(receivedDataLength != -1) {                // Receive data from server into intermediate buffer and record the length of that data as long as there is data left to receive
            try {
                receivedDataLength = clientSocket.getInputStream().read(receiveBuffer);
            } catch (SocketTimeoutException timedOut) {
                clientSocket.close();
                System.err.println(timedOut);
                return fromServerBytes.toByteArray();
            }
            receivedDataLimit += receivedDataLength;
            if(receivedDataLength != -1) {              // Write data from intermediate buffer into dynamic byte array if there is more data to write
                if(limit > 0 && receivedDataLimit > limit) {         // If limit is reached, write remaining data to dynamic byte array and then end receiving loop
                    fromServerBytes.write(receiveBuffer, 0, limit % bufferSize);
                    break;
                } else {                                // If limit is not reached, write data to dynamic byte array and continue receiving loop
                    fromServerBytes.write(receiveBuffer, 0, receivedDataLength);
                }
            }
        }

        return fromServerBytes.toByteArray();   // Return a byte array of all the received data in the dynamic byte array
    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        byte[] receiveBuffer = new byte[bufferSize];  // Intermediate buffer for received data from server
        int receivedDataLength = 0;             // Length of received data from server
        int receivedDataLimit = 0;

        Socket clientSocket = new Socket(hostname, port);   // Create client socket and connect to the server
        ByteArrayOutputStream fromServerBytes = new ByteArrayOutputStream();    // Create dynamic byte array for all received data from server

        clientSocket.setKeepAlive(shutdown);        // Set SOKeepAlive to shutdown variable value

        if(timeout != null && timeout > 0) {        // Set connection timeout if a valid timeout parameter is given
            clientSocket.setSoTimeout(timeout);
        }

        clientSocket.getOutputStream().write(toServerBytes, 0, toServerBytes.length); // Send input data to server

        if(clientSocket.getKeepAlive()) {               // If shutdown is true, the client closes the outgoing connection after sending
            clientSocket.shutdownOutput();
        }

        while(receivedDataLength != -1) {                // Receive data from server into intermediate buffer and record the length of that data as long as there is data left to receive
            try {
                receivedDataLength = clientSocket.getInputStream().read(receiveBuffer);
            } catch (SocketTimeoutException timedOut) {
                clientSocket.close();
                System.err.println(timedOut);
                return fromServerBytes.toByteArray();
            }
            receivedDataLimit += receivedDataLength;
            if(receivedDataLength != -1) {              // Write data from intermediate buffer into dynamic byte array if there is more data to write
                if(limit > 0 && receivedDataLimit > limit) {         // If limit is reached, write remaining data to dynamic byte array and then end receiving loop
                    fromServerBytes.write(receiveBuffer, 0, limit % bufferSize);
                    break;
                } else {                                // If limit is not reached, write data to dynamic byte array and continue receiving loop
                    fromServerBytes.write(receiveBuffer, 0, receivedDataLength);
                }
            }
        }

        return fromServerBytes.toByteArray();   // Return a byte array of all the received data in the dynamic byte array
    }
}
