package model;

// Imports all main classes from the OpenCSV library
// Used for reading and writing CSV (Comma-Separated Values) files
import com.opencsv.*;

// Imports specific exception class from OpenCSV
// Thrown when a CSV file contains invalid or unreadable data
import com.opencsv.exceptions.CsvValidationException;

// Imports I/O classes for reading from and writing to files
// Includes FileReader, FileWriter, BufferedReader, etc.
import java.io.*;

// Imports utility classes from the Java Collections Framework
// Includes ArrayList, HashMap, List, Set, etc., commonly used for storing and manipulating data
import java.util.*;

public class FileHandler {

    // ======== Fields and Constants ========
    // Folder path where the employee and attendance files are stored
    private final String folderPath = "data";
    private final String credentialsFilePath = "data/credentials.csv";

    // Storage for headers and data rows of employee records
    private final List<String> employeeHeaders = new ArrayList<>();
    private final List<String[]> employeeData = new ArrayList<>();

    // Storage for headers and data rows of attendance records
    private final List<String> attendanceHeaders = new ArrayList<>();
    private final List<String[]> attendanceData = new ArrayList<>();

    // File paths
    private final String EMPLOYEE_FILE = folderPath + "/employee.csv";
    private final String ATTENDANCE_FILE = folderPath + "/attendance.csv";
    
    // ── NEW: Leave file path and data storage ────────────────────────────────
    private final String LEAVE_FILE = folderPath + "/leave.csv";
    private final List<String[]> leaveData = new ArrayList<>();

    // ======== Read Methods ========

    // Reads and parses the employee file, storing headers and data
    public void readEmployeeFile() {
        File file = new File(EMPLOYEE_FILE);
        if (!file.exists()) {
            System.out.println("❌ employee.csv not found.");
            return;
        }

        employeeHeaders.clear();
        employeeData.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Split line by semicolon, accounting for quoted strings
                String[] fields = line.split("(?=(?:[^\"]*\"[^\"]*\")*[^\"]*)\\;");
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].replaceAll("^\"|\"$", "").trim(); // Remove outer quotes
                }

                if (isFirstLine) {
                    employeeHeaders.addAll(Arrays.asList(fields));
                    isFirstLine = false;
                } else {
                    fields = adjustRowLength(fields, employeeHeaders.size()); // Normalize row length
                    employeeData.add(fields);
                }
            }

            System.out.println("✅ employee.csv loaded: " + employeeData.size() + " rows");

        } catch (IOException e) {
            System.out.println("❌ Error reading employee file: " + e.getMessage());
        }
    }
    
    public boolean authenticateUser(String username, String password) {
        // Admin Credentials Logic (from credentials.txt)
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 2) {
                    if (parts[0].trim().equals(username) && parts[1].trim().equals(password)) {
                        return true; // admin match found
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Employee CSV (if admin check failed)
        // This part allows employee to use ID as username and Last name as password 
        readEmployeeFile();
            
        if (employeeData != null) {
            for (String[] employee : employeeData) {
                if (employee.length > 1) {
                    String employeeId = employee[0].trim();
                    String lastName = employee[1].trim();

                    if (employeeId.equals(username) && lastName.equalsIgnoreCase(password)) {
                        System.out.println("Logging in: " + lastName);
                        return true;
                    }
                }
            }
        }       
        System.out.println("No match found for: " + username);
        return false;
    }
        
    // Reads and parses the attendance file using OpenCSV
    public void readAttendanceFile() {
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) {
            System.out.println("❌ attendance.csv not found.");
            return;
        }

        attendanceHeaders.clear();
        attendanceData.clear();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length == 0 || line[0].trim().isEmpty()) continue;
                attendanceData.add(line);
            }

            System.out.println("✅ Attendance data loaded: " + attendanceData.size() + " rows");

        } catch (IOException | CsvValidationException e) {
            System.out.println("❌ Error reading attendance.csv: " + e.getMessage());
        }
    }

    // ── NEW: Reads leave.txt into leaveData ──────────────────────────────────
    // Row format: employeeId, leaveID, date, type, days, reason, status
    public void readLeaveFile() {
        leaveData.clear();
        File file = new File(LEAVE_FILE);
        if (!file.exists()) {
            System.out.println("ℹ️  leave.csv not found — will be created on first save.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                leaveData.add(line.split(",", -1));
            }
            System.out.println("✅ leave.csv loaded: " + leaveData.size() + " rows");
        } catch (IOException e) {
            System.out.println("❌ Error reading leave.csv: " + e.getMessage());
        }
    }

    // ======== Write Methods ========

    // Writes employee data to the employee file
    public void writeEmployeeFile(List<String[]> data) {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE))
                .withSeparator(';')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(employeeHeaders.toArray(String[]::new));
            for (String[] row : data) {
                writer.writeNext(row);
            }

            System.out.println("✅ employee.csv written successfully.");

        } catch (IOException e) {
            System.out.println("❌ Error writing employee file: " + e.getMessage());
        }
    }

    // Writes attendance data to the attendance file
    public void writeAttendanceFile(List<String[]> data) {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(ATTENDANCE_FILE))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {

            writer.writeNext(attendanceHeaders.toArray(String[]::new));
            for (String[] row : data) {
                writer.writeNext(row);
            }

            System.out.println("✅ attendance.csv written successfully.");

        } catch (IOException e) {
            System.out.println("❌ Error writing attendance file: " + e.getMessage());
        }
    }

    // ── NEW: Overwrites leave.txt with the full leaveData list ───────────────
    public void writeLeaveFile(List<String[]> data) {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(LEAVE_FILE))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            for (String[] row : data) {
                writer.writeNext(row);
            }
            System.out.println("✅ leave.csv written: " + data.size() + " rows");
        } catch (IOException e) {
            System.out.println("❌ Error writing leave.csv: " + e.getMessage());
        }
    }

    // ======== Update & Append Methods ========

    // Appends a new employee row to the employee file
    public boolean appendEmployeeToFile(String[] employeeRow) {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE, true))
                .withSeparator(';')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            writer.writeNext(employeeRow);
            return true;
        } catch (IOException e) {
            System.out.println("❌ Error appending to employee.csv: " + e.getMessage());
            return false;
        }
    }

    // Updates a specific field of an employee identified by employee ID
    public boolean updateEmployeeField(String employeeId, String columnName, String newValue) {
        int idIndex = employeeHeaders.indexOf("Employee #");
        int columnIndex = employeeHeaders.indexOf(columnName);

        if (idIndex == -1 || columnIndex == -1) return false;

        for (String[] row : employeeData) {
            if (row[idIndex].equals(employeeId)) {
                row[columnIndex] = newValue;
                writeEmployeeFile(employeeData);
                return true;
            }
        }
        return false;
    }

    // Updates multiple benefit fields of an employee by ID
    public boolean updateBenefitsByEmployeeId(String employeeId, Benefits benefits) {
        return updateEmployeeField(employeeId, "Rice Subsidy", String.valueOf(benefits.getRiceSubsidy())) &&
               updateEmployeeField(employeeId, "Phone Allowance", String.valueOf(benefits.getPhoneAllowance())) &&
               updateEmployeeField(employeeId, "Clothing Allowance", String.valueOf(benefits.getClothingAllowance()));
    }

    // Updates attendance record by employee ID and date
    public boolean updateAttendance(String employeeId, String date, String[] newRow) {
        for (int i = 0; i < attendanceData.size(); i++) {
            String[] row = attendanceData.get(i);
            if (row[0].equals(employeeId) && row[1].equals(date)) {
                attendanceData.set(i, newRow);
                writeAttendanceFile(attendanceData);
                return true;
            }
        }
        return false;
    }

    // ── NEW: Appends one leave row to leave.txt without rewriting the whole file
    public boolean appendLeave(String[] leaveRow) {
        new File(folderPath).mkdirs(); // Ensure data/ folder exists
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(LEAVE_FILE, true))
                .withSeparator(',')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            writer.writeNext(leaveRow);
            leaveData.add(leaveRow); // Keep in-memory list in sync
            return true;
        } catch (IOException e) {
            System.out.println("❌ Error appending to leave.csv: " + e.getMessage());
            return false;
        }
    }

    // ── NEW: Updates the status of a leave record by leaveID ─────────────────
    public boolean updateLeaveStatus(String leaveID, String newStatus) {
        for (String[] row : leaveData) {
            if (row.length > 1 && row[1].equals(leaveID)) {
                row[6] = newStatus;
                writeLeaveFile(leaveData);
                return true;
            }
        }
        return false;
    }

    // ======== Delete Methods ========

    // Deletes an employee record by ID
    public boolean deleteEmployeeById(String employeeId) {
        int idIndex = employeeHeaders.indexOf("Employee #");
        boolean removed = employeeData.removeIf(row -> row[idIndex].equals(employeeId));
        if (removed) writeEmployeeFile(employeeData);
        return removed;
    }

    // Deletes attendance entry for an employee on a given date
    public boolean deleteAttendance(String employeeId, String date) {
        boolean removed = attendanceData.removeIf(row -> row[0].equals(employeeId) && row[1].equals(date));
        if (removed) writeAttendanceFile(attendanceData);
        return removed;
    }

    // ======== Utility Methods ========

    // Retrieves benefits object by employee ID
    public Benefits getBenefitsByEmployeeId(String employeeId) {
        for (String[] emp : employeeData) {
            if (emp[0].equals(employeeId)) {
                double rice = safeParseDouble(emp[14], 0.0);
                double phone = safeParseDouble(emp[15], 0.0);
                double clothing = safeParseDouble(emp[16], 0.0);
                return new Benefits(rice, phone, clothing);
            }
        }
        return new Benefits(0.0, 0.0, 0.0);
    }

    // Parses string to double with fallback in case of error
    private double safeParseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value.replace("\"", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse double: " + value);
            return defaultValue;
        }
    }

    // Adjusts the length of the row to match expected number of columns
    private String[] adjustRowLength(String[] fields, int expectedSize) {
        if (fields.length < expectedSize) {
            String[] padded = Arrays.copyOf(fields, expectedSize);
            Arrays.fill(padded, fields.length, expectedSize, "");
            return padded;
        } else if (fields.length > expectedSize) {
            return Arrays.copyOf(fields, expectedSize);
        } else {
            return fields;
        }
    }

    // ======== Getters ========

    public List<String[]> getEmployeeData() { return employeeData; }
    public List<String[]> getAttendanceData() { return attendanceData; }

    // ── NEW: Returns all leave rows ───────────────────────────────────────────
    public List<String[]> getAllLeaveData() { return leaveData; }

    // ── NEW: Returns leave rows for a specific employee ───────────────────────
    public List<String[]> getLeavesByEmployeeId(String employeeId) {
        List<String[]> result = new ArrayList<>();
        for (String[] row : leaveData) {
            if (row.length > 0 && row[0].equals(employeeId)) result.add(row);
        }
        return result;
    }

    public String[] getEmployeeHeaders() {
        return new String[]{
                "Employee #", "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
        };
    }

    public List<String> getAttendanceHeaders() { return attendanceHeaders; }

    public String[] getEmployeeById(String employeeId) {
        int idIndex = employeeHeaders.indexOf("Employee #");
        for (String[] row : employeeData) {
            if (row[idIndex].equals(employeeId)) return row;
        }
        return null;
    }

    public boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equals(username)) return true;
            }
        } catch (IOException e) {
            System.err.println("Error reading credentials file: " + e.getMessage());
        }
        return false;
    }

    public boolean addUser(String username, String password) {
        if (username.contains(",") || password.contains(",")) {
            System.err.println("Username and password must not contain commas.");
            return false;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(credentialsFilePath, true))) {
            writer.write(username + "," + password);
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to credentials file: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserPassword(String username, String newPassword) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equals(username)) {
                    lines.add(username + "," + newPassword);
                    updated = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return false;
        }
        if (updated) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(credentialsFilePath))) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                System.err.println("Error writing file: " + e.getMessage());
            }
        }
        return false;
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(credentialsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) users.add(parts[0].trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        return users;
    }
    public boolean isIdDuplicate(String newId) {
        String line;
  
        try (BufferedReader br = new BufferedReader(new FileReader("employee.csv"))) {
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length > 0) {
                    String existingId = data[0].trim();
                    if (existingId.equalsIgnoreCase(newId.trim())) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
}