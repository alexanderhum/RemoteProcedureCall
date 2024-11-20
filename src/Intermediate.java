import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;

public class Intermediate implements Runnable {
    /**
     * DatagramPackets for sending and receiving data.
     */
    private DatagramPacket sendPacket, receivePacket, receivePacketServer;

    /**
     * DatagramSockets for sending and receiving UDP packets.
     */
    private DatagramSocket sendSocket, receiveSocket, receiveSocketServer;

    /**
     * SharedDataInterface for interacting with shared data through RMI.
     */
    private SharedData sharedData;
    /**
     * HashSet used for duplicateRequests
     */
    private static HashSet<Integer> duplicateHashes = new HashSet<>();

    public Intermediate(SharedData sharedData) {
        this.sharedData = sharedData;
        try {
            sendSocket = new DatagramSocket();
            // set the socket ports
            receiveSocket = new DatagramSocket(23);
            receiveSocketServer = new DatagramSocket(69);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void receiveAndEcho() {
        byte[] data = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("\nIntermediate waiting for packet...");

        // receive the packet
        try {
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // check for duplicate requests and respond accordingly
        if (isDuplicateRequest(receivePacket.getData())) {
            System.out.println("[" + Thread.currentThread().getName() + "] Duplicate request received. Ignoring...");
            return; // will not send an acknowledgement if the request was received twice
        }

        // process the received datagram
        System.out.print("Intermediate: Packet received containing: ");
        String received = new String(data, 0, receivePacket.getLength());

        byte temp[] = new byte[receivePacket.getLength()];
        for (int j = 0; j < receivePacket.getLength(); j++) {
            temp[j] = data[j];
        }
        Message.printByteToString(temp);

        // store the received message in shared data
        try {
            sharedData.addMessage(received);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Intermediate: Message stored in SharedData");

        // send an acknowledgment to the client
        sendAcknowledgment(receivePacket);

        byte[] dataRequest = new byte[100];
        receivePacketServer = new DatagramPacket(dataRequest, dataRequest.length);
        System.out.println("Intermediate waiting for packet...");

        // receive the packet
        try {
            receiveSocketServer.receive(receivePacketServer);
            System.out.println("Intermediate Host: Received request from Server");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        sendPacket = new DatagramPacket(temp, temp.length,
                receivePacketServer.getAddress(), receivePacketServer.getPort());

        try {
            sendSocket.send(sendPacket);
            System.out.println("Sent packet with data to Server");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void sendAcknowledgment(DatagramPacket receivedPacket) {
        // extract the content of the received packet
        String received = new String(receivedPacket.getData(), 0, receivedPacket.getLength());

        // construct acknowledgment data including the content of the received packet
        byte[] acknowledgmentData = ("ACK " + received).getBytes();

        // create a DatagramPacket for the acknowledgment
        sendPacket = new DatagramPacket(acknowledgmentData, acknowledgmentData.length,
                receivedPacket.getAddress(), receivedPacket.getPort());

        // send the acknowledgment packet
        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1); // consider handling this exception more gracefully in a production environment
        }
        System.out.println("Acknowledgment sent");

        String receive = null;
        try {
            receive = sharedData.getMessage();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        byte[] receivedFromSharedData = receive.getBytes();
        System.out.print("Intermediate gets the following from the SharedData: " );
        Message.printByteToString(receivedFromSharedData);
    }


    private boolean isDuplicateRequest(byte[] requestData) {
        // creates a hash for the requestData
        int hash = java.util.Arrays.hashCode(requestData);

        // checks if the hash contains
        if (duplicateHashes.contains(hash)) {
            return true;
        } else {
            duplicateHashes.add(hash);
            return false;
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < 11; i++) {
            receiveAndEcho();
        }
        sendSocket.close();
        receiveSocket.close();
    }

    public static void main(String[] args) {
        try {
            SharedDataImpl sharedData = new SharedDataImpl();

            // Create and export the RMI registry
            LocateRegistry.createRegistry(1099);

            // Bind the remote object's stub in the registry
            Naming.rebind("SharedData", sharedData);

            Intermediate intermediate = new Intermediate(sharedData);

            Thread serverThread = new Thread(intermediate);
            serverThread.start();

            System.out.println("Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
