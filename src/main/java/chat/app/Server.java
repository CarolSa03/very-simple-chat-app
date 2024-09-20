package chat.app;

import chat.app.database.DatabaseManager;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class Server extends Thread {

    private static final Logger logger = Logger.getLogger(Server.class.getName());
    public static ConcurrentHashMap<String, UserHandler> activeUsers = new ConcurrentHashMap<>();
    static Socket socket;
    static BufferedReader in;
    static BufferedWriter out;
    static int port = 1234;

    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        db.connect();
        
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server started on port " + port);

            while(true){
                socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress());

                UserHandler userHandler = new UserHandler(socket);
                Thread userThread = new Thread(userHandler);
                userThread.start();
            }
        } catch (ConnectException e) {
            logger.log(Level.SEVERE, "Connection refused: Is the server running?", e);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket error occurred: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error occurred while communicating with the server.", e);
        }
    }

    public static void privateMessage(String recipient, String message, UserHandler sender) {
        UserHandler user = activeUsers.get(recipient);
        if(user != null){
            user.sendMessage("Private message from " + sender.getUsername() + ": " + message);
        }else {
                sender.sendMessage("User " + recipient + " not found.");
        }

    }

    public static void broadcastMessage(String message, UserHandler excludeUser){
        for (UserHandler userHandler : activeUsers.values()) {
            if (userHandler != excludeUser) {
                userHandler.sendMessage(message);
            }
        }
    }

    public static void addUser(String username, UserHandler userHandler) {
        activeUsers.put(username,userHandler);
    }

    public static void removeUser(String username) {
        activeUsers.remove(username);
    }
}