/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;

/**
 * Leave — represents a leave request.
 * Row format: employeeId, leaveID, date, type, days, status
 * Note: Reason removed to be more inclusive as per team feedback.
 */
public class Leave {

    private String   leaveID;
    private String   employeeId;
    private LocalDate date;
    private String   type;    // "Sick", "Vacation", "Emergency"
    private int      days;
    private String   status;  // "Pending", "Approved", "Rejected"

    // ── Full constructor ─────────────────────────────────────────────────────
    public Leave(String leaveID, String employeeId, LocalDate date, String type, int days) {
        this.leaveID    = leaveID;
        this.employeeId = employeeId;
        this.date       = date;
        this.type       = type;
        this.days       = days;
        this.status     = "Pending";
    }

    // ── Backward-compatible constructor ──────────────────────────────────────
    public Leave(String leaveID, LocalDate date, String type, int days) {
        this(leaveID, "", date, type, days);
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String    getLeaveID()    { return leaveID; }
    public String    getEmployeeId() { return employeeId; }
    public LocalDate getDate()       { return date; }
    public String    getType()       { return type; }
    public int       getDays()       { return days; }
    public String    getStatus()     { return status; }

    // ── Setter ───────────────────────────────────────────────────────────────
    public void setStatus(String status) { this.status = status; }

    // ── CSV row: employeeId, leaveID, date, type, days, status ───────────────
    public String[] toRow() {
        return new String[]{
            employeeId,
            leaveID,
            date != null ? date.toString() : "",
            type,
            String.valueOf(days),
            status
        };
    }

    // ── Rebuild from CSV row ─────────────────────────────────────────────────
    public static Leave fromRow(String[] row) {
        Leave leave = new Leave(
            row[1],                   // leaveID
            row[0],                   // employeeId
            LocalDate.parse(row[2]),  // date
            row[3],                   // type
            Integer.parseInt(row[4])  // days
        );
        leave.setStatus(row[5]);      // status
        return leave;
    }
}
