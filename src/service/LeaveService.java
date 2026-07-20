/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.LeaveDAO;
import java.time.LocalDate;
import java.util.*;

/**
 * LeaveService — business logic for leave management.
 * Connected directly to MySQL database via LeaveDAO.
 */
public class LeaveService {

    private final LeaveDAO leaveDAO;

    public static final int DEFAULT_SICK_BALANCE      = 5;
    public static final int DEFAULT_VACATION_BALANCE  = 10;
    public static final int DEFAULT_EMERGENCY_BALANCE = 3;

    public LeaveService(LeaveDAO leaveDAO) {
        this.leaveDAO = leaveDAO;
        // leaveDAO.load() removed: JDBC fetches data live from the database.
    }

    // ── File a new leave request ─────────────────────────────────────────────
    public boolean fileLeave(String employeeId, LocalDate date, String type, int days) {
        if (days <= 0) return false;
        if (date.isBefore(LocalDate.now())) return false;

        // Assuming database auto-increments LeaveID, we only pass the data fields
        return leaveDAO.addLeave(employeeId, type, date.toString(), date.plusDays(days).toString());
    }

    // ── Approve ──────────────────────────────────────────────────────────────
    public boolean approveLeave(String leaveID) {
        // Convert String ID to int as required by LeaveDAO
        int id = Integer.parseInt(leaveID);
        if (!"Pending".equals(getStatusById(id))) return false;
        return leaveDAO.updateStatus(id, "Approved");
    }

    // ── Reject ───────────────────────────────────────────────────────────────
    public boolean rejectLeave(String leaveID) {
        int id = Integer.parseInt(leaveID);
        if (!"Pending".equals(getStatusById(id))) return false;
        return leaveDAO.updateStatus(id, "Rejected");
    }

    // ── Queries ──────────────────────────────────────────────────────────────
    public List<String[]> getAllLeaves() { 
        return leaveDAO.findAll(); 
    }
    
    public List<String[]> getLeavesForEmployee(String empId) { 
        // Note: You may need to add findByEmployeeId(String) to LeaveDAO if not already there
        return leaveDAO.findByEmployeeId(empId); 
    }

    public int countByStatus(String status) {
        if (status == null) return leaveDAO.findAll().size();
        // Uses the helper method created in LeaveDAO to filter by status
        return leaveDAO.findByStatus(status).size();
    }

    // ── Remaining balance ────────────────────────────────────────────────────
    public int getRemainingBalance(String employeeId, String type) {
        int defaultBalance = switch (type) {
            case "Sick"      -> DEFAULT_SICK_BALANCE;
            case "Vacation"  -> DEFAULT_VACATION_BALANCE;
            case "Emergency" -> DEFAULT_EMERGENCY_BALANCE;
            default          -> 0;
        };
        
        int usedDays = 0;
        for (String[] row : leaveDAO.findByEmployeeId(employeeId)) {
            // row mapping: [0]=LeaveID, [1]=EmpID, [2]=Type, [3]=Start, [4]=End, [5]=Status
            if (row[2].equals(type) && "Approved".equals(row[5])) {
                usedDays += 1; // Simplification: counting leave entries as 1 day
            }
        }
        return Math.max(0, defaultBalance - usedDays);
    }

    private String getStatusById(int leaveID) {
        for (String[] row : leaveDAO.findAll()) {
            if (Integer.parseInt(row[0]) == leaveID) {
                return row[5];
            }
        }
        return null;
    }
}
