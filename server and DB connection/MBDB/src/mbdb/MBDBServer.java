
package mbdb;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javafx.util.Pair;

/**
 * Following is code that would run on the Server 
 * @author Reese
 */
public class MBDBServer {
    
    private static String IP_ADDRESS_TO_USE = "localhost";
    
    private static int PORT_TO_USE = 3306;
    
    public static void main(String argv[]) throws Exception {
        String capitalizedSentence;
        ServerSocket welcomeSocket = new ServerSocket(6789);
        SuperAdminConnection DB = new SuperAdminConnection(IP_ADDRESS_TO_USE, PORT_TO_USE);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient =
              new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String requestType = inFromClient.readLine();
            if(requestType.equals("0")){
            //means this is a start up request rest of request will be the email
            // will be of the form: 0\nemail\n
                String email = inFromClient.readLine();
                
                int userID = DB.getUserIDFromEmail(email);
                if(userID!=0){
                    //this is an existing user
                    int userType = DB.getUserType(userID);
                    if(userType==1){
                        //the user is a student
                        //Lets get his name
                        String userName = DB.getUserNameFromID(userID);
                        //Lets get his classID's
                        ArrayList<Integer> classIDs = DB.getStudentsClasses(userID);
                        ArrayList<Pair<String, String>> CNTN = DB.getClassAndTeacherNames(classIDs);
                        //now send it back
                        String userInfo = userType + "\n" + userID + "\n";
                        userInfo+= userName + "\n";
                        while(!classIDs.isEmpty()){
                            userInfo += classIDs.remove(0) + "\n";
                            userInfo += CNTN.get(0).getKey() + "\n";
                            userInfo += CNTN.remove(0).getValue() + "\n";
                        }
                        userInfo+= "EOF\n";
                        PrintStream outToClient = new PrintStream(connectionSocket.getOutputStream());
                        outToClient.print(userInfo);
                        //client is expected to close the connection after reading the data
                    }
                    else if(userType==2){
                        //the user is a teacher
                        String userName = DB.getUserNameFromID(userID);
                        ArrayList<Integer> classIDs = DB.getTeachersClasses(userID);
                        ArrayList<String> classNames = new ArrayList();
                        ArrayList<ArrayList<Integer>> studentIDs = new ArrayList();
                        ArrayList<ArrayList<String>> studentNames = new ArrayList();
                        for(int i = 0; i < classIDs.size(); i++){
                            classNames.add(DB.getClassroomNameFromClassroomID(classIDs.get(i)));
                            studentIDs.add(DB.getStudentsInClass(classIDs.get(i)));
                            ArrayList<String> tempNames = new ArrayList();
                            for(int j = 0; j < studentIDs.get(i).size(); j++){
                                tempNames.add(DB.getUserNameFromID(studentIDs.get(i).get(j)));
                            }
                            studentNames.add(tempNames);
                        }
                        //prep output string
                        String OutToClient = userType + "\n" + userID + "\n";
                        OutToClient += userName + "\n";
                        while(!classIDs.isEmpty()){
                            OutToClient+= classIDs.remove(0) + "\n";
                            OutToClient+= classNames.remove(0)+ "\n";
                            OutToClient+= studentIDs.get(0).size() + "\n";
                            while(!studentIDs.get(0).isEmpty()){
                                OutToClient+= studentIDs.get(0).remove(0) + "\n";
                                OutToClient+= studentNames.get(0).remove(0) + "\n";
                            }
                            if(!studentIDs.isEmpty()){
                                studentIDs.remove(0);
                                studentNames.remove(0);
                            }
                        }
                        OutToClient += "EOF\n";
                        PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                        out.print(OutToClient);
                        //client is expected to close the connection after reading the data
                    }
                    else if(userType==3){
                        //the user is an SGL
                    }

                }
            else{
                //this is a new user will need to get information from them
                String outToClientString = "new\nEOF\n";
                PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                out.print(outToClientString);
            }
            
            
            }
            else if(requestType.equals("1")){
            //this means that the client requested to make a new user
            //expected in the format 1\nuserType\nuserName\nuserEmail\n
            //will return the userID
                int userType = Integer.parseInt(inFromClient.readLine());
                String userName = inFromClient.readLine();
                String userEmail = inFromClient.readLine();
                
                DB.addUser(userName, userEmail, userType);
                    String outToClientString = DB.getUserIDFromEmail(userEmail) + "\nEOF\n";
                    PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                    out.print(outToClientString);
            }
            else if(requestType.equals("2")){
            //means this is a teacher requesting the making of a new class
            //will be of form 2\nnewClassID\nEOF\n
            //sends back just an EOF so update page after this
                String className = inFromClient.readLine();
                int teacherID = Integer.parseInt(inFromClient.readLine());
               
                int newClassID = DB.createClassroom(teacherID, className);
                String outToClientString = newClassID + "\nEOF\n";
                PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                out.print(outToClientString);
            //client expected to kill connection after reading
            }
            else if(requestType.equals("3")){
            //This means that A class was selected by a student or
            // that a student was selected by a teacher
            // will send back the activity list for a student in a class actID\nactType\nscore\nseed\nnumprobs\nEOF\n
            //request in form form 3\nuserType\nuserID\nclassID\n
                int userType = Integer.parseInt(inFromClient.readLine());
                int userID = Integer.parseInt(inFromClient.readLine());
                int classID = Integer.parseInt(inFromClient.readLine());
               
                ArrayList<Integer> acts = DB.getStudentsActivities(userID, classID);
                String outToClientString = "";
                while(!acts.isEmpty()){
                    outToClientString+= acts.remove(0) + "\n";
                    outToClientString+= acts.remove(0) + "\n";
                    outToClientString+= acts.remove(0) + "\n";
                    outToClientString+= acts.remove(0) + "\n";
                    outToClientString+= acts.remove(0) + "\n";
                }
                outToClientString+= "EOF\n";
                PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                out.print(outToClientString);
            }
            else if(requestType.equals("4")){
            //This is a request from a teacher to add or remove a student from a class
            //in for format 4\n(1OR0)\nclassID\nstudentEmail\n //1 means add 0 means remove
            //If it was adding will send back  newStudentID\nNewStudentName\nEOF\n
            // if it was removing a student, will just send back d\nEOF\n
            //will send back updated roster in form ID\nName\nId\nName\n...EOF\n
                String addOrRemove = inFromClient.readLine();
                int classID = Integer.parseInt(inFromClient.readLine());
                String studentEmail = inFromClient.readLine();
                
                String outToClientString = "";
                if(addOrRemove.equals("0")){
                    //user will me removed
                     DB.removeStudentFromClass(classID, studentEmail);
                     outToClientString+="d\nEOF\n";
                }
                else{
                    //user will be added to class
                    DB.addStudentToClass(classID, studentEmail);
                    //want to make the send back string of studentID\nstudentName\nEOF\n
                    int studentID = DB.getUserIDFromEmail(studentEmail);
                    outToClientString+= studentID + "\n";
                    outToClientString+= DB.getUserNameFromID(studentID) + "\n" + "EOF\n";
                }
                PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                out.print(outToClientString);
            }
            else if(requestType.equals("5")){
            //This is the making an activity by a teacher
            //will be of form: 5\n1\nnclassID\nactivityType\nseed\nnumProblems\n 
            //returns new class activity list??
                String addorDelete = inFromClient.readLine();
                int classID = Integer.parseInt(inFromClient.readLine());
                int activityType = Integer.parseInt(inFromClient.readLine());
                int seed = Integer.parseInt(inFromClient.readLine());
                int numProblsm = Integer.parseInt(inFromClient.readLine());
               
                DB.createActivity(classID, activityType, numProblsm, seed);
                String outToClientString  = "1\nEOF\n";
                PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                out.print(outToClientString);
            }
            else if(requestType.equals("6")){
            //this means a student selected a quiz
            //should only happen when the student wants to take the quiz
            // should not be allowed to happen if score is already given
            //will return a seed so the program can generate the problems
            // makes the scote 0 then the seed must be sent back to change it back
            //of format: 6\nactivityID\n  out format: seed\nEOF\n 
                int activityID = Integer.parseInt(inFromClient.readLine());
                
                int seed = DB.getActivitySeed(activityID);
                String outToClientString = seed + "\nEOF\n";
                PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                out.print(outToClientString);
            }
            else if(requestType.equals("7")){
            //this is a student trying to submit the quiz score
            //seed must be given as verification
            //form: 7\nactivityID\nseed\nscore\n
            //will return recordedScore\nEOF\n
                int activityID = Integer.parseInt(inFromClient.readLine());
                int seed = Integer.parseInt(inFromClient.readLine());
                int score = Integer.parseInt(inFromClient.readLine());
                
                int realSeed = DB.getActivitySeed(activityID);
                if(realSeed == seed){
                    DB.recordActivityScore(activityID, score);
                    String outToClientString = score + "\nEOF\n";
                    PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                    out.print(outToClientString);
                }
                else{
                    String outToClientString = "0\nEOF\n";
                    PrintStream out = new PrintStream(connectionSocket.getOutputStream());
                    out.print(outToClientString);
                }
            }  
        }
         
         
         
         
         
    }
    
}

