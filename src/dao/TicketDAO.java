/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Ticket;
import java.sql.*;
import java.util.*;

public class TicketDAO {

    // ── Primary JDBC Methods (Returning Ticket objects) ─────────────────────
    
    public boolean addTicket(String employeeId, String subject, String priority) {
        String query = "INSERT INTO SupportTicket (EmployeeID, Subject, Priority, Status) VALUES (?, ?, ?, 'Open')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            pstmt.setString(2, subject);
            pstmt.setString(3, priority);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Ticket> findAll() {
        List<Ticket> list = new ArrayList<>();
        String query = "SELECT TicketID, EmployeeID, Subject, Priority, Status FROM SupportTicket";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Ticket(
                    String.valueOf(rs.getInt("TicketID")),
                    rs.getString("EmployeeID"),
                    rs.getString("Subject"),
                    rs.getString("Priority"),
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean updateStatus(int ticketId, String status) {
        String query = "UPDATE SupportTicket SET Status = ? WHERE TicketID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, ticketId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Bridge Methods (Now returning List<Ticket> to match GUI expectations) ──
    
    public boolean submitTicket(String employeeId, String category, String subject, String description) {
        return addTicket(employeeId, subject, "Normal"); 
    }

    public boolean updateTicketStatus(String ticketID, String newStatus) {
        return updateStatus(Integer.parseInt(ticketID), newStatus);
    }

    public List<Ticket> getAllTickets() {
        return findAll();
    }
    
    public List<Ticket> getTicketsByEmployee(String employeeId) {
        List<Ticket> list = new ArrayList<>();
        String query = "SELECT TicketID, EmployeeID, Subject, Priority, Status FROM SupportTicket WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Ticket(
                    String.valueOf(rs.getInt("TicketID")),
                    rs.getString("EmployeeID"),
                    rs.getString("Subject"),
                    rs.getString("Priority"),
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
