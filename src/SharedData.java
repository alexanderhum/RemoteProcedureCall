import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SharedData extends Remote {
    /**
     * Retrieves a message from the shared data.
     *
     * @return a String representing the retrieved message.
     * @throws RemoteException if a communication-related exception occurs during the remote method invocation.
     */
    String getMessage() throws RemoteException;

    /**
     * Adds a message to the shared data.
     *
     * @param message a String representing the message to be added to the shared data.
     * @throws RemoteException if a communication-related exception occurs during the remote method invocation.
     */
    void addMessage(String message) throws RemoteException;
}
