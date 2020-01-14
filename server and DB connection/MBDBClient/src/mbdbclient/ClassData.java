package mbdbclient;

import java.util.ArrayList;

/**
 * A simple Data Structure class composed of only public fields that contains all data
 * related to a given classroom from the database
 * @author Reese
 */
public class ClassData {
    public int classID; //The ID of the Classroom
    public int teacherID; // The userID of the teacher  of the Classroom
    public String teacherName; //The Name of the teacher of the Classroom
    public String className; //The name of the class
    public ArrayList<UserData> students; //an array List containing info on students
}
