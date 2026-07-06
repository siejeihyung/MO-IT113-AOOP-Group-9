/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.util.Vector;

/**
 *
 * @author SunnyEljohn
 */
public class PayslipFrame extends JFrame implements Printable {
    private JPanel printPanel;
    
    private double parseMoney(Object value) {
        if (value == null) return 0.0;
        // Removes commas and quotes so "1,500.00" becomes "1500.00"
        String cleanValue = value.toString().replace(",", "").replace("\"", "").trim();
        try {
            return Double.parseDouble(cleanValue);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Updated Constructor to accept both raw data and calculated results
    public PayslipFrame(Vector<Object> data, double gross, double totalDeductions, double netPay) {
        setTitle("MotorPH Payslip - " + data.get(0));
        setSize(450, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        printPanel = new JPanel();
        printPanel.setBackground(Color.WHITE);
        printPanel.setLayout(new BoxLayout(printPanel, BoxLayout.Y_AXIS));
        printPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header
        addLabel("MOTORPH OFFICIAL PAYSLIP", new Font("Segoe UI", Font.BOLD, 18));
        printPanel.add(Box.createVerticalStrut(15));
        addLabel("Employee ID: " + data.get(0));
        addLabel("Name: " + data.get(2) + " " + data.get(1));
        printPanel.add(new JSeparator());
        printPanel.add(Box.createVerticalStrut(10));

        // Detailed Salary Calculations using the helper
        addLabel("EARNINGS", new Font("Segoe UI", Font.BOLD, 12));
        addLabel(String.format("• Gross Salary: ₱%,.2f", gross));
        addLabel(String.format("• Rice Subsidy: ₱%,.2f", parseMoney(data.get(14))));
        addLabel(String.format("• Phone Allowance: ₱%,.2f", parseMoney(data.get(15))));

        printPanel.add(Box.createVerticalStrut(10));

        addLabel("DEDUCTIONS", new Font("Segoe UI", Font.BOLD, 12));
        addLabel(String.format("• Total Gov. Deductions & Tax: ₱%,.2f", totalDeductions));

        printPanel.add(Box.createVerticalStrut(20));
        printPanel.add(new JSeparator());
        addLabel(String.format("NET PAY: ₱%,.2f", netPay), new Font("Segoe UI", Font.BOLD, 14));

        JButton btn = new JButton("Print to PDF / Printer");
        btn.addActionListener(e -> startPrintJob());

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

    private void startPrintJob() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try { job.print(); } catch (PrinterException ex) { ex.printStackTrace(); }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pi) {
        if (pi > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        printPanel.printAll(g);
        return PAGE_EXISTS;
    }
}