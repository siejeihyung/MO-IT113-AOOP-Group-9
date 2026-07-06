/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;

// ── IMPORT YOUR REPORT ENGINE UTILITIES ─────────────────────────────────────
import reports.PayslipModel;
import reports.ReportGenerator;

/**
 * @author SunnyEljohn
 */
public class PayslipFrame extends JFrame {
    private JPanel printPanel;
    
    // Maintain references to your state parameters for mapping later
    private Vector<Object> employeeData;
    private double grossSalary;
    private double govDeductions;
    private double netTakeHomePay;
    
    private double parseMoney(Object value) {
        if (value == null) return 0.0;
        String cleanValue = value.toString().replace(",", "").replace("\"", "").trim();
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public PayslipFrame(Vector<Object> data, double gross, double totalDeductions, double netPay) {
        this.employeeData = data;
        this.grossSalary = gross;
        this.govDeductions = totalDeductions;
        this.netTakeHomePay = netPay;

        // Safely pull simple identity text properties
        String empId = (data != null && !data.isEmpty()) ? String.valueOf(data.get(0)) : "00000";
        String lastName = (data != null && data.size() > 1) ? String.valueOf(data.get(1)) : "";
        String firstName = (data != null && data.size() > 2) ? String.valueOf(data.get(2)) : "";

        setTitle("MotorPH Payslip - " + empId);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        printPanel = new JPanel();
        printPanel.setBackground(Color.WHITE);
        printPanel.setLayout(new BoxLayout(printPanel, BoxLayout.Y_AXIS));
        printPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // UI View Display
        addLabel("MOTORPH OFFICIAL PAYSLIP", new Font("Segoe UI", Font.BOLD, 18));
        printPanel.add(Box.createVerticalStrut(15));
        addLabel("Employee ID: " + empId);
        addLabel("Name: " + firstName + " " + lastName);
        printPanel.add(new JSeparator());
        printPanel.add(Box.createVerticalStrut(10));

        addLabel("EARNINGS", new Font("Segoe UI", Font.BOLD, 12));
        addLabel(String.format("• Gross Salary: ₱%,.2f", gross));

        // 🟢 FIXED: Check the size of the array before accessing missing allowance indices
        double riceSubsidy = (data != null && data.size() > 14) ? parseMoney(data.get(14)) : 0.0;
        double phoneAllowance = (data != null && data.size() > 15) ? parseMoney(data.get(15)) : 0.0;

        addLabel(String.format("• Rice Subsidy: ₱%,.2f", riceSubsidy));
        addLabel(String.format("• Phone Allowance: ₱%,.2f", phoneAllowance));

        printPanel.add(Box.createVerticalStrut(10));

        addLabel("DEDUCTIONS", new Font("Segoe UI", Font.BOLD, 12));
        addLabel(String.format("• Total Gov. Deductions & Tax: ₱%,.2f", totalDeductions));

        printPanel.add(Box.createVerticalStrut(20));
        printPanel.add(new JSeparator());
        addLabel(String.format("NET PAY: ₱%,.2f", netPay), new Font("Segoe UI", Font.BOLD, 14));

        JButton btn = new JButton("🖨 Open Official Jasper Layout Preview");
        btn.addActionListener(e -> generateJasperPayslip());

        setLayout(new BorderLayout());
        add(new JScrollPane(printPanel), BorderLayout.CENTER);
        add(btn, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void addLabel(String text) { addLabel(text, new Font("Segoe UI", Font.PLAIN, 12)); }
    private void addLabel(String text, Font font) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        printPanel.add(l);
    }

    private void generateJasperPayslip() {
        try {
            List<PayslipModel> payslipData = new ArrayList<>();

            String empId = (employeeData != null && !employeeData.isEmpty()) ? String.valueOf(employeeData.get(0)) : "00000";
            String lastName = (employeeData != null && employeeData.size() > 1) ? String.valueOf(employeeData.get(1)) : "";
            String firstName = (employeeData != null && employeeData.size() > 2) ? String.valueOf(employeeData.get(2)) : "";
            String fullName = firstName + " " + lastName;
            
            PayslipModel slipRecord = new PayslipModel(
                "PS-2026-" + empId, 
                fullName, 
                netTakeHomePay
            );

            // 🟢 FIXED: Safely pass values into the model fields using the verified layout indices
            /*
            double riceSubsidy = (employeeData.size() > 14) ? parseMoney(employeeData.get(14)) : 0.0;
            double phoneAllowance = (employeeData.size() > 15) ? parseMoney(employeeData.get(15)) : 0.0;

            slipRecord.setEmployeeId(empId);
            slipRecord.setGrossIncome(grossSalary);
            slipRecord.setTotalDeductions(govDeductions);
            slipRecord.setRiceSubsidy(riceSubsidy);
            slipRecord.setPhoneAllowance(phoneAllowance);
            */

            payslipData.add(slipRecord);
            ReportGenerator.generatePayslip(payslipData);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error compiling data parameters for Jasper engine:\n" + ex.getMessage(),
                    "Jasper Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}