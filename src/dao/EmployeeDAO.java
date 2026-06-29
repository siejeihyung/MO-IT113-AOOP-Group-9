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

    /**
     * Fetches all employees as RegularEmployee objects.
     * Ensure your MySQL table columns match these names exactly.
     */
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        
        // Fetching all 11 fields required by your constructor
        String query = "SELECT EmployeeID, LastName, FirstName, SSS, Philhealth, TIN, " +
                       "PagIbig, BasicSalary, SemiMonthlyRate, HourlyRate, TotalBenefits " +
                       "FROM Employee";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Employee emp = new RegularEmployee(
                    rs.getString("EmployeeID"),
                    rs.getString("LastName"),
                    rs.getString("FirstName"),
                    rs.getString("SSS"),
                    rs.getString("Philhealth"),
                    rs.getString("TIN"),
                    rs.getString("PagIbig"),
                    rs.getDouble("BasicSalary"),
                    rs.getDouble("SemiMonthlyRate"),
                    rs.getDouble("HourlyRate"),
                    rs.getDouble("TotalBenefits")
                );
                employees.add(emp);
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDAO: Error fetching employee objects - " + e.getMessage());
            e.printStackTrace();
        }
        return employees;
    }
    // --- JDBC Methods to replace old CSV functionality ---

    public Employee findById(String id) {
        String query = "SELECT * FROM Employee WHERE EmployeeID = ?";
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
                    rs.getString("Philhealth"),
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
            emp.getSSS(),          // Changed from getSss()
            emp.getPhilHealth(),   // Changed from getPhilhealth()
            emp.getTIN(),          // Changed from getTin()
            emp.getPagIbig(),      // Changed from getPagibig()
            String.valueOf(emp.getBasicSalary()), 
            String.valueOf(emp.getSemiMonthlyRate()), 
            String.valueOf(emp.getHourlyRate()), 
            String.valueOf(emp.getTotalBenefits())
        };
    }
    return null;
}

    public List<String[]> findAllRaw() {
        List<String[]> list = new ArrayList<>();
        // Fetch all employees to satisfy the legacy GUI table format
        for (Employee emp : findAll()) {
            list.add(new String[]{emp.getEmployeeID(), emp.getLastName(), emp.getFirstName()});
        }
        return list;
    }
    
    // Placeholder methods to stop the build errors. 
    // You can implement these later once you need to add/update employees.
    public boolean append(String[] row) { System.out.println("Append not implemented"); return false; }
    public boolean updateField(String id, String col, String val) { System.out.println("Update not implemented"); return false; }
    public boolean deleteById(String id) { System.out.println("Delete not implemented"); return false; }
}