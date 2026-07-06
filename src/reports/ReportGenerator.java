/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package reports;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javax.swing.JOptionPane;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {

    public static void generatePayslip(List<PayslipModel> dataList) {
        // Validation Rule: Guard against empty lists triggering silent Jasper engine blocks
        if (dataList == null || dataList.isEmpty()) {
            System.err.println("CRITICAL ERROR: Payslip data list is empty or null!");
            JOptionPane.showMessageDialog(null, 
                "Cannot generate payslip. No employee record data selected.", 
                "Report Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            System.out.println("Attempting to locate JRXML template...");
            InputStream reportStream = ReportGenerator.class.getResourceAsStream("src/reports/motorph_employee_payslip.jrxml");

            if (reportStream == null) {
                throw new JRException("The system cannot find the report file layout (src/reports/motorph_employee_payslip.jrxml). Please verify your folder path placement.");
            }

            System.out.println("Compiling Jasper template...");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            System.out.println("Mapping data collection parameters...");
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("companyName", "MotorPH");

            System.out.println("Filling template structural nodes...");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            System.out.println("Launching viewer frame window...");
            // Run this inside a safe Swing thread context to avoid freezing your dashboard screen
            java.awt.EventQueue.invokeLater(() -> {
                try {
                    net.sf.jasperreports.view.JasperViewer.viewReport(jasperPrint, false);
                } catch (Exception windowEx) {
                    windowEx.printStackTrace();
                }
            });

        } catch (JRException e) {
            System.err.println("Jasper Exception Engine Intercepted:");
            e.printStackTrace();
            // Pulls the hidden engine terminal exception into a readable popup dialog
            JOptionPane.showMessageDialog(null, 
                "Failed to render PDF Report Viewer:\n" + e.getMessage(), 
                "Jasper Engine Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}