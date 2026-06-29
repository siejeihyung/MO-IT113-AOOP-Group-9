/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package model;

/**
 *
 * @author SunnyEljohn
 */
public class Ticket {
    private String ticketID;
    private String senderName;
    private String category;
    private String subject;
    private String description;
    private String status; // "Open", "In Progress", "Resolved", "Closed"

    public Ticket(String ticketID, String senderName, String category, String subject, String description) {
        this.ticketID = ticketID;
        this.senderName = senderName;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.status = "Open";
    }
    
    // Constructor for LOADING from CSV (includes status
    public Ticket(String ticketID, String senderName, String category, String subject, String description, String status){
    this(ticketID, senderName, category, subject, description);
    this.status = status; 
    }
    
    // ── Getters ──────────────────────────────────────────────────────────────
    public String getTicketID()    { return ticketID; }
    public String getSenderName()  { return senderName; }
    public String getCategory()    { return category; }
    public String getSubject()     { return subject; }
    public String getDescription() { return description; }
    public String getStatus()      { return status; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setTicketID(String ticketID)       { this.ticketID = ticketID; }
    public void setSenderName(String senderName)   { this.senderName = senderName; }
    public void setCategory(String category)       { this.category = category; }
    public void setSubject(String subject)         { this.subject = subject; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status)           { this.status = status; }

    public Object getID() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
