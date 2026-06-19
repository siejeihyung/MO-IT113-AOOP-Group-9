/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
 
import com.opencsv.*;
import model.Employee;
import model.RegularEmployee;
import java.io.*;
import java.util.*;
 
/**
 * EmployeeDAO — Data Access Object for employee records.
 * ONLY responsibility: read and write employee.csv
 * No business logic here
 * KEY CHANGE: findById() and findAll() now return Employee objects
 * instead of raw String arrays, fixing the data/business logic leak.
 */
public class EmployeeDAO {
 
    private final String folderPath    = "data";
    private final String EMPLOYEE_FILE = folderPath + "/employee.csv";
 
    private final List<String>   headers = new ArrayList<>();
    private final List<String[]> data    = new ArrayList<>();
 
    // Read 
    public void load() {
        headers.clear();
        data.clear();
 
        File file = new File(EMPLOYEE_FILE);
        if (!file.exists()) {
            System.out.println("employee.csv not found.");
            return;
        }
 
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split("(?=(?:[^\"]*\"[^\"]*\")*[^\"]*)\\;");
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = fields[i].replaceAll("^\"|\"$", "").trim();
                }
                if (isFirstLine) {
                    headers.addAll(Arrays.asList(fields));
                    isFirstLine = false;
                } else {
                    data.add(adjustRowLength(fields, headers.size()));
                }
            }
            System.out.println("employee.csv loaded: " + data.size() + " rows");
        } catch (IOException e) {
            System.out.println("Error reading employee.csv: " + e.getMessage());
        }
    }
 
    // Write 
    public void save() {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE))
                .withSeparator(';')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            writer.writeNext(headers.toArray(String[]::new));
            for (String[] row : data) writer.writeNext(row);
            System.out.println("employee.csv saved.");
        } catch (IOException e) {
            System.out.println("Error writing employee.csv: " + e.getMessage());
        }
    }
 
    // Append
    public boolean append(String[] row) {
        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(EMPLOYEE_FILE, true))
                .withSeparator(';')
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()) {
            writer.writeNext(row);
            data.add(row);
            return true;
        } catch (IOException e) {
            System.out.println("Error appending employee: " + e.getMessage());
            return false;
        }
    }
 
    // Update
    public boolean updateField(String employeeId, String columnName, String newValue) {
        int idIndex  = headers.indexOf("Employee #");
        int colIndex = headers.indexOf(columnName);
        if (idIndex == -1 || colIndex == -1) return false;
 
        for (String[] row : data) {
            if (row[idIndex].equals(employeeId)) {
                row[colIndex] = newValue;
                save();
                return true;
            }
        }
        return false;
    }
 
    // Delete
    public boolean deleteById(String employeeId) {
        int idIndex = headers.indexOf("Employee #");
        boolean removed = data.removeIf(row -> row[idIndex].equals(employeeId));
        if (removed) save();
        return removed;
    }
 
    // Raw Queries
    public String[]       findRawById(String employeeId) {
        int idIndex = headers.indexOf("Employee #");
        for (String[] row : data) {
            if (row[idIndex].equals(employeeId)) return row;
        }
        return null;
    }
 
    public List<String[]> findAllRaw() { return data; }
    public List<String>   getHeaders() { return headers; }
 
    // Employee Object Queries
 
    // Returns a single Employee object by ID.
    public Employee findById(String employeeId) {
        String[] row = findRawById(employeeId);
        return row != null ? mapToEmployee(row) : null;
    }
 
    // Returns all employees as Employee objects.
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        for (String[] row : data) {
            Employee emp = mapToEmployee(row);
            if (emp != null) employees.add(emp);
        }
        return employees;
    }
 
    // Mapper: CSV row → Employee object
    /**
     * Converts a raw CSV String[] row into a RegularEmployee object.
     * This is the ONLY place that knows the column structure of employee.csv.
     *
     * Column mapping:
     *  [0]  Employee ID       [7]  PhilHealth #
     *  [1]  Last Name         [8]  TIN #
     *  [2]  First Name        [9]  Pag-IBIG #
     *  [6]  SSS #             [13] Basic Salary
     *  [14] Rice Subsidy      [15] Phone Allowance
     *  [16] Clothing Allow.   [18] Hourly Rate
     */
    private Employee mapToEmployee(String[] row) {
        try {
            String employeeID  = get(row, 0);
            String lastName    = get(row, 1);
            String firstName   = get(row, 2);
            String sss         = get(row, 6);
            String philhealth  = get(row, 7);
            String tin         = get(row, 8);
            String pagibig     = get(row, 9);
 
            double basicSalary     = parseDouble(get(row, 13));
            double semiMonthlyRate = basicSalary / 2;
            double rice            = parseDouble(get(row, 14));
            double phone           = parseDouble(get(row, 15));
            double clothing        = parseDouble(get(row, 16));
            double totalBenefits   = rice + phone + clothing;
            double hourlyRate      = parseDouble(get(row, 18));
 
            return new RegularEmployee(
                employeeID, lastName, firstName,
                sss, philhealth, tin, pagibig,
                basicSalary, semiMonthlyRate,
                hourlyRate, totalBenefits
            );
        } catch (Exception e) {
            System.err.println("EmployeeDAO: Failed to map row: " + e.getMessage());
            return null;
        }
    }
 
    // Utility
    private String get(String[] row, int index) {
        return (index < row.length) ? row[index] : "";
    }
 
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) { return 0.0; }
    }
 
    private String[] adjustRowLength(String[] fields, int expectedSize) {
        if (fields.length < expectedSize) {
            String[] padded = Arrays.copyOf(fields, expectedSize);
            Arrays.fill(padded, fields.length, expectedSize, "");
            return padded;
        }
        return fields.length > expectedSize ? Arrays.copyOf(fields, expectedSize) : fields;
    }
}