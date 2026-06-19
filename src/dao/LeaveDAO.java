/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import com.opencsv.*;
import java.io.*;
import java.util.*;

/**
 * LeaveDAO — reads and writes leave.csv.
 * Row format: employeeId, leaveID, date, type, days, status
 * Reason removed as per team feedback.
 */
public class LeaveDAO {

    private final String folderPath  = "data";
    private final String LEAVE_FILE  = folderPath + "/leave.csv";
    private final List<String[]> data = new ArrayList<>();

    // ── Read ────────────────────────────────────────────────────────────────
    public void load() {
        data.clear();
        File file = new File(LEAVE_FILE);
        if (!file.exists()) {
            System.out.println("leave.csv not found — will be created on first save.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                data.add(line.split(",", -1));
            }
            System.out.println("✅ leave.csv loaded: " + data.size() + " rows");
        } catch (IOException e) {
            System.out.println("❌ Error reading leave.csv: " + e.getMessage());
        }
    }

    // ── Write ───────────────────────────────────────────────────────────────
    public void save() {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(LEAVE_FILE))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            for (String[] row : data) writer.writeNext(row);
        } catch (IOException e) {
            System.out.println("❌ Error writing leave.csv: " + e.getMessage());
        }
    }

    // ── Append ──────────────────────────────────────────────────────────────
    public boolean append(String[] row) {
        new File(folderPath).mkdirs();
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(LEAVE_FILE, true))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            writer.writeNext(row);
            data.add(row);
            return true;
        } catch (IOException e) {
            System.out.println("❌ Error appending leave: " + e.getMessage());
            return false;
        }
    }

    // ── Update status (column 5 now, was 6 before reason was removed) ────────
    public boolean updateStatus(String leaveID, String newStatus) {
        for (String[] row : data) {
            if (row.length > 1 && row[1].equals(leaveID)) {
                row[5] = newStatus;
                save();
                return true;
            }
        }
        return false;
    }

    // ── Delete ──────────────────────────────────────────────────────────────
    public boolean delete(String leaveID) {
        boolean removed = data.removeIf(row -> row.length > 1 && row[1].equals(leaveID));
        if (removed) save();
        return removed;
    }

    // ── Queries ─────────────────────────────────────────────────────────────
    public List<String[]> findAll()                         { return data; }

    public List<String[]> findByEmployeeId(String employeeId) {
        List<String[]> result = new ArrayList<>();
        for (String[] row : data)
            if (row.length > 0 && row[0].equals(employeeId)) result.add(row);
        return result;
    }

    public List<String[]> findByStatus(String status) {
        List<String[]> result = new ArrayList<>();
        for (String[] row : data)
            if (row.length > 5 && row[5].equalsIgnoreCase(status)) result.add(row);
        return result;
    }
}
