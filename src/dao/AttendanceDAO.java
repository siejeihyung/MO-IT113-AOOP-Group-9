/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import java.util.*;

public class AttendanceDAO {

    // ── Create ──────────────────────────────────────────────────────────────
    public boolean append(String employeeId, String date, String timeIn, String breakOut, String breakIn, String timeOut, String remarks) {
        String query = "INSERT INTO Attendance (EmployeeID, AttendanceDate, TimeIn, BreakOut, BreakIn, TimeOut, Remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            pstmt.setString(2, date);
            pstmt.setString(3, timeIn);
            pstmt.setString(4, breakOut);
            pstmt.setString(5, breakIn);
            pstmt.setString(6, timeOut);
            pstmt.setString(7, remarks);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Read All ────────────────────────────────────────────────────────────
    public List<String[]> findAll() {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT EmployeeID, AttendanceDate, TimeIn, BreakOut, BreakIn, TimeOut, Remarks FROM Attendance";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("EmployeeID"),
                    rs.getString("AttendanceDate"),
                    rs.getString("TimeIn"),
                    rs.getString("BreakOut"),
                    rs.getString("BreakIn"),
                    rs.getString("TimeOut"),
                    rs.getString("Remarks")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Read Single Employee ────────────────────────────────────────────────
    public List<String[]> findByEmployeeId(String employeeId) {
        List<String[]> list = new ArrayList<>();
        String query = "SELECT EmployeeID, AttendanceDate, TimeIn, BreakOut, BreakIn, TimeOut, Remarks FROM Attendance WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("EmployeeID"),
                        rs.getString("AttendanceDate"),
                        rs.getString("TimeIn"),
                        rs.getString("BreakOut"),
                        rs.getString("BreakIn"),
                        rs.getString("TimeOut"),
                        rs.getString("Remarks")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Update ──────────────────────────────────────────────────────────────
    public boolean update(String employeeId, String date, String timeIn, String breakOut, String breakIn, String timeOut, String remarks) {
        String query = "UPDATE Attendance SET TimeIn = ?, BreakOut = ?, BreakIn = ?, TimeOut = ?, Remarks = ? WHERE EmployeeID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, timeIn);
            pstmt.setString(2, breakOut);
            pstmt.setString(3, breakIn);
            pstmt.setString(4, timeOut);
            pstmt.setString(5, remarks);
            pstmt.setString(6, employeeId);
            pstmt.setString(7, date);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Delete ──────────────────────────────────────────────────────────────
    public boolean delete(String employeeId, String date) {
        String query = "DELETE FROM Attendance WHERE EmployeeID = ? AND AttendanceDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            pstmt.setString(2, date);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}