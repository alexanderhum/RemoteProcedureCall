import java.io.IOException;
import java.net.*;
import java.rmi.Naming;

public class Server implements Runnable {
    /**
     * Datagram packets and sockets used for sending and receiving messages
     */
    private DatagramPacket receivePacket;
    private DatagramSocket sendReceiveSocket;
    private SharedData sharedData;

    public Server(SharedData sharedData) {
        this.sharedData = sharedData;
        try {
            // use a different DatagramSocket for each client
            sendReceiveSocket = new DatagramSocket();
            sendReceiveSocket.setSoTimeout(10000);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void sendAndReceive() {
        System.out.println("\nServer: Requesting the data from Intermediate");
        byte[] request = new String("server requesting data").getBytes();
        DatagramPacket requestPacket = null;
        try {
            requestPacket = new DatagramPacket(request, request.length, InetAddress.getLocalHost(), 69);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            sendReceiveSocket.send(requestPacket);
            System.out.println("Sent request to Intermediate Host");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        System.out.println("Server: Waiting for Packet.");

        // receives the datagram packet from the host
        try {
            sendReceiveSocket.receive(receivePacket);
            handleAcknowledgment(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * checks what request if it is a read or write request
     * @param byteArray that holds bytes that is used to send to and from client, host, and server
     * @return a byte[] with four bytes if it is a read or write request
     */
    private static byte[] validReadWriteRequest(byte[] byteArray) {
        if(byteArray[0]==0 && byteArray[1]==1) {
            return new byte[] {0, 3, 0, 1};
        } else if(byteArray[0]==0 && byteArray[1]==2) {
            return new byte[] {0, 4, 0, 0};
        } else {
            throw new IllegalArgumentException("Invalid request");
        }
    }
    /**
     * checks to see if the request is a valid request checking if the byte array starts, ends, and in the middle has a
     * zero byte. Also checks if the second byte is a 1 or 2.
     * @param byteArray that holds bytes that is used to send to and from client, host, and server
     * @return boolean returns true if the input is valid
     */
    private static boolean isValidRequest(byte[] byteArray) {
        boolean firstZero = byteArray[0] == 0;
        boolean middleZero = false;
        boolean secondByte = false;
        boolean lastZero = byteArray[byteArray.length-1] == 0;
        // if the byte array is null throw an exception
        if(byteArray == null) {
            throw new IllegalArgumentException("Invalid Request");
        }
        // if the first byte and last byte are not zero throw exception
        if(!firstZero || !lastZero) {
            throw new IllegalArgumentException("Invalid Request");
        }
        // returns true if the second byte is either a 1 or 2
        if(byteArray[1]==1 ||byteArray[1]==2) {
            secondByte = true;
        }
        // checks the middle of teh byte array to see if there is a zero byte
        for(int i=0; i<byteArray.length-1; i++) {
            if(byteArray[i]==0) {
                middleZero = true;
                break;
            }
        }
        // returns true if the input request is valid
        if(firstZero && secondByte && middleZero && lastZero) {
            return true;
        } else {
            throw new IllegalArgumentException("Invalid Request");
        }
    }

    public void rpc_send(DatagramPacket request, DatagramPacket response) {
        try {
            sendReceiveSocket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println(Thread.currentThread().getName() + ": Packet sent.\n");
    }
    private void handleAcknowledgment(DatagramPacket acknowledgmentPacket) {
        // Handle the acknowledgment packet received from the server
        System.out.println("Server: Acknowledgment received from server. ");
        System.out.print("Containing: ");
        byte[] temp = new byte[receivePacket.getLength()];
        // copy's the byte array from the datagram packet into the new byte array
        for (int i = 0; i < receivePacket.getLength(); i++) {
            temp[i] = acknowledgmentPacket.getData()[i];;
        }
        isValidRequest(temp);
        // creates a new byte array of four bytes that checks the validity of the request
        byte[] test = validReadWriteRequest(temp);
        Message.printByteToString(temp);
    }

    @Override
    public void run() {
        for(int i=0; i<11; i++) {
            sendAndReceive();
            // slows down by a second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void main(String args[]) {
        try {
            // Retrieve the shared data interface from the RMI registry
            SharedData sharedData = (SharedData) Naming.lookup("rmi://localhost/SharedData");

            // Create two instances of SimpleEchoClient
            Server server1 = new Server(sharedData);

            // Create two threads for the client instances
            Thread server1Thread1 = new Thread(server1, "Server 1");

            // Start both client threads
            server1Thread1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
