package application;

import java.sql.SQLException;
import databasePart1.DatabaseHelper;

/**
 * Test class used during initial user/account validation checks.
 */
public class FirstUserCreationTest {

public static void main(String[] args) throws SQLException {
    DatabaseHelper mockDb = new DatabaseHelper(); 
    try {
        // Connect to the database
    	mockDb.connectToDatabase();
    } catch (SQLException e) {
	    	System.out.println(e.getMessage());
	    	}

        // Now validate inputs (will be able to check the database)
        String result = SetupAccountLogic.validateInputs("Pond500", "Password123!", mockDb, "wrongCode");
        if (!result.isEmpty()) {
            System.out.println("Error: " + result);
        } else {
            System.out.println("All inputs valid");
        }
        
}
}

		