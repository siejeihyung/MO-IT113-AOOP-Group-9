/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.io.*;

/**
 * CredentialsDAO - Data Access Object for credentials.csv
 * @author anton
 */
public class CredentialsDAO {
    
    private final String CREDENTIALS_FILE = "credentials.csv";
            
    public String findRole(String username, String password) {
       File file = new File(CREDENTIALS_FILE);
       if (!file.exists()) {
          System.err.println("CredentialsDAO: credentials.csv not found");
          return null;
       }
       
       try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
           
        String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.trim().split(",");
 
                // 3-column format: username,password,role
                if (parts.length >= 3
                        && parts[0].trim().equals(username)
                        && parts[1].trim().equals(password)) {
                    return parts[2].trim().toUpperCase();
                }
 
                // Backward compatible: 2-column format assumes ADMIN
                if (parts.length == 2
                        && parts[0].trim().equals(username)
                        && parts[1].trim().equals(password)) {
                    return "ADMIN";
                }
            }
        } catch (IOException e) {
            System.err.println("CredentialsDAO: Error reading credentials: " + e.getMessage());
        }
        return null;
    }
}
      
