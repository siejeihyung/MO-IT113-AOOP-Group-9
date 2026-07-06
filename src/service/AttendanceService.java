/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AttendanceDAO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AttendanceService — Business logic for attendance.
 * Updated with aliases to resolve symbol errors in UI panels.
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    private static final LocalTime STANDARD_START  = LocalTime.of(8, 0);
    private static final int     GRACE_MINUTES     = 10;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }
    
    // ── Missing Time Card Method ─────────────────────────────────────────────
    /**
     * Resolves compile error in HRDashboard.
     * Fetches attendance lines and bridges them into report data formats.
     */
    public List<?> getTimeCardData(String employeeId) {
        List<String[]> rawAttendance = attendanceDAO.findByEmployeeId(employeeId);
        
        // If your report template directly accepts an ArrayList of String[], return this:
        return rawAttendance;
    }

    // ── Clock In ─────────────────────────────────────────────────────────────
    public boolean clockIn(String employeeId, String employeeName) {
        String today    = LocalDate.now().format(DATE_FMT);
        String timeNow  = LocalTime.now().format(TIME_FMT);

        for (String[] row : attendanceDAO.findByEmployeeId(employeeId)) {
            if (row[1].equals(today)) return false; 
        }
        return attendanceDAO.append(employeeId, today, timeNow, "");
    }

    // ── Clock Out ────────────────────────────────────────────────────────────
    public boolean clockOut(String employeeId) {
        String today   = LocalDate.now().format(DATE_FMT);
        String timeNow = LocalTime.now().format(TIME_FMT);

        for (String[] row : attendanceDAO.findByEmployeeId(employeeId)) {
            if (row[1].equals(today)) {
                return attendanceDAO.update(employeeId, today, row[2], timeNow);
            }
        }
        return false;
    }

    // ── Queries ──────────────────────────────────────────────────────────────
    public List<String[]> getAllAttendance() {
        return attendanceDAO.findAll();
    }

    /**
     * Resolves error: cannot find symbol 'getAttendanceByEmployee'
     */
    public List<String[]> getAttendanceByEmployee(String employeeId) {
        return attendanceDAO.findByEmployeeId(employeeId);
    }

    /**
     * Resolves error: cannot find symbol 'getAttendanceForEmployee'
     * This is an alias for getAttendanceByEmployee
     */
    public List<String[]> getAttendanceForEmployee(String employeeId) {
        return getAttendanceByEmployee(employeeId);
    }

    // ── Lateness Calculation ─────────────────────────────────────────────────
    public int calculateLateMinutes(String loginTimeStr) {
        if (loginTimeStr == null || loginTimeStr.isEmpty()) return 0;
        try {
            LocalTime loginTime = LocalTime.parse(loginTimeStr, TIME_FMT);
            LocalTime graceEnd  = STANDARD_START.plusMinutes(GRACE_MINUTES);
            if (loginTime.isAfter(graceEnd)) {
                return (int) java.time.Duration.between(graceEnd, loginTime).toMinutes();
            }
        } catch (Exception e) {
            System.out.println("⚠️ Could not parse login time: " + loginTimeStr);
        }
        return 0;
    }

    public double calculateLateDeduction(int minutesLate, double hourlyRate) {
        return minutesLate * (hourlyRate / 60.0);
    }

    public double getTotalHoursWorked(String employeeId) {
        double total = 0;
        for (String[] row : attendanceDAO.findByEmployeeId(employeeId)) {
            if (row.length >= 4 && !row[2].isEmpty() && !row[3].isEmpty()) {
                try {
                    LocalTime login  = LocalTime.parse(row[2], TIME_FMT);
                    LocalTime logout = LocalTime.parse(row[3], TIME_FMT);
                    total += java.time.Duration.between(login, logout).toMinutes() / 60.0;
                } catch (Exception ignored) {}
            }
        }
        return total;
    }
}