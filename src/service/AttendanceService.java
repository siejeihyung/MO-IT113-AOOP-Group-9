/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AttendanceDAO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AttendanceService {

    private final AttendanceDAO attendanceDAO;

    private static final LocalTime STANDARD_START  = LocalTime.of(8, 0);
    private static final int       GRACE_MINUTES     = 10;
    
    // Internal tracking formats
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    // Display UI custom formats requested (e.g., "Dec 2", "08:am", "12pm")
    private static final DateTimeFormatter UI_DATE_FMT = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter UI_TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public AttendanceService(AttendanceDAO attendanceDAO) {
        this.attendanceDAO = attendanceDAO;
    }

    public List<?> getTimeCardData(String employeeId) {
        return getAttendanceByEmployee(employeeId);
    }

    // ── Clock In ─────────────────────────────────────────────────────────────
    public boolean clockIn(String employeeId, String employeeName) {
        String today    = LocalDate.now().format(DATE_FMT);
        String timeNow  = LocalTime.now().format(TIME_FMT);

        for (String[] row : attendanceDAO.findByEmployeeId(employeeId)) {
            if (row[1].equals(today)) return false; 
        }
        // Appends initial clock-in log with empty placeholders for breaks/outs
        return attendanceDAO.append(employeeId, today, timeNow, "", "", "", "Present");
    }

    // ── Clock Out ────────────────────────────────────────────────────────────
    public boolean clockOut(String employeeId) {
        String today   = LocalDate.now().format(DATE_FMT);
        String timeNow = LocalTime.now().format(TIME_FMT);

        for (String[] dbRow : attendanceDAO.findByEmployeeId(employeeId)) {
            if (dbRow[1].equals(today)) {
                return attendanceDAO.update(employeeId, today, dbRow[2], dbRow[3], dbRow[4], timeNow, dbRow[6]);
            }
        }
        return false;
    }

    // ── Processing and Mapping Rows cleanly into your 8-column layout ──────────
    public List<String[]> getAllAttendance() {
        return formatUiRows(attendanceDAO.findAll(), true);
    }

    public List<String[]> getAttendanceByEmployee(String employeeId) {
        return formatUiRows(attendanceDAO.findByEmployeeId(employeeId), false);
    }

    public List<String[]> getAttendanceForEmployee(String employeeId) {
        return getAttendanceByEmployee(employeeId);
    }

    /**
     * Converts raw DB storage fields into clean UI columns:
     * [Date, Day, Timein, Breakout, Break in, Timeout, Total Hours worked, Remarks]
     */
    private List<String[]> formatUiRows(List<String[]> rawRows, boolean includeIdHeader) {
        List<String[]> formattedList = new ArrayList<>();

        for (String[] dbRow : rawRows) {
            try {
                // dbRow schema map: [0]:EmpID, [1]:Date, [2]:In, [3]:BreakOut, [4]:BreakIn, [5]:Out, [6]:Remarks
                String rawDateStr = dbRow[1];
                LocalDate date = LocalDate.parse(rawDateStr, DATE_FMT);
                
                String uiDateStr = date.format(UI_DATE_FMT); // "Dec 2"
                String uiDayStr  = date.format(DateTimeFormatter.ofPattern("E")); // "Fri"
                
                String timeIn    = formatUiTime(dbRow[2]);
                String breakOut  = formatUiTime(dbRow[3]);
                String breakIn   = formatUiTime(dbRow[4]);
                String timeOut   = formatUiTime(dbRow[5]);
                
                // Calculates total hour durations subtracting intermediate break lengths
                String hoursWorked = calculateNetHours(dbRow[2], dbRow[3], dbRow[4], dbRow[5]);
                String remarks     = (dbRow[6] == null || dbRow[6].isEmpty()) ? "Present" : dbRow[6];

                if (includeIdHeader) {
                    // Prepend Employee ID for HR Dashboard administrative views
                    formattedList.add(new String[]{
                        dbRow[0], uiDateStr, uiDayStr, timeIn, breakOut, breakIn, timeOut, hoursWorked, remarks
                    });
                } else {
                    // Regular clean layout matching your exact specification for individual modules
                    formattedList.add(new String[]{
                        uiDateStr, uiDayStr, timeIn, breakOut, breakIn, timeOut, hoursWorked, remarks
                    });
                }
            } catch (Exception ex) {
                // Fallback rendering safeguard if custom items fail parsing checks
                if (includeIdHeader) {
                    formattedList.add(new String[]{dbRow[0], dbRow[1], "—", dbRow[2], dbRow[3], dbRow[4], dbRow[5], "0 hours", "Error"});
                } else {
                    formattedList.add(new String[]{dbRow[1], "—", dbRow[2], dbRow[3], dbRow[4], dbRow[5], "0 hours", "Error"});
                }
            }
        }
        return formattedList;
    }

    private String formatUiTime(String rawTime) {
        if (rawTime == null || rawTime.isEmpty()) return "—";
        try {
            LocalTime t = LocalTime.parse(rawTime, TIME_FMT);
            return t.format(UI_TIME_FMT).toLowerCase().replace(" ", ""); // standardizes "08:am" / "12pm" formats
        } catch (Exception e) {
            return rawTime;
        }
    }

    private String calculateNetHours(String in, String bOut, String bIn, String out) {
        if (in == null || out == null || in.isEmpty() || out.isEmpty()) return "0 hours";
        try {
            LocalTime timeIn = LocalTime.parse(in, TIME_FMT);
            LocalTime timeOut = LocalTime.parse(out, TIME_FMT);
            long grossMinutes = ChronoUnit.MINUTES.between(timeIn, timeOut);
            
            long breakMinutes = 0;
            if (bOut != null && bIn != null && !bOut.isEmpty() && !bIn.isEmpty()) {
                LocalTime breakOut = LocalTime.parse(bOut, TIME_FMT);
                LocalTime breakIn = LocalTime.parse(bIn, TIME_FMT);
                breakMinutes = ChronoUnit.MINUTES.between(breakOut, breakIn);
            }
            
            double totalHours = (grossMinutes - breakMinutes) / 60.0;
            if (totalHours < 0) totalHours = 0;
            
            // Displays as plain round format or decimal values cleanly
            if (totalHours % 1 == 0) {
                return (int) totalHours + " hours";
            } else {
                return String.format("%.1f hours", totalHours);
            }
        } catch (Exception e) {
            return "0 hours";
        }
    }

    // ── Lateness Rules ───────────────────────────────────────────────────────
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
            // Evaluates structural raw lengths using dynamic helper calculations
            String res = calculateNetHours(row[2], row[3], row[4], row[5]);
            try {
                total += Double.parseDouble(res.replace(" hours", ""));
            } catch (Exception ignored) {}
        }
        return total;
    }
}