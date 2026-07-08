/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reports;

import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.view.JasperViewer;
import dao.DatabaseConnection; // Automatically hooks into your existing project DAO

import javax.swing.JOptionPane;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class ReportGenerator {
    
    /**
     * Generic method to fill and display any compiled (.jasper) report 
     * using a direct JDBC database connection.
     */
    public static void generateReport(String jasperPath, Map<String, Object> parameters) {
        // Use try-with-resources to automatically manage and close both the DB connection and file stream
        try (Connection conn = DatabaseConnection.getConnection(); 
             InputStream reportStream = ReportGenerator.class.getResourceAsStream(jasperPath)) {
            
            if (reportStream == null) {
                throw new JRException("Compiled report file not found at path: " + jasperPath);
            }

            // Instead of passing a JRBeanCollectionDataSource, we pass 'conn'
            // Jasper will execute the SQL Query embedded inside the report using this connection
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, conn);
            
            // View report inside the Event Dispatch Thread for Swing stability
            java.awt.EventQueue.invokeLater(() -> JasperViewer.viewReport(jasperPrint, false));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating report: " + e.getMessage());
        }
    }

    /**
     * Generates the Employee Payslip report
     */
    public static void generatePayslip(String employeeId, String periodStartDate, String periodEndDate) {
        Map<String, Object> parameters = new HashMap<>();
        
        // Feed parameters directly matching your employeePayslip.jrxml configuration
        parameters.put("employeeId", employeeId);
        parameters.put("periodStartDate", periodStartDate);
        parameters.put("periodEndDate", periodEndDate);
        parameters.put("companyName", "MotorPH");

        // Directly invoke using the pre-compiled .jasper file path
        generateReport("/reports/employeePayslip.jasper", parameters);
    }

    /**
     * Generates the Payroll Summary report
     */
    public static void generatePayrollSummary() {
        Map<String, Object> parameters = new HashMap<>();
        // Add any global parameters needed by PayrollSummary.jasper here if necessary
        
        generateReport("/reports/PayrollSummary.jasper", parameters);
    }

    /**
     * Generates the Timecard report
     */
    public static void generateTimecard(String employeeId, String periodEndDate) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("employeeId", employeeId);
        parameters.put("periodEndDate", periodEndDate);
        parameters.put("companyName","MotorPH");
        parameters.put("companyAddress","7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
        parameters.put("companyPhone","Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073");
        parameters.put("companyEmail","Email: corporate@motorph.com");
        
        
        generateReport("/reports/timecard.jasper", parameters);
    }
}