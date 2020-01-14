/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mbdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Reese
 */
public class MBDB {
     
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        SuperAdminConnection DB = new SuperAdminConnection("ip_to_DB", 3306);
        try{
        //DB.addUser("student1", "studentemail", 1);
        System.out.println(DB.getUserIDFromEmail("studentemail"));
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        
    }
    
}
