package chat.app.database;

public class CleanDataBase {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.connect();
        dbManager.clearUsersTable();
        dbManager.close();
    }
}
