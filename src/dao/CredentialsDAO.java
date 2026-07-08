/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CredentialsDAO {
    
    public String authenticateEmployee(String employeeId, String lastName) {
    String query = "SELECT PositionID FROM employee WHERE EmployeeID = ? AND LastName = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setString(1, employeeId);
        pstmt.setString(2, lastName);
        
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return "Employee"; // Successful login
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null; // Login failed
}
    
    public String findRole(String username, String password) {
        // This query joins UserAccount and Role to get the RoleName based on credentials
        String query = "SELECT r.RoleName " +
                       "FROM UserAccount u " +
                       "JOIN UserRole ur ON u.UserID = ur.UserID " +
                       "JOIN Role r ON ur.RoleID = r.RoleID " +
                       "WHERE u.Username = ? AND u.PasswordHash = ? AND u.AccountStatus = 'Active'";
       
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
           
            // Insert the username and password into the query safely
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // If a match is found, return the role (e.g., "ADMIN", "HR")
                return rs.getString("RoleName").trim().toUpperCase();
            }
            
        } catch (SQLException e) {
            System.err.println("CredentialsDAO: Database connection error - " + e.getMessage());
            e.printStackTrace();
        }
        
        // Return null if credentials are wrong or DB fails
        return null;
    }
}