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
 *  - Clock in / Clock out
 *  - Late calculation
 *  - Deduction calculation
 *  - Fetching records per employee
 */
public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    private static final LocalTime STANDARD_START   = LocalTime.of(8, 0);
    private static final int       GRACE_MINUTES     = 10;
    private static final DateTimeFormatter TIME_FMT  = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
        this.attendanceDAO.load();
    }

    // ── Clock In ─────────────────────────────────────────────────────────────
    // Records a new attendance entry with login time
    public boolean clockIn(String employeeId, String employeeName) {
        String today    = LocalDate.now().format(DATE_FMT);
        String timeNow  = LocalTime.now().format(TIME_FMT);

        // Business rule: can't clock in twice on the same day
        for (String[] row : attendanceDAO.findByEmployeeId(employeeId)) {
            if (row[1].equals(today)) return false; // already clocked in
        }

        // Row format: employeeId, date, loginTime, logoutTime
        String[] row = { employeeId, today, timeNow, "" };
        return attendanceDAO.append(row);
    }

    // ── Clock Out ────────────────────────────────────────────────────────────
    // Updates the logout time for today's entry
    public boolean clockOut(String employeeId) {
        String today   = LocalDate.now().format(DATE_FMT);
        String timeNow = LocalTime.now().format(TIME_FMT);

        for (String[] row : attendanceDAO.findByEmployeeId(employeeId)) {
            if (row[1].equals(today)) {
                row[3] = timeNow; // update logout time
                return attendanceDAO.update(employeeId, today, row);
            }
        }
        return false; // no clock-in record found for today
    }

    // ── Lateness Calculation ─────────────────────────────────────────────────
    // Returns minutes late beyond the grace period (0 if on time)
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

    // ── Late Deduction ───────────────────────────────────────────────────────
    // Returns the peso amount to deduct based on minutes late and hourly rate
    public double calculateLateDeduction(int minutesLate, double hourlyRate) {
        return minutesLate * (hourlyRate / 60.0);
    }

    // ── Queries ──────────────────────────────────────────────────────────────
    public List<String[]> getAllAttendance() {
        return attendanceDAO.findAll();
    }

    public List<String[]> getAttendanceForEmployee(String employeeId) {
        return attendanceDAO.findByEmployeeId(employeeId);
    }

    // ── Summary: total hours worked for an employee ──────────────────────────
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
