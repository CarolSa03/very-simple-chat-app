package chat.app.database;

public class AddUsers {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.connect();

        dbManager.addUser("alice", "password123");
        dbManager.addUser("bob", "mypassword");
        dbManager.addUser("andrew", "minepassword");

        dbManager.close();
    }

}
