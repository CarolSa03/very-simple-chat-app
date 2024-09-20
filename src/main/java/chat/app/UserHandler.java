package chat.app;

import chat.app.database.DatabaseManager;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserHandler extends Thread {
    private static final Logger logger = Logger.getLogger(UserHandler.class.getName());
    public static DatabaseManager dbManager = new DatabaseManager();
    Socket userSocket;
    BufferedReader in = null;
    BufferedWriter out = null;
    String username;
    String password;

    public UserHandler(Socket socket) {
        this.userSocket = socket;
        dbManager.connect();
    }

    public void run() {

        try {
            in = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(userSocket.getOutputStream()));

            if (!authenticateUser()) {
                close();
                return;
            }

            Server.broadcastMessage(username + " has joined the chat!", this);

            out.write("Welcome " + username + ".\n " +
                    "To write a private message please follow the following format: " +
                    "/pm <username> <message>. \n To send a message to all active users, simple type your message. \n" +
                    "To leave the session, type '/logout'.");
            out.newLine();
            out.flush();
            String userMessage;
            while ((userMessage = in.readLine()) != null) {
                if (userMessage.startsWith("/pm ")) {
                    // private message format: /pm <username> <message>
                    handlePrivateMessage(userMessage);
                } else if (userMessage.equalsIgnoreCase("/logout")) {
                    break;
                } else {
                    Server.broadcastMessage(username + ":" + userMessage, this);
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
        }
        finally {
            close();
        }
    }

    private boolean authenticateUser() throws IOException {
        while (true){
            out.write("Enter your username: ");
            out.newLine();
            out.flush();
            username = in.readLine();

            out.write("Enter your password: ");
            out.newLine();
            out.flush();
            password = in.readLine();

            if (dbManager.validateUser(username, password)) {
                if(Server.activeUsers.containsKey(username)){
                    out.write("Username already in use. Please try again.");
                    out.newLine();
                    out.flush();
                    continue;
                }
                Server.addUser(username, this);
                out.write("Login successful! Welcome, " + username + ".");
                out.newLine();
                out.flush();
                System.out.println(username + " has logged in.");
                return true;
            }else{
                out.write("Invalid credentials. Please try again.");
                out.newLine();
                out.flush();
            }
        }
    }

    private void handlePrivateMessage(String message) throws IOException {
        String[] parts = message.split(" ", 3);
        if (parts.length >= 3) {
            String recipient = parts[1];
            String privateMessage = parts[2];
            Server.privateMessage(recipient, privateMessage, this);
        } else {
            out.write("Invalid private message format. Use: /pm <username> <message>");
            out.newLine();
            out.flush();
        }
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unknown Host: Please verify the server address.", e);
        } catch (ConnectException e) {
            logger.log(Level.SEVERE, "Connection refused: Is the server running?", e);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket error occurred: " + e.getMessage(), e);
        }catch(IOException e){
            System.out.println("Error sending message to " + username + ": " + e.getMessage());
        }
    }
    private void close() {
        try {
            if (username != null) {
                Server.removeUser(username);
                Server.broadcastMessage(username + " has left the chat.", this);
                System.out.println(username + " has disconnected.");
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (userSocket != null) userSocket.close();
            dbManager.close();
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Socket error occurred: " + e.getMessage(), e);
        }catch(IOException e){
            System.out.println("Error closing connection for " + username + ": " + e.getMessage());
        }
    }
}
