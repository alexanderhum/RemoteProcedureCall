import java.util.Arrays;

public class Message {
    /**
     * prints out a user-friendly text of the bytes given byte array
     * @param data a byte[] that stores the bytes that passes from client, host, and server
     */
    public static void printByteFromArray(byte[] data) {
        // loops and prints out the bytes from the array
        for (byte b : data) {
            System.out.print(b + " ");
        }
    }

    /**
     * prints out a user-friendly text of the string given the byte array
     * @param data a byte[] that stores the bytes that passes from client, host, and server
     */
    public static void printByteToString(byte[] data) {
        // copy's the byte array into a new array to not manipulate the old data
        byte[] temp = Arrays.copyOf(data, data.length);
        // helps convert the decimal to a character
        for(int i=0; i< temp.length; i++) {
            if(temp[i] <= 0b10) {
                temp[i] = (byte) (temp[i] + 48);
            }
        }
        System.out.println(new String(temp));
    }
}
