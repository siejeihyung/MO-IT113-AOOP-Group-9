/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import model.Ticket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketDAO {

    private static final String[] POSSIBLE_PATHS = {
        "src/data/ticket.csv",
        "data/ticket.csv",
        "ticket.csv"
    };
    private String activePath = POSSIBLE_PATHS[0];
    private static final String CSV_HEADER = "TicketID,Sender,Category,Subject,Description,Status";

    // ── Load all tickets ──────────────────────────────────────────────────────
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        File file = findFile();
        if (file == null || !file.exists()) {
            System.err.println("❌ TicketDAO: CSV not found.");
            return tickets;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; } // skip header

                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (data.length >= 6) {
                    tickets.add(new Ticket(
                        clean(data[0]), clean(data[1]), clean(data[2]),
                        clean(data[3]), clean(data[4]), clean(data[5])
                    ));
                } else if (data.length == 5) {
                    // Backward compatible with old 5-column format
                    tickets.add(new Ticket(
                        clean(data[0]), clean(data[1]), clean(data[2]),
                        clean(data[3]), "", clean(data[4])
                    ));
                }
            }
            System.out.println("✅ TicketDAO: Loaded " + tickets.size() + " tickets.");
        } catch (IOException e) {
            System.err.println("❌ TicketDAO: Read error: " + e.getMessage());
        }
        return tickets;
    }

    // ── Get tickets by employee ID or name ────────────────────────────────────
    public List<Ticket> getTicketsByEmployee(String employeeId) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : getAllTickets()) {
            if (t.getSenderName().equalsIgnoreCase(employeeId)) {
                result.add(t);
            }
        }
        return result;
    }

    // ── Submit a new ticket ───────────────────────────────────────────────────
    public boolean submitTicket(String senderName, String category,
                                 String subject, String description) {
        String ticketID = "TKT-" + UUID.randomUUID().toString()
                .substring(0, 6).toUpperCase();
        Ticket ticket = new Ticket(ticketID, senderName, category, subject, description);

        // Ensure file exists with header
        File file = findFile();
        String path = (file != null) ? file.getPath() : POSSIBLE_PATHS[0];
        File target = new File(path);

        try {
            // Create parent dirs and header if file doesn't exist
            if (!target.exists()) {
                target.getParentFile().mkdirs();
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(target))) {
                    bw.write(CSV_HEADER);
                    bw.newLine();
                }
            }
            // Append new ticket
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(target, true))) {
                bw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                        ticket.getTicketID(), ticket.getSenderName(),
                        ticket.getCategory(), ticket.getSubject(),
                        ticket.getDescription(), ticket.getStatus()));
                bw.newLine();
            }
            System.out.println("✅ Ticket submitted: " + ticketID);
            return true;
        } catch (IOException e) {
            System.err.println("❌ TicketDAO: Submit error: " + e.getMessage());
            return false;
        }
    }

    // ── Update ticket status ──────────────────────────────────────────────────
    public boolean updateTicketStatus(String ticketID, String newStatus) {
        List<Ticket> tickets = getAllTickets();
        boolean found = false;
        for (Ticket t : tickets) {
            if (t.getTicketID().equals(ticketID)) {
                t.setStatus(newStatus);
                found = true;
                break;
            }
        }
        return found && saveAllTickets(tickets);
    }

    // ── Save all tickets to CSV ───────────────────────────────────────────────
    private boolean saveAllTickets(List<Ticket> tickets) {
        File file = findFile();
        String path = (file != null) ? file.getPath() : POSSIBLE_PATHS[0];
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(CSV_HEADER);
            bw.newLine();
            for (Ticket t : tickets) {
                bw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                        t.getTicketID(), t.getSenderName(), t.getCategory(),
                        t.getSubject(), t.getDescription(), t.getStatus()));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("❌ TicketDAO: Write error: " + e.getMessage());
            return false;
        }
    }

    private File findFile() {
        for (String path : POSSIBLE_PATHS) {
            File file = new File(path);
            if (file.exists()) { activePath = path; return file; }
        }
        return null;
    }

    private String clean(String s) {
        return s.replace("\"", "").trim();
    }
}