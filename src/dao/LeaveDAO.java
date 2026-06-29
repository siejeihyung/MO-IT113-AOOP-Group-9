/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LeaveDAO — reads and writes leave data from MySQL database.
 * Row format: employeeId, leaveID, date, type, days, status
 * Reason removed as per team feedback.
 */
public class LeaveDAO {

    // Helper method to obtain a database connection
    private Connection getConnection() throws SQLException {
        // Points directly to your project's database utility class
        return DatabaseConnection.getConnection();
    }

    // ── Load / Sync (Kept for backward compatibility, now acts as a table verify check) ──
    public void load() {
        String sql = "SELECT COUNT(*) FROM leave_requests";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("✅ leave_requests table synchronized. Current records: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("❌ Database sync error or table missing: " + e.getMessage());
        }
    }

    // ── Save (Kept empty for backward compatibility since MySQL saves instantly on updates) ──
    public void save() {
        // No operation needed here because database commits occur in real-time
    }

    // ── Append (Creates a new leave record in MySQL) ──────────────────────────
    // Expects row: {employeeId, leaveID, date, type, days, status}
    public boolean append(String[] row) {
        String sql = "INSERT INTO leave_requests (EmployeeID, StartDate, LeaveType, Days, Status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, row[0]); // employeeId
            pstmt.setDate(2, Date.valueOf(row[2])); // date (YYYY-MM-DD)
            pstmt.setString(3, row[3]); // type (Sick, Vacation, Emergency)
            pstmt.setInt(4, Integer.parseInt(row[4])); // days
            pstmt.setString(5, row[5]); // status (Pending)

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("❌ Error appending leave to database: " + e.getMessage());
            return false;
        }
    }

    // ── Update status ────────────────────────────────────────────────────────
    public boolean updateStatus(String leaveID, String newStatus) {
        String sql = "UPDATE leave_requests SET Status = ? WHERE LeaveRequestID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, Integer.parseInt(leaveID));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error updating leave status: " + e.getMessage());
            return false;
        }
    }

    // ── Delete ──────────────────────────────────────────────────────────────
    public boolean delete(String leaveID) {
        String sql = "DELETE FROM leave_requests WHERE LeaveRequestID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(leaveID));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error deleting leave record: " + e.getMessage());
            return false;
        }
    }

    // ── Find All ────────────────────────────────────────────────────────────
    public List<String[]> findAll() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT EmployeeID, LeaveRequestID, StartDate, LeaveType, Days, Status FROM leave_requests ORDER BY LeaveRequestID DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("EmployeeID"),
                    String.valueOf(rs.getInt("LeaveRequestID")),
                    rs.getDate("StartDate").toString(),
                    rs.getString("LeaveType"),
                    String.valueOf(rs.getInt("Days")),
                    rs.getString("Status")
                });
            }
        } catch (SQLException e) {
            System.out.println("❌ Error querying all leaves: " + e.getMessage());
        }
        return list;
    }

    // ── Find By Employee ID ─────────────────────────────────────────────────
    public List<String[]> findByEmployeeId(String employeeId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT EmployeeID, LeaveRequestID, StartDate, LeaveType, Days, Status FROM leave_requests WHERE EmployeeID = ? ORDER BY LeaveRequestID DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("EmployeeID"),
                        String.valueOf(rs.getInt("LeaveRequestID")),
                        rs.getDate("StartDate").toString(),
                        rs.getString("LeaveType"),
                        String.valueOf(rs.getInt("Days")),
                        rs.getString("Status")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error querying employee leaves: " + e.getMessage());
        }
        return list;
    }

    // ── Find By Status ──────────────────────────────────────────────────────
    public List<String[]> findByStatus(String status) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT EmployeeID, LeaveRequestID, StartDate, LeaveType, Days, Status FROM leave_requests WHERE Status = ? ORDER BY LeaveRequestID DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("EmployeeID"),
                        String.valueOf(rs.getInt("LeaveRequestID")),
                        rs.getDate("StartDate").toString(),
                        rs.getString("LeaveType"),
                        String.valueOf(rs.getInt("Days")),
                        rs.getString("Status")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error querying leaves by status: " + e.getMessage());
        }
        return list;
    }
}