#README

SYSC 3303 Assignment 4

Remote Procedure Calls

Author: Alexander Hum 101180821

Project Description:
The purpose of the following assignment is to develop a program that uses remote procedure calls so that the client and server interact by acknowledgingand replying with the intermediate host.

File Names:
Client: Client is responsible for sending data messages to the Intermediate and acknowledging that the Intermediate recieves the data message.
Intermediate: Receives a data message from the Client and waits for the request from the Server and send the data to the Server.
Server: Server sends a request data message to the Interemdiate and recieves and validates the data and send an acknowledgement.
Message: Helps print the output for the client, host, and server.
SharedData: SharedData interface that defines the implementation for the shared data
SharedDataImpl: The implementation of the SharedData class.

Question 1: It was suggested to use more than one thread for the implementation of the Intermediate task because they should be responsible for half of the communication between the (client-intermediate) and (intermediate-server). You would need two because the system must run concurrently.

Question 2: No it is not necessary to use synchronized in the intermediate task because the Intermediate does not have a critical section. There is no shared information within the Intermediate the shared data has its own class that holds the data.

Set up instructions:
  1. Unzip the assignment submission.
  2. Open up IntelliJ IDE
  3. Open up the project folder in Intellij IDE
  4. Click on "src" folder and open the Client java class, Host java class, and Server java class.
  5. Run the main method in each of the three classes as followed in order; Host -> Server -> Client.