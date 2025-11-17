package application;

import java.sql.SQLException;
import databasePart1.DatabaseHelper;

public class SetupAccountLogic {
	
	// DatabaseHelper to handle database operations.
    public SetupAccountLogic(DatabaseHelper databaseHelper) {
    }
    
    public static String validateInputs(String userName, String password, DatabaseHelper databaseHelper, String code) throws SQLException {
        String userNameCheck = UserNameRecognizer.checkForValidUserName(userName);
        String passwordCheck = PasswordEvaluator.evaluatePassword(password);

        if (!userNameCheck.isEmpty()) return userNameCheck;
        if (!passwordCheck.isEmpty()) return passwordCheck;

        if (databaseHelper.doesUserExist(userName)) return "This username is taken!";
        if (databaseHelper.doesUserExist(userName)) return "Invalid invitation code";

        return ""; // no errors
    }
}
