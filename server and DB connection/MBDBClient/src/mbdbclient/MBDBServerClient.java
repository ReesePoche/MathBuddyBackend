package mbdbclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A Client designed for the MBDBServer class
 * When the application needs data, or wants to upload data to The DB
 * This is the object to use
 * Please user ClientParser class to take output created by the methods below 
 * into more useful data Structures
 * @author Reese
 */
public class MBDBServerClient {
    

    /**
     * The host of the server that MBDBServer is located and running
     */
    private String host;

    /**
     * The port Number on the host the MBDBServer is listening
     */
    private int portNum;

    private InetAddress IntAddress;

    /**
     * Creates a MBDBServerClient object with the given parameters
     * Should be created when user attempts to login.
     * @param host
     * @param portNum 
     */
    public MBDBServerClient(String host, int portNum){
        this.host = host;
        this.portNum = portNum;
        try {
            this.IntAddress = InetAddress.getByName(host);
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }

    /**
     * This should be initial request type for the application. 
     * it feeds the server the email gotten from the Google Account login
     * 
     *
     * @param email email the user types in
     * @return if the email is not in the DB the user is new the return will be new\n
     *         if the user is a student the return will be 1\nuserID\nuserName\nclass1ID\nclass1Name\nteacherName\nclass2ID\n...\n
     *         if teacher the return will be 2\nuserID\nuserName\nClassID\nclassName\nnumStudents\nstudentID\nstudentName\n... \n
     * @throws IOException
     */
    public String requestType0(String email){
        String out = "";
        try {
            Socket clientSocket = new Socket(this.IntAddress, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("0\n" + email + "\n");

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!inFromServer.ready()) {
            } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while (!temp.equals("EOF")) {
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;

    }

    /**
     * A request from the application to make a user with the given information
     * @param userType type of user being made
     * @param userName name user wants
     * @param userEmail email the user typed in
     * @return will return 0\nEOF\n if a failure otherwise it will be userID\n
     */
    public String requestType1(int userType, String userName, String userEmail){
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("1\n"+userType +"\n" + userName + "\n"+ userEmail +"\n");
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;
    }

    /**
     * This is a request from a teacher user to create a classroom with the given name
     * @param className name of class to be created
     * @param TeacherID ID of teacher that created the class
     * @return returns the new list of class IDS including the one that was just created of format
     * classID1\nclassID2\n....
     * @throws IOException
     */
    public String requestType2(String className, int TeacherID) {
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("2\n"+className +"\n" + TeacherID + "\n");

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;
    }

    /**
     * This is the type of request that happens when a student selects a class or
     * a teacher selects a student in a class
     * @param userType
     * @param studentID
     * @param classID
     * @return a string with act info of form ActID\nactType\nScore\nseed\nNumProbs\n
     * A score value of -1 implies that the quiz has not been taken
     * @throws IOException
     */
    public String requestType3(int userType, int studentID, int classID) {
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("3\n"+userType +"\n" + studentID + "\n" + classID + "\n");
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;
    }



    /**
     * This is a request from a teacher to add or remove a student from a class
     * @param classID
     * @param StudentID
     * @param adding true if adding a student false if removing
     * @return if adding a student the outputString will be studentID\nstudentName\n
     *         if removing the output string will just be done\n can be just thrown away
     * @throws IOException
     */
    public String requestType4(int classID, int StudentID, boolean adding) {
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            if(adding) {
                outToServer.print("4\n1\n"+ classID +"\n" + StudentID + "\n");
            }
            else{
                outToServer.print("4\n0\n"+ classID +"\n" + StudentID + "\n");
            }
            
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;
    }

    /**
     * This is a request by a teacher to make an activity for  a class
     * @param classID ID the activity is being made for
     * @param activityType type of activity wanted
     * @param seed the seed for the problem
     * @param numProbs the number of problems wanted
     * @return will just return the String "1" as a confirmation the server got and processed the request
     * @throws IOException
     */
    public String requestType5(int classID, int activityType, int seed, int numProbs) {
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("5\n1\n"+ classID +"\n" + activityType + "\n" +seed + "\n" + numProbs +"\n" +"\nEOF\n");
            
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;
    }

    /**
     * when a student selects an unfinished quiz to try and attempt
     * will return a seed so the student can generate the problems
     * save the seed it will be needed to turn in the information
     * @param ActivityID
     * @return of the form seed\n
     * @throws IOException
     */
    public String requestType6(int ActivityID) {
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("6\n"+ ActivityID +"\n");
            
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }
        return out;
    }

    /**
     * This is a request for a student to turn in the score that he got for a quiz
     * @param activityID
     * @param seed
     * @param score
     * @return will return the score that was recorded will be 0 if seed was wrong score\n
     * @throws IOException
     */
    public String requestType7(int activityID, int seed, int score) {
        String out = "";
        try{
            Socket clientSocket = new Socket(this.host, this.portNum);
            PrintStream outToServer = new PrintStream(clientSocket.getOutputStream());
            outToServer.print("7\n"+ activityID +"\n" + seed + "\n" + score + "\n");
            
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!inFromServer.ready()) { } //waits for the inputstream to be written to by the server
            String temp = inFromServer.readLine();
            while(!temp.equals("EOF")){
                out += temp + "\n";
                temp = inFromServer.readLine();
            }
            clientSocket.close();
        }
        catch(Exception e){
             System.out.println(e.toString());
        }   
        return out;
    }
    
}
