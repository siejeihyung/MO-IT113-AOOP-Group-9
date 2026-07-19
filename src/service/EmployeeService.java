/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.CredentialsDAO;
import dao.EmployeeDAO;
import model.Employee;
import java.util.*;

/**
 * EmployeeService — Business logic ONLY. No file reading here.
 *
 * Handles:
 * Authentication via CredentialsDAO and EmployeeDAO
 * Business logic using Employee objects (OOP requirement)
 * Backward compatible String[] methods for existing GUI panels
 */
public class EmployeeService {

    private final EmployeeDAO    employeeDAO;
    private final CredentialsDAO credentialsDAO;

    // Roles for RBAC
    public static final String ROLE_ADMIN      = "ADMIN";
    public static final String ROLE_HR         = "HR";
    public static final String ROLE_FINANCE    = "FINANCE";
    public static final String ROLE_EMPLOYEE   = "EMPLOYEE";
    public static final String ROLE_IT_SUPPORT = "IT_SUPPORT";

    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO    = employeeDAO;
        this.credentialsDAO = new CredentialsDAO();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Authentication
    // ════════════════════════════════════════════════════════════════════════
    public String authenticate(String username, String password) {

        // 1. Check credentials.csv (for manual Admin accounts)
        String roleFromCredentials = credentialsDAO.findRole(username, password);
        if (roleFromCredentials != null) return roleFromCredentials;

        // 2. Check employee database for regular login
        String[] emp = employeeDAO.findRawById(username); 

        // UPDATED: Guard check to match your new 19-column DAO framework
        if (emp != null && emp.length >= 19) { 
            String storedLastName = emp[1].trim();

            if (storedLastName.equalsIgnoreCase(password.trim())) {
                // Read from Index 11 safely matching the explicit DAO select query position
                String position = emp[11].trim().toUpperCase(); 

                // HR Roles
                if (position.equalsIgnoreCase("HR Manager") ||
                    position.equalsIgnoreCase("HR Rank and File")) {
                    return ROLE_HR;
                }               
                // Finance roles
                else if (position.equalsIgnoreCase("Payroll Manager") ||
                         position.equalsIgnoreCase("Payroll Rank and File") ||
                         position.equalsIgnoreCase("Payroll Team Leader") ||
                         position.equalsIgnoreCase("Accounting Head")) {
                    return ROLE_FINANCE;
                }
                // IT roles
                else if (position.equalsIgnoreCase("IT Operations and Systems")) {
                    return ROLE_IT_SUPPORT;
                }
                // Default fallback role configuration
                return ROLE_EMPLOYEE;
            }
        }

        return null; // Login failed
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Business Logic — uses Employee objects (OOP requirement)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Returns all employees as Employee objects.
     */
    public Iterable<Employee> getAllEmployeeObjects() {
        return employeeDAO.findAll();
    }

    /**
     * Returns a single Employee object by ID.
     */
    public Employee getEmployeeObject(String id) {
        return employeeDAO.findById(id);
    }

    /**
     * Calculates gross pay using polymorphism.
     * Each employee type overrides calculateGrossPay() differently.
     */
    public double calculateGrossPay(String employeeId) {
        Employee emp = employeeDAO.findById(employeeId);
        if (emp == null) return 0.0;
        return emp.calculateGrossPay(); // Polymorphism!
    }

    /**
     * Returns the full name of an employee.
     */
    public String getEmployeeName(String employeeId) {
        Employee emp = employeeDAO.findById(employeeId);
        if (emp != null) return emp.getFullName();
        return employeeId; // fallback to ID
    }

    /**
     * Returns the employee type (Regular, Contractual, etc.)
     */
    public String getEmployeeType(String employeeId) {
        Employee emp = employeeDAO.findById(employeeId);
        if (emp != null) return emp.getEmployeeType();
        return "Unknown";
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Backward Compatible Methods (Ensures 19-column arrays run cleanly)
    // ════════════════════════════════════════════════════════════════════════
    public List<String[]> getAllEmployees() { 
        return employeeDAO.findAllRaw(); 
    }
    
    public String[] getEmployeeById(String id) { 
        String[] empData = employeeDAO.findRawById(id);
        
        // Safety scrubber: ensure no indices turn into blank null strings
        if (empData != null) {
            for (int i = 0; i < empData.length; i++) {
                if (empData[i] == null) {
                    empData[i] = "0";
                }
            }
        }
        return empData; 
    }
    
    public boolean addEmployee(String[] row)                       { return employeeDAO.append(row); }
    public boolean updateField(String id, String col, String val) { return employeeDAO.updateField(id, col, val); }
    public boolean deleteEmployee(String id)                      { return employeeDAO.deleteById(id); }
}