package mbdbclient;

import java.util.ArrayList;

/**
 *A simple Data Structure class composed of only public fields that contains all data
 * related to a User from the Database
 * When a user logs in a UserData object will be made and can be thought of as 
 * the root data Structure that the front end should use to get data collected 
 * from the DB
 * @author Reese
 */
public class UserData {
    public int userID; //The ID of the user 
    public int userType;//The type of user it is
    public String userName; // The username of the user
    public String email; //The email of the user
    public ArrayList<ClassData> classes; //A list of class data the student is in or teacher is heading
    public ArrayList<ActivityData> activities; //A list of activities the student has for the selected class
    
}
