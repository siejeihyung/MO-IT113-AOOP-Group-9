/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import java.util.*;

public class LeaveDAO {

    // ── Create a new Leave Request ──────────────────────────────────────────
    public boolean addLeave(String employeeId, String type, String start, String end) {
        String query = "INSERT INTO LeaveRequest (EmployeeID, LeaveType, StartDate, EndDate, Status) VALUES (?, ?, ?, ?, 'Pending')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            pstmt.setString(2, type);
            pstmt.setString(3, start);
            pstmt.setString(4, end);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Read all Leave Requests ─────────────────────────────────────────────
    public List<String[]> findAll() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT LeaveID, EmployeeID, LeaveType, StartDate, EndDate, Status FROM LeaveRequest";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("LeaveID")),
                    rs.getString("EmployeeID"),
                    rs.getString("LeaveType"),
                    rs.getString("StartDate"),
                    rs.getString("EndDate"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Update Leave Status (Approve/Reject) ────────────────────────────────
    public boolean updateStatus(int leaveId, String status) {
        String query = "UPDATE LeaveRequest SET Status = ? WHERE LeaveID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, leaveId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Find leaves by Employee ID ──────────────────────────────────────────
    public List<String[]> findByEmployeeId(String employeeId) {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT LeaveID, EmployeeID, LeaveType, StartDate, EndDate, Status FROM LeaveRequest WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("LeaveID")),
                    rs.getString("EmployeeID"),
                    rs.getString("LeaveType"),
                    rs.getString("StartDate"),
                    rs.getString("EndDate"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Find leaves by Status ───────────────────────────────────────────────
    public List<String[]> findByStatus(String status) {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT LeaveID, EmployeeID, LeaveType, StartDate, EndDate, Status FROM LeaveRequest WHERE Status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("LeaveID")),
                    rs.getString("EmployeeID"),
                    rs.getString("LeaveType"),
                    rs.getString("StartDate"),
                    rs.getString("EndDate"),
                    rs.getString("Status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
