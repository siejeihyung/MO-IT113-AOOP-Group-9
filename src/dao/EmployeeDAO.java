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
    
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        
        String query = "SELECT EmployeeID, LastName, FirstName, SSS, PhilHealth, TIN, " +
                       "PagIbig, BasicSalary, SemiMonthlyRate, HourlyRate, TotalBenefits " +
                       "FROM employee";

        System.out.println("Executing query: " + query); 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                count++;
                Employee emp = new RegularEmployee(
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
                employees.add(emp);
            }
            System.out.println("EmployeeDAO: Found " + count + " employees in database.");
            
        } catch (SQLException e) {
            System.err.println("EmployeeDAO: Error fetching employee objects - " + e.getMessage());
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

    public List<String[]> findAllRaw() {
    List<String[]> list = new ArrayList<>();
    for (Employee emp : findAll()) {
        list.add(new String[]{
            emp.getEmployeeID(), 
            emp.getLastName(), 
            emp.getFirstName(), 
            emp.getSSS(),        
            emp.getPhilHealth(), 
            emp.getTIN(),        
            emp.getPagIbig()     
        });
    }
    return list;
}
    
    public boolean append(String[] row) { System.out.println("Append not implemented"); return false; }
    public boolean updateField(String id, String col, String val) { System.out.println("Update not implemented"); return false; }
    public boolean deleteById(String id) { System.out.println("Delete not implemented"); return false; }
}
