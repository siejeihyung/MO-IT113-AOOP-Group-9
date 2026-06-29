/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.LeaveDAO;
import model.Leave;
import java.time.LocalDate;
import java.util.*;

/**
 * LeaveService — business logic for leave management.
 * Reason field removed as per team feedback.
 */
public class LeaveService {

    private final LeaveDAO leaveDAO;

    public static final int DEFAULT_SICK_BALANCE      = 5;
    public static final int DEFAULT_VACATION_BALANCE  = 10;
    public static final int DEFAULT_EMERGENCY_BALANCE = 3;

    public LeaveService(LeaveDAO leaveDAO) {
        this.leaveDAO = leaveDAO;
        this.leaveDAO.load();
    }

    // ── File a new leave request (no reason needed) ──────────────────────────
    public boolean fileLeave(String employeeId, LocalDate date, String type, int days) {
        if (days <= 0) return false;
        if (date.isBefore(LocalDate.now())) return false;

        String leaveID = "LV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Leave leave = new Leave(leaveID, employeeId, date, type, days);
        return leaveDAO.append(leave.toRow());
    }

    // ── Approve ──────────────────────────────────────────────────────────────
    public boolean approveLeave(String leaveID) {
        if (!"Pending".equals(getStatusById(leaveID))) return false;
        return leaveDAO.updateStatus(leaveID, "Approved");
    }

    // ── Reject ───────────────────────────────────────────────────────────────
    public boolean rejectLeave(String leaveID) {
        if (!"Pending".equals(getStatusById(leaveID))) return false;
        return leaveDAO.updateStatus(leaveID, "Rejected");
    }

    // ── Queries ──────────────────────────────────────────────────────────────
    public List<String[]> getAllLeaves()                          { return leaveDAO.findAll(); }
    public List<String[]> getLeavesForEmployee(String empId)     { return leaveDAO.findByEmployeeId(empId); }

    public int countByStatus(String status) {
        return status == null ? leaveDAO.findAll().size() : leaveDAO.findByStatus(status).size();
    }

    // ── Remaining balance for an employee per leave type ─────────────────────
    public int getRemainingBalance(String employeeId, String type) {
        int defaultBalance = switch (type) {
            case "Sick"      -> DEFAULT_SICK_BALANCE;
            case "Vacation"  -> DEFAULT_VACATION_BALANCE;
            case "Emergency" -> DEFAULT_EMERGENCY_BALANCE;
            default          -> 0;
        };
        int usedDays = 0;
        for (String[] row : leaveDAO.findByEmployeeId(employeeId)) {
            if (row[3].equals(type) && "Approved".equals(row[5])) {
                usedDays += Integer.parseInt(row[4]);
            }
        }
        return Math.max(0, defaultBalance - usedDays);
    }

    private String getStatusById(String leaveID) {
        for (String[] row : leaveDAO.findAll())
            if (row.length > 1 && row[1].equals(leaveID)) return row[5];
        return null;
    }
}