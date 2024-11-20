import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class Client implements Runnable {
    /**
     * Datagram packets and sockets used for sending and receiving messages
     */
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;

    /**
     * constructor for the Client
     */
    public Client() {
        try {
            sendReceiveSocket = new DatagramSocket();
            // timeouts after 5 seconds
            sendReceiveSocket.setSoTimeout(5000);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Sends and receives messages to the Intermediate
     */
    public void sendAndReceive() {
        // loops through 11 total requests and changes the byte each time if i is odd or even
        for(int i=0; i<=10; i++) {
            byte readWriteInvalidByte;
            if (i == 10) {
                readWriteInvalidByte = 0;
            } else if (i % 2 == 0) {
                readWriteInvalidByte = 1;
            } else {
                readWriteInvalidByte = 2;
            }
            //initializes variables and bytes
            Byte zeroByte = 0;
            String s = "test" + i + ".txt";
            String netasciiName = "netascii";

            // gets the bytes from the text strings above
            byte[] sBytes = s.getBytes();
            byte[] netasciiNameBytes = netasciiName.getBytes();

            // creates and arraylist of bytes and adds the bytes from the text strings
            // ArrayList is able to manipulated and changed if it is a read or write request
            // formats the bytes in the required order
            ArrayList<Byte> byteArrayList = new ArrayList<>();
            byteArrayList.add(zeroByte);
            byteArrayList.add(readWriteInvalidByte);
            for (byte b : sBytes) {
                byteArrayList.add(b);
            }
            byteArrayList.add(zeroByte);
            for (byte x : netasciiNameBytes) {
                byteArrayList.add(x);
            }
            byteArrayList.add(zeroByte);

            // to be able to send using a packet must be converted into a byte array
            byte[] msg = new byte[byteArrayList.size()];
            for (int z = 0; z < byteArrayList.size(); z++) {
                msg[z] = byteArrayList.get(z);
            }

            // create the packet with the data message
            try {
                sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23);
                // initialize receivePacket before using it
                byte[] receiveData = new byte[100];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.out.print("Client sending a packet to intermediate host containing: ");
            Message.printByteToString(msg);

            // perform sending and receiving with timeout handling
            int attempt = 0;
            boolean receivedResponse = false;
            while (attempt < 3 && !receivedResponse) { // retry up to 3 times
                System.out.println("Client Attempt " + (attempt + 1) + "...");
                rpc_send(sendPacket, receivePacket);

                try {
                    sendReceiveSocket.receive(receivePacket); // attempt to receive the acknowledgment
                    handleAcknowledgment(receivePacket);
                    receivedResponse = true;
                } catch (SocketTimeoutException ste) {
                    // handle timeout exception
                    System.out.println("Client timeout. Resending packet...");
                    attempt++;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            if (!receivedResponse) {
                System.out.println("Client no response after multiple attempts. Exiting...");
                return;
            }

            // slows down by a second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
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
        System.out.println("Client: Acknowledgment received from Intermediate Host.");
        System.out.print("Containing: ");
        byte[] temp = new byte[receivePacket.getLength()];
        // copy's the byte array from the datagram packet into the new byte array
        for (int i = 0; i < receivePacket.getLength(); i++) {
            temp[i] = acknowledgmentPacket.getData()[i];;
        }
        Message.printByteToString(temp);
    }

    @Override
    public void run() {
        sendAndReceive();
        sendReceiveSocket.close();
    }

    public static void main(String args[]) {
        try {
            // Create two instances of SimpleEchoClient
            Client client = new Client();

            // Create two threads for the client instances
            Thread clientThread = new Thread(client, "Client");

            // Start both client threads
            clientThread.start();
            //clientThread2.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
