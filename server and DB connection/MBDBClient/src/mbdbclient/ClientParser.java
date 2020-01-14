package mbdbclient;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A Class of static methods that interpret the string returns by methods of 
 * the MBDBServerClient class and returns the data Structure that is related to the 
 * data requested
 * parseT0 parses requestType0, parseT1 parses requestType1, ect
 * example of typical code would be:
 *      UserData data = ClientParser.parseT0(MBDBServerClient.requestType0("some_email")); 
 * @author Reese
 */
public class ClientParser {
    
    /**
     * Creates UserData object from String from requestType0
     * @param in
     * @return 
     */
    public static UserData parseT0(String in){
        UserData out = new UserData();
        Scanner s = new Scanner(in);
        String type = s.next();
        if(type.equals("new")){
            out.userID = -1;
            return out;
        }
        out.userType = Integer.parseInt(type);
        out.userName = s.next();
        ArrayList<ClassData> classList = new ArrayList(); //the class list for the userData structure
        if(out.userType == 1){ //returning data for a student
            while(s.hasNext()){
                ClassData temp = new ClassData();
                temp.classID = s.nextInt();
                temp.className = s.next();
                temp.teacherName = s.next();
                classList.add(temp);
            }
        }
        else{ //a teacher user
            while(s.hasNext()){
                ClassData temp = new ClassData();
                temp.classID = s.nextInt();
                temp.className = s.next();
                int numStudents = s.nextInt();
                ArrayList<UserData> studentList = new ArrayList();
                for(int i = 0; i < numStudents; i++){
                    UserData tempStu = new UserData();
                    tempStu.userID = s.nextInt();
                    tempStu.userName = s.next();
                    studentList.add(tempStu);
                }
                temp.students = studentList;
                classList.add(temp);
            }
        }
        out.classes = classList;
        return out;
    }

    /**
     * returns int from String from requestType1
     * @param in
     * @return 
     */
    public static int parseT1(String in){
        Scanner s = new Scanner(in);
        return s.nextInt();
    }

    /**
     * returns int from String from requestType2
     * @param in
     * @return 
     */
    public static int parseT2(String in){
        Scanner s = new Scanner(in);
        int out = -1;
        if(s.hasNext()){
            out = s.nextInt();
        }
        return out;
    }

    /**
     * Creates an arrayList of ActivityData objects from String from requestType3
     * @param in
     * @return 
     */
    public static ArrayList<ActivityData> parseT3(String in){
        Scanner s = new Scanner(in);
        ArrayList<ActivityData> out = new ArrayList();
        while(s.hasNext()){
            ActivityData temp = new ActivityData();
            temp.ActID = s.nextInt();
            temp.ActType = s.nextInt();
            temp.score = s.nextInt();
            temp.seed = s.nextInt();
            temp.numProbs = s.nextInt();
            out.add(temp);
        }
        return out;
    }

    /**
     * Creates UserData object from String from requestType4
     * @param in
     * @return 
     */
    public static UserData parseT4(String in){
        Scanner s = new Scanner(in);
        UserData out = new UserData();
        String firstLine = s.next();
        if(!firstLine.equals("d")){
            //Means the request was a request to add, if the above was true
            // then a null userData will be returned just
            out.userID = Integer.parseInt(firstLine);
            out.userName = s.next();
        }
        return out;
    }
}
