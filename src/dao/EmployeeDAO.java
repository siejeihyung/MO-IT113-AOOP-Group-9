/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Employee;
import model.RegularEmployee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    
    public boolean exists(String username) {
        return false; 
    }

    public boolean save(String username, String password) {
        return false; 
    }

    public boolean updatePassword(String username, String password) {
        return false; 
    }
    
   public List<String[]> getAllEmployees() {
    List<String[]> employees = new ArrayList<>();
    // This query now explicitly selects all 19 columns in the correct order
    String query = "SELECT EmployeeID, LastName, FirstName, Birthday, Address, PhoneNumber, " +
                   "SSS, PhilHealth, TIN, PagIbig, EmploymentStatus, PositionID, " +
                   "ImmediateSupervisorID, BasicSalary, RiceSubsidy, PhoneAllowance, " +
                   "ClothingAllowance, GrossSemiMonthlyRate, HourlyRate FROM employee";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            // Mapping the database result to the 19-column array
            String[] row = new String[19];
            for (int i = 0; i < 19; i++) {
                row[i] = rs.getString(i + 1); 
            }
            employees.add(row);
        }
    } catch (SQLException e) {
        System.err.println("EmployeeDAO: Error fetching employees - " + e.getMessage());
        e.printStackTrace();
    }
    return employees;
}
    public Employee findById(String id) {
        String query = "SELECT * FROM employee WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new RegularEmployee(
                    rs.getString("EmployeeID"),
                    rs.getString("LastName"),
                    rs.getString("FirstName"),
                    rs.getString("SSS"),
                    rs.getString("PhilHealth"),
                    rs.getString("TIN"),
                    rs.getString("PagIbig"),
                    rs.getDouble("BasicSalary"),
                    rs.getDouble("SemiMonthlyRate"),
                    rs.getDouble("HourlyRate"),
                    rs.getDouble("TotalBenefits")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String[] findRawById(String id) {
        Employee emp = findById(id);
        if (emp != null) {
            return new String[]{
                emp.getEmployeeID(), 
                emp.getLastName(), 
                emp.getFirstName(), 
                emp.getSSS(),          
                emp.getPhilHealth(),   
                emp.getTIN(),          
                emp.getPagIbig(),      
                String.valueOf(emp.getBasicSalary()), 
                String.valueOf(emp.getSemiMonthlyRate()), 
                String.valueOf(emp.getHourlyRate()), 
                String.valueOf(emp.getTotalBenefits())
            };
        }
        return null;
    }

    // Inside EmployeeDAO.java
    public List<Employee> findAll() {
        List<Employee> list = new ArrayList<>();
        // This query fetches the data needed to create Employee objects
        String query = "SELECT * FROM employee";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            // Mapping the database record to your Employee model
            list.add(new RegularEmployee(
                rs.getString("EmployeeID"),
                rs.getString("LastName"),
                rs.getString("FirstName"),
                rs.getString("SSS"),
                rs.getString("PhilHealth"),
                rs.getString("TIN"),
                rs.getString("PagIbig"),
                rs.getDouble("BasicSalary"),
                rs.getDouble("GrossSemiMonthlyRate"),
                rs.getDouble("HourlyRate"),
                0.0 // Placeholder for benefits
            ));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
    
    public boolean append(String[] row) { System.out.println("Append not implemented"); return false; }
    public boolean updateField(String id, String col, String val) { System.out.println("Update not implemented"); return false; }
    public boolean deleteById(String id) { System.out.println("Delete not implemented"); return false; }

    public List<String[]> findAllRaw() {
    List<String[]> list = new ArrayList<>();
    String query = "SELECT EmployeeID, LastName, FirstName, Birthday, Address, PhoneNumber, " +
                   "SSS, PhilHealth, TIN, PagIbig, EmploymentStatus, PositionID, " +
                   "ImmediateSupervisorID, BasicSalary, RiceSubsidy, PhoneAllowance, " +
                   "ClothingAllowance, GrossSemiMonthlyRate, HourlyRate FROM employee";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            String[] row = new String[19];
            for (int i = 0; i < 19; i++) {
                row[i] = rs.getString(i + 1); // Retrieves all 19 columns
            }
            list.add(row);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
}
