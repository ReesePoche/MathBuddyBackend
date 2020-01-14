
package mbdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;


/**
 * This class creates a connection with the SQL DB that has all privileges
 * note when this object is made the connection isn't live 
 * go to the helper method initConnection() to change the Database login credentials
 * for any questions regarding DB structure please see MathBuddyDBSetup.sql 
 * @author Reese
 */
public class SuperAdminConnection {
    
    
    //the connection variable used in all methods below
    private Connection con;
    
    //the statement variable used in all prepared statements
    private Statement stmt;
    
    private PreparedStatement prestmt;
    
    //the latestresultset of the connection
    private ResultSet rs;
    
    //the string that is the host Name
    private String severHostName;
    
    //the port number the connection is in
    private int portNum;
    
    /**
     * makes a superadminConnection object
     * @param hostName the name of the server you are connecting to
     * @param portNum the port number the connection is on
     */
    public SuperAdminConnection(String hostName, int portNum){
        this.con = null;
        this.stmt = null;
        this.prestmt = null;
        this.rs=null;
        this.severHostName = hostName;
        this.portNum = portNum;
    }
    
    /**
     * adds a user with the following credentials to the user table
     * @param userName
     * @param email
     * @param userType 
     */
    public void addUser(String userName, String email, int userType) throws SQLException{
        this.initConnection();
        String command = "INSERT INTO mathbuddydb.users (userName, userEmail, UserType) "
                            + "VALUES (?,?,?);";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setString(1, userName);
        this.prestmt.setString(2, email);
        this.prestmt.setInt(3, userType);
        this.prestmt.executeUpdate();
        this.closeConnection();
    }
    
    /**
     * deletes all records relating to the user in the DB
     * either UserID or email can be null if not known
     * make userID 0 to make null
     * make email null
     * userType must be known
     * @param userID UserID of user to be deleted can be null if only email is known
     * @param email   email of user to be deleted can be null if not known
     * @param userType UserType of user to be deleted cannot be null
     * @throws SQLException 
     */
    public void deleteUser(int userID, String email, int userType) throws SQLException{
        if(userID != 0){
            this.initConnection();
            String command = "DELETE FROM mathbuddydb.users WHERE userID=?;";
            this.prestmt = this.con.prepareStatement(command);
            this.prestmt.setInt(1, userID);
            this.prestmt.executeUpdate();
            this.closeConnection();
            if(userType==1){
                this.initConnection();
                command = "DELETE FROM mathbuddydb.activities WHERE userID=?;";
                this.prestmt = this.con.prepareStatement(command);
                this.prestmt.setInt(1, userID);
                this.prestmt.executeUpdate();
                this.closeConnection();
            }
            else {
                ArrayList<Integer> classes = this.getTeachersClasses(userID);
                while(!classes.isEmpty()){
                    this.deleteClassroom(classes.remove(0));
                }
            }
        }
        else if(email != null){
            int id = this.getUserIDFromEmail(email);
            if(id==0)
                return; //means the email was not found
            this.deleteUser(id, email, userType);
        }
        else
            return; ////means both values null
    }
    
    
    
    /**
     * creates the class with the specified teacher user as head of the class
     * returns the classID of the new class if it returns -1 then class was not made
     * @param teacherID id of teacher user that is creating class
     * @param classroomName the name of the class that is being created
     * @throws SQLException 
     */
    public int createClassroom(int teacherID, String classroomName)throws SQLException{ 
        this.initConnection();
        int out = -1;
        String command = "INSERT INTO mathbuddydb.classrooms (teacherID, classroomName)" 
                            + "VALUES (?, ?);";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, teacherID);
        this.prestmt.setString(2, classroomName);
        this.prestmt.executeUpdate();
        this.closeConnection();
        this.initConnection();
        command = "SELECT MAX(classroomID) FROM mathbuddydb.classrooms " 
                            + "WHERE TeacherID=? AND classroomName=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, teacherID);
        this.prestmt.setString(2, classroomName);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out =rs.getInt(1);
        }
        this.closeConnection();
        return out;
    }
    
    
    /**
     * Deletes classroom with the given classroom ID
     * @param classroomID ID of class to be deleted
     * @throws SQLException 
     */
    public void deleteClassroom(int classroomID) throws SQLException{
        //delete the class activities first
        this.initConnection();
        String command = "DELETE FROM mathbuddydb.activities WHERE classroomID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, classroomID);
        this.prestmt.executeUpdate();
        this.closeConnection();
        //now delete from the classroom table
        this.initConnection();
        command = "DELETE FROM mathbuddydb.classrooms WHERE classroomID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, classroomID);
        this.prestmt.executeUpdate();
        this.closeConnection();
    }
    
    /**
     * adds student user with given email to the class
     * @param classID ID of class you want student to be added to
     * @param email email of student user you want to add to the class
     * @throws SQLException 
     */
    public void addStudentToClass(int classID, String email) throws SQLException{
        int studentID = this.getUserIDFromEmail(email);
        this.initConnection();
        String command = "INSERT INTO mathbuddydb.activities "
                + "(classroomID, userID, activityType) "
                + "VALUES (?,?,?);";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, classID);
        this.prestmt.setInt(2, studentID);
        this.prestmt.setInt(3, 1); //activity of 1 the activity of being in the class
        this.prestmt.executeUpdate();
        this.closeConnection();
        //want to return now the 
    }
    
    /**
     * removes student user with given email from given class
     * @param classroomID class you want the student to be removed from
     * @param studentEmail
     * @throws SQLException 
     */
    public void removeStudentFromClass(int classroomID, String studentEmail) throws SQLException{
        int studentID = this.getUserIDFromEmail(studentEmail);
        ArrayList<Integer> studActs;
        studActs = this.getStudentsActivities(studentID, classroomID);
        while(!studActs.isEmpty()){
            this.deleteSpecificActivity(studActs.remove(0));
        }
    }
    
    
    /**
     * Creates an activity for the given classroom with the given properties for 
     * all student users
     * @param classroomID ID of class that the activity will be made for
     * @param activityTypeID the type of activity that it will be (multiplication, division, ect)
     * @param numProblems the number of problems that will be on the activity
     * @param seed the random number for the problem generator
     * @throws SQLException 
     */
    public void createActivity(int classroomID, int activityTypeID, int numProblems, int seed) throws SQLException{//may need a bigger value for seed
        ArrayList<Integer> students = this.getStudentsInClass(classroomID);
        while(!students.isEmpty()){
            this.initConnection();
            String command = "INSERT INTO mathbuddydb.activities (classroomID, userID, activityType, numProblems, seed)"
                    + "VALUES (?, ?, ?, ?, ?);";
            this.prestmt = this.con.prepareStatement(command);
            this.prestmt.setInt(1, classroomID);
            this.prestmt.setInt(2, students.remove(0));
            this.prestmt.setInt(3, activityTypeID);
            this.prestmt.setInt(4, numProblems);
            this.prestmt.setInt(5, seed);
            this.prestmt.executeUpdate();
            this.closeConnection();
        }
    }
    
    /**
     * Deletes the specified activity
     * @param activityID
     * @throws SQLException 
     */
    public void deleteSpecificActivity(int activityID) throws SQLException{
        this.initConnection();
        String command = "DELETE FROM mathbuddydb.activities WHERE activityID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, activityID);
        this.prestmt.executeUpdate();
        this.closeConnection();
    }
    
    /**
     * Records the activity score for given activity
     * @param activityID
     * @param score
     * @throws SQLException 
     */
    public void recordActivityScore(int activityID, int score) throws SQLException{
        this.initConnection();
        String command = "UPDATE mathbuddydb.activities"
                + " SET score=? WHERE activityID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, score);
        this.prestmt.setInt(2, activityID);
        this.prestmt.executeUpdate();
        this.closeConnection();
    }
    
    /**
     * 
     * @param classID
     * @return returns an arrayList of the userID of all students in the database
     * @throws SQLException 
     */
    public ArrayList<Integer> getStudentsInClass(int classID) throws SQLException{
        ArrayList<Integer> out = new ArrayList();
        this.initConnection();
        String command = "SELECT userID FROM mathbuddydb.activities "
                + "WHERE classroomID=? AND activityType=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, classID);
        this.prestmt.setInt(2, 1);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out.add(rs.getInt(1));
        }
        this.closeConnection();
        return out;
    }
   
    /**
     * Gets the list of the student's classes
     * @param studentID student User ID that you want to get the classes for
     * @return returns an arrayList of the classID's that the student is in
     * @throws SQLException 
     */
    public ArrayList<Integer> getStudentsClasses(int studentID) throws SQLException{
        ArrayList<Integer> out = new ArrayList();
        this.initConnection();
        String command = "SELECT classroomID FROM mathbuddydb.activities " +
                "WHERE userID=? AND activityType=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, studentID);
        this.prestmt.setInt(2, 1);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out.add(rs.getInt(1));
        }
        this.closeConnection();
        return out;
    }
    
    /**
     * 
     * @param classroomIDs an arrayList of the classID's you want to get the teacher for
     * @return An arrayList of Pairs where the key is the className and the value is the teacher's Name
     * @throws SQLException 
     */
    public ArrayList<Pair<String, String>> getClassAndTeacherNames(ArrayList<Integer> classroomIDs) throws SQLException{
        ArrayList<Pair<String, String>> out = new ArrayList();
        ArrayList<String> classNameholder = new ArrayList();
        ArrayList<Integer> teacherIDholder = new ArrayList();
        for(int i = 0; i < classroomIDs.size(); i++){
            this.initConnection();
            String command = "SELECT classroomName, TeacherID FROM mathbuddydb.classrooms " +
                "WHERE classroomID=?;";
            this.prestmt = this.con.prepareStatement(command);
            this.prestmt.setInt(1, classroomIDs.get(i));
            rs=prestmt.executeQuery();
            while(rs.next()){
                classNameholder.add(rs.getString(1));
                teacherIDholder.add(rs.getInt(2));
                
            }
            this.closeConnection();
        }
        while(!classNameholder.isEmpty()){
           Pair<String, String> pair = new Pair(classNameholder.remove(0), this.getUserNameFromID(teacherIDholder.remove(0))); 
           out.add(pair);
        }
        return out;
    }
    
    
    /**
     * Gets all of the activities that the student user is in along with all
     * data relating to the activity
     * @param studentID the student user you want to get the activity data for
     * @param classroomID the classroom ID of the activities you want to get the activities for
     * @return an arrayList of length (num of activities)*5 in the following order
     *          activityID, activity Type, score, seed, and number of problems
     *          a -1 score means the score was null meaning the score not taken
     * @throws SQLException 
     */
    public ArrayList<Integer> getStudentsActivities(int studentID, int classroomID) throws SQLException{
        ArrayList<Integer> out = new ArrayList();
        this.initConnection();
        String command = "SELECT activityID, activityType, score, seed, numProblems FROM mathbuddydb.activities " +
                "WHERE classroomID=? AND userID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, classroomID);
        this.prestmt.setInt(2, studentID);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out.add(rs.getInt(1));
            out.add(rs.getInt(2));
            int holder = rs.getInt(3);
            if(rs.wasNull()){
                out.add(-1);
            }
            else{
                out.add(holder);
            }
            out.add(rs.getInt(4));
            out.add(rs.getInt(5));   
        }
        this.closeConnection();
        return out;
    }
    
    
    
    /**
     * Gets the given teacher user's classes
     * @param teacherID id of teacher user the list of classID's are wanted for
     * @return an arrayList that contains all of the classID of the classes the teacher user created
     * @throws SQLException 
     */
    public ArrayList<Integer> getTeachersClasses(int teacherID) throws SQLException{
        ArrayList<Integer> out = new ArrayList();
        this.initConnection();
        String command = "SELECT classroomID FROM mathbuddydb.classrooms " +
                "WHERE teacherID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, teacherID);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out.add(rs.getInt(1));
        }
        this.closeConnection();
        return out;
    }

/**
 * returns an int that is the userID of the user with the given email
 * emails are considered unique to all users
 * @param email the email of the user 
 * @return an int that is the userID of the user with the given email
 * @throws SQLException 
 */    
public int getUserIDFromEmail(String email) throws SQLException{
        int out = 0;
        this.initConnection();
        String command = "SELECT userID FROM mathbuddydb.users " +
                "WHERE userEmail=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setString(1, email);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out=rs.getInt(1);
        }
        this.closeConnection();
        return out;
    }


/**
 * gives an int that represents the type of user who's ID you given
 * @param userID userID of the type of user you want
 * @return will return an int that represents the user type.
 *         0 means user with given ID does not exist
 *         1 means user is a student
 *         2 means user is a teacher
 *         3 means user is a SGL
 *         *are based off of the DB setup from MathBuddyDBSetup.sql
 * @throws SQLException 
 */
public int getUserType(int userID) throws SQLException{
    int out = 0;
    this.initConnection();
    String command = "SELECT userType FROM mathbuddydb.users " +
            "WHERE userID=?;";
    this.prestmt = this.con.prepareStatement(command);
    this.prestmt.setInt(1, userID);
    rs=prestmt.executeQuery();
    while(rs.next()){
        out=rs.getInt(1);
    }
    this.closeConnection();
    return out;
}

/**
 * returns the username of the user with the given ID
 * @param userID
 * @return A string that is the userName of the user with the given userID
 *         will be an empty string "" if user does not exist
 * @throws SQLException 
 */
public String getUserNameFromID(int userID) throws SQLException{
        String out = "";
        this.initConnection();
        String command = "SELECT userName FROM mathbuddydb.users " +
                "WHERE userID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, userID);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out=rs.getString(1);
        }
        this.closeConnection();
        return out;
    }

/**
 * @param classroomID classroom ID of class you want the name of
 * @return A string that is the classroom name of the classroom with the given ID
 *         If classroom does not exist will return an empty string ""
 * @throws SQLException 
 */
public String getClassroomNameFromClassroomID(int classroomID) throws SQLException{
    String out = "";
        this.initConnection();
        String command = "SELECT classroomName FROM mathbuddydb.classrooms " +
                "WHERE classroomID=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, classroomID);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out=rs.getString(1);
        }
        this.closeConnection();
        return out;
}

/**
 * will return the seed of the activity with the given activityID
 * @param activityID
 * @return an int that is the seed of the activity with the ID given
 *         will be 0 if activity with given ID does not exist
 * @throws SQLException 
 */
public int getActivitySeed(int activityID) throws SQLException{
    int out = 0;
    this.initConnection();
    String command = "SELECT seed FROM mathbuddydb.activities " +
                "WHERE activityID=?;";
    this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setInt(1, activityID);
        rs=prestmt.executeQuery();
    while(rs.next()){
            out=rs.getInt(1);
        }
    this.closeConnection();
    return out;
}

    ////////////////////////////////////////////////////////////////////////////
    ///                    HELPER METHODS                                    ///
    /// METHODS THAT ARE PRIVATE AND ONLY OTHER METHODS USE                  ///
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * 
     * @param classroomName name of classroom you want the classroom ID to
     * @return an int that is the classroom ID of the named classroom
     * @throws SQLException 
     */
    private int getClassroomIDFromName(String classroomName) throws SQLException{
        int out = 0;
        this.initConnection();
        String command = "SELECT classroomID FROM mathbuddydb.classrooms " +
                "WHERE classroomName=?;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.setString(1, classroomName);
        rs=prestmt.executeQuery();
        while(rs.next()){
            out=rs.getInt(1);
        }
        this.closeConnection();
        return out;
    }
    
    
    
    
    
    
    /**
     * Just makes the connection URL from the objects variables
     * If name of  Database changes This method will have to be changed
     * Make need better way of keeping track of DB name than hard coding it
     * @return 
     */
    private String makeURL(){
        return "jdbc:mysql://"+severHostName+":"+portNum+"/mathbuddydb?useSSL=false&allowPublicKeyRetrieval=true";
    }
    
    /**
     * initializes the con variable must be done before interaction with the DB 
     * ALWAYS CLOSE THE CONNECTION AFTER OPENING IT
     * if your admin username and pass is different please change
     */
    private void initConnection(){
        try{
                con = DriverManager.getConnection(this.makeURL(), "MathBuddyAdmin", "password");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Just makes the connection Null do after all initConnections
     * goes through all the global variables make sure results are taken care of
     */
    private void closeConnection(){
        if (rs != null) try { rs.close(); } catch(Exception e) {}
        if (prestmt != null) try { prestmt.close(); } catch(Exception e) {}
        if (stmt != null) try { stmt.close(); } catch(Exception e) {}
        if (con != null) try { con.close(); } catch(Exception e) {}
    }
    
////////////////////////////////////////////////////////////////////////////////
///                 TEST METHODS                                             ///
/// FOLLOWING METHODS ARE FOR PURELY TESTING PURPOSES                        ///
/// you are just supposed to call the demo methods that carry out the        ///
///                 private methods                                          ///
////////////////////////////////////////////////////////////////////////////////

    /**
     * gets rid of all data in all of the tables except for UserTypes, activityTypes
     * @throws SQLException 
     */    
    private void clearTables() throws SQLException{
        this.initConnection();
        String command = "DELETE FROM mathbuddydb.activities;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.executeUpdate();
        this.closeConnection();
        System.out.println("cleared activites");
        this.initConnection();
        command = "DELETE FROM mathbuddydb.classrooms;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.executeUpdate();
        this.closeConnection();
        System.out.println("cleared classrooms");
        this.initConnection();
        command = "DELETE FROM mathbuddydb.users;";
        this.prestmt = this.con.prepareStatement(command);
        this.prestmt.executeUpdate();
        this.closeConnection();
        System.out.println("cleared users");



    }

    /**
     * prints out all of the tables from the data into the console
     * for easy method testing
     * @throws SQLException 
     */
    private void printTables() throws SQLException{
        this.initConnection();
        String command = "SELECT * FROM mathbuddydb.users;";
        this.prestmt = this.con.prepareStatement(command);
        rs=prestmt.executeQuery();
        System.out.println("PRINTING THE USERTABLE");
        while(rs.next()){
            System.out.printf("%d,\t%s,\t%s\t,%d\n", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4));
        }
        this.closeConnection();
        System.out.println("PRINTING THE CLASSROOM TABLE");
        this.initConnection();
        command = "SELECT * FROM mathbuddydb.classrooms;";
        this.prestmt = this.con.prepareStatement(command);
        rs=prestmt.executeQuery();
        while(rs.next()){
            System.out.printf("%d,\t%s,\t%d\n", rs.getInt(1), rs.getString(2), rs.getInt(3));
        }
        this.closeConnection();
        System.out.println("PRINTING THE ACTIVITIES TABLE;");
        this.initConnection();
        command = "SELECT * FROM mathbuddydb.activities;";
        this.prestmt = this.con.prepareStatement(command);
        rs=prestmt.executeQuery();
        while(rs.next()){
            System.out.printf("%d,\t%d,\t%d\t,%d\t%d,\t%d,\t%d,\n", rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7));
        }
        this.closeConnection();
    }

    /**
     * just opens a connection and closes it
     */
    private void openEmptyConnection(){
        System.out.println("About to open a blank connection");
        this.initConnection();
        System.out.println("OPENED THE CONNECTION");
        this.closeConnection();
        System.out.println("CLOSED THE CONNECTION");
    }

    /**
     * populates table with test data
     * please get rid of old data with clearTables() before running this again
     * @throws SQLException 
     */
    private void fillTheTableSome() throws SQLException{
        System.out.println("about to make 3 teachers");
        this.addUser("Teacher1", "email1@teacher.com", 2);
        System.out.println("Made 1 teacher");
        this.addUser("Teacher2", "email2@teacher.com", 2);
        System.out.println("Made 2 teacher");
        this.addUser("Teacher3", "email3@teacher.com", 2);
        System.out.println("Made 3 teacher");
        System.out.println("ABOUT to make 3 students");
        this.addUser("student1", "email1@student.com", 1);
        this.addUser("student2", "email2@student.com", 1);
        this.addUser("student3", "email3@student.com", 1);
        System.out.println("students made");
    }

    /**
     * adds classroom to the classroom table
     * @throws SQLException 
     */
    private void createClassrooms() throws SQLException{
        int teacherNum = this.getUserIDFromEmail("email1@teacher.com");
        System.out.println("Teacher1 about to make a class");
        this.createClassroom(teacherNum, "C1T1");
        System.out.println("Teacher1 finished making classes");
        teacherNum = this.getUserIDFromEmail("email2@teacher.com");
        System.out.println("teacher2 about to make 2 clases");
        this.createClassroom(teacherNum, "C1T2");
        this.createClassroom(teacherNum, "C2T2");
        System.out.println("teacher2 finished making classes");
        teacherNum = this.getUserIDFromEmail("email3@teacher.com");
        System.out.println("Teacher3 about to make 3 classes");
        this.createClassroom(teacherNum, "C1T3");
        this.createClassroom(teacherNum, "C2T3");
        this.createClassroom(teacherNum, "C3T3");
        System.out.println("teacher3 finished making classes");
    }
}
    