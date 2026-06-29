/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.*;

/**
 * AttendanceDAO — Data Access Object for attendance records.
 * ONLY responsibility: read and write attendance.csv.
 */
public class AttendanceDAO {

    private final String folderPath = "data";
    private final String ATTENDANCE_FILE = folderPath + "/attendance.csv";
    private final List<String[]> data = new ArrayList<>();

    // ── Read ────────────────────────────────────────────────────────────────
    public void load() {
        data.clear();
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) {
            System.out.println("❌ attendance.csv not found.");
            return;
        }

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length == 0 || line[0].trim().isEmpty()) continue;
                data.add(line);
            }
            System.out.println("✅ attendance.csv loaded: " + data.size() + " rows");
        } catch (IOException | CsvValidationException e) {
            System.out.println("❌ Error reading attendance.csv: " + e.getMessage());
        }
    }

    // ── Write ───────────────────────────────────────────────────────────────
    public void save() {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(ATTENDANCE_FILE))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            for (String[] row : data) writer.writeNext(row);
            System.out.println("✅ attendance.csv saved.");
        } catch (IOException e) {
            System.out.println("❌ Error writing attendance.csv: " + e.getMessage());
        }
    }

    // ── Append ──────────────────────────────────────────────────────────────
    public boolean append(String[] row) {
        new File(folderPath).mkdirs();
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(ATTENDANCE_FILE, true))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            writer.writeNext(row);
            data.add(row);
            return true;
        } catch (IOException e) {
            System.out.println("❌ Error appending attendance: " + e.getMessage());
            return false;
        }
    }

    // ── Update ──────────────────────────────────────────────────────────────
    public boolean update(String employeeId, String date, String[] newRow) {
        for (int i = 0; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row[0].equals(employeeId) && row[1].equals(date)) {
                data.set(i, newRow);
                save();
                return true;
            }
        }
        return false;
    }

    // ── Delete ──────────────────────────────────────────────────────────────
    public boolean delete(String employeeId, String date) {
        boolean removed = data.removeIf(r -> r[0].equals(employeeId) && r[1].equals(date));
        if (removed) save();
        return removed;
    }

    // ── Queries ─────────────────────────────────────────────────────────────
    public List<String[]> findAll() { return data; }

    public List<String[]> findByEmployeeId(String employeeId) {
        List<String[]> result = new ArrayList<>();
        for (String[] row : data) {
            if (row[0].equals(employeeId)) result.add(row);
        }
        return result;
    }
}
