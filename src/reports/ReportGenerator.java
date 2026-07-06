/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package reports;

import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.*;
import javax.swing.JOptionPane;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    
    public static void generateReport(String jrxmlPath, List<?> dataList, Map<String, Object> parameters) {
        // Use try-with-resources to automatically close the InputStream
        try (InputStream reportStream = ReportGenerator.class.getResourceAsStream(jrxmlPath)) {
            
            if (reportStream == null) {
                throw new JRException("Report file not found: " + jrxmlPath);
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // View report inside the Event Dispatch Thread for stability
            java.awt.EventQueue.invokeLater(() -> JasperViewer.viewReport(jasperPrint, false));
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error generating report: " + e.getMessage());
        }
    }

    public static void generatePayslip(List<PayslipModel> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No data selected.", "Report Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use try-with-resources here as well
        try (InputStream reportStream = ReportGenerator.class.getResourceAsStream("/reports/motorph_employee_payslip.jrxml")) {
            
            if (reportStream == null) {
                throw new JRException("Layout file not found.");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("companyName", "MotorPH");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            java.awt.EventQueue.invokeLater(() -> JasperViewer.viewReport(jasperPrint, false));

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to render Report: " + e.getMessage());
        }
    }
}