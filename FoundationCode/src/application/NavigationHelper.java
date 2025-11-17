// NavigationHelper.java
package application;
import javafx.stage.Stage;
import databasePart1.*;

/**
 * Simple navigation helper to route users to the correct home page based on role.
 */
public class NavigationHelper{
    public static void goToHomePage(String role, Stage primaryStage, DatabaseHelper databaseHelper, User currentUser) {
        System.out.println("Navigating to " + role + " homepage");

        if(role.equals("admin")) {
            new AdminHomePage(databaseHelper, currentUser).show(primaryStage);
        } else if (role.equals("user")) {
            new UserHomePage(databaseHelper, currentUser).show(primaryStage);
        } else if (role.equals("student")) {
            new StudentHomePage(databaseHelper, currentUser).show(primaryStage);  // Added parameters
        } else if (role.equals("instructor")){
            new InstructorHomePage(databaseHelper, currentUser).show(primaryStage);  // Added parameters
        } else if (role.equals("staff")) {
            new StaffHomePage(databaseHelper, currentUser).show(primaryStage);
        } else if (role.equals("reviewer")) {
            new ReviewerHomePage(databaseHelper, currentUser).show(primaryStage);
        }
    }
}