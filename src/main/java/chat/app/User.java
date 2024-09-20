package chat.app;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class User {

    private static final Logger logger = Logger.getLogger(User.class.getName());
    static Socket userSocket;
    static BufferedReader in;
    static BufferedWriter out;
    static BufferedReader userInput;

    public void start(String ip, int port) {
        try{
            userSocket = new Socket(ip,port);

            in = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));
            userInput = new BufferedReader(new InputStreamReader(System.in));

            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Connection to server lost.");
                }
            }).start();

            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.write(userMessage);
                out.newLine();
                out.flush();

                if (userMessage.equalsIgnoreCase("/logout")) {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unknown Host: Please verify the server address.", e);
        } catch (ConnectException e) {
            logger.log(Level.SEVERE, "Connection refused: Is the server running?", e);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket error occurred: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error occurred while communicating with the server.", e);
        } finally {
            stop();
        }
    }
    public void stop(){
        try {
            if (userInput != null) userInput.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (userSocket != null) userSocket.close();
            System.out.println("Disconnected from the server.");
        }catch (IOException e) {
            logger.log(Level.WARNING, "Error closing resources.", e);
        }
    }
    public static void main(String[] args) {
        User user = new User();
        user.start("localhost", 1234);
    }
}
