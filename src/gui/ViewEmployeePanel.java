package gui;

import model.PayrollLogic;
import model.Deductions;
import model.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Vector;

public class ViewEmployeePanel extends JPanel {

    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Color GRADIENT_START = new Color(255, 204, 229);
    private static final Color GRADIENT_END = new Color(255, 229, 180);
    
    public JPanel getContentPane() {
        return this; 
    }

    public ViewEmployeePanel(Vector<Object> employeeData) {
        setLayout(new BorderLayout());

        // ===== LEFT PANEL =====
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        String[][] sections = {
            {"Personal Information", "Employee No.", "Last Name", "First Name", "Birthday", "Address", "Phone Number"},
            {"Government Identifications", "SSS No.", "PhilHealth No.", "TIN No.", "PAG-IBIG No."},
            {"Job Information", "Status", "Position", "Immediate Supervisor"},
            {"Compensation & Benefits", "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"}
        };

        int dataIndex = 0;
        gbc.gridy = 0;

        for (String[] section : sections) {
            JLabel sectionTitle = new JLabel(section[0]);
            sectionTitle.setFont(HEADER_FONT);
            sectionTitle.setForeground(new Color(70, 70, 70));
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            leftPanel.add(sectionTitle, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;

            for (int i = 1; i < section.length; i++) {
                gbc.gridx = 0;
                JLabel label = new JLabel(section[i] + ":");
                label.setFont(UI_FONT);
                leftPanel.add(label, gbc);

                gbc.gridx = 1;
                JTextArea dataField = new JTextArea(employeeData.get(dataIndex++).toString());
                dataField.setWrapStyleWord(true);
                dataField.setLineWrap(true);
                dataField.setEditable(false);
                dataField.setOpaque(false);
                dataField.setFont(UI_FONT);
                dataField.setBorder(null);
                leftPanel.add(dataField, gbc);

                gbc.gridy++;
            }

            gbc.gridy++;
        }

        JScrollPane contentScrollPane = new JScrollPane(leftPanel);
        contentScrollPane.setBorder(null);
        contentScrollPane.getVerticalScrollBar().setUI(createScrollBarUI());

        // ===== RIGHT PANEL =====
        JPanel rightPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setFont(UI_FONT);
        comboBox.addItem("Select Month");
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setFont(UI_FONT);
                c.setForeground("Select Month".equals(value) ? Color.GRAY : Color.BLACK);
                return c;
            }
        });
        
        // Bottom panel to hold the Back button
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setOpaque(false);
        navigationPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });

        navigationPanel.add(backBtn);
        add(navigationPanel, BorderLayout.SOUTH);

        JButton submitButton = new JButton("Compute");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setBackground(Color.BLACK);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder());
        submitButton.setPreferredSize(new Dimension(130, 40));

        JTextPane rightTextPane = new JTextPane();
        rightTextPane.setContentType("text/html");
        rightTextPane.setEditable(false);
        rightTextPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rightTextPane.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane textAreaScrollPane = new JScrollPane(rightTextPane);
        textAreaScrollPane.setBorder(null);
        textAreaScrollPane.getVerticalScrollBar().setUI(createScrollBarUI());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        topPanel.add(comboBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(submitButton);

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(textAreaScrollPane, BorderLayout.CENTER);

        // ===== FILE HANDLING & LOGIC =====
        FileHandler fileHandler = new FileHandler();
        fileHandler.readEmployeeFile();
        fileHandler.readAttendanceFile();
        String employeeId = employeeData.get(0).toString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        Set<Month> availableMonths = new TreeSet<>();
        for (String[] record : fileHandler.getAttendanceData()) {
            if (record[0].equals(employeeId)) {
                try {
                    LocalDate date = LocalDate.parse(record[1], formatter);
                    availableMonths.add(date.getMonth());
                } catch (DateTimeParseException ignored) {}
            }
        }
        for (Month month : availableMonths) {
            comboBox.addItem(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }

        submitButton.addActionListener(e -> {
            String selectedMonth = (String) comboBox.getSelectedItem();
            if (selectedMonth == null || selectedMonth.equals("Select Month")) {
                JOptionPane.showMessageDialog(this, "Please select a valid month.");
                return;
            }

            // ── REMOVED: totalLateMinutes tracking based from feedback ──
            int totalWorkedMinutes = 0;

            for (String[] record : fileHandler.getAttendanceData()) {
                if (record[0].equals(employeeId)) {
                    try {
                        LocalDate date = LocalDate.parse(record[1], formatter);
                        if (date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals(selectedMonth)) {
                            int worked = calculateWorkedMinutes(record[2], record[3]);
                            totalWorkedMinutes += worked;
                        }
                    } catch (DateTimeParseException ignored) {}
                }
            }

            double totalHoursWorked = totalWorkedMinutes / 60.0;
            String[] emp = fileHandler.getEmployeeById(employeeId);
            if (emp == null) {
                JOptionPane.showMessageDialog(this, "Employee not found.");
                return;
            }

            try {
    double hourlyRate  = safeParseDouble(emp[18]);
    double basicSalary = safeParseDouble(emp[13]);

    // Validation
    if (hourlyRate <= 0) {
        JOptionPane.showMessageDialog(this, "Invalid hourly rate for this employee.");
        return;
    }
    if (basicSalary <= 0) {
        JOptionPane.showMessageDialog(this, "Invalid basic salary for this employee.");
        return;
    }
    if (totalHoursWorked <= 0) {
        JOptionPane.showMessageDialog(this, "No valid attendance records found for " + selectedMonth + ".");
        return;
    }

    Benefits benefits  = fileHandler.getBenefitsByEmployeeId(employeeId);
    if (benefits == null) {
        JOptionPane.showMessageDialog(this, "No benefits record found for this employee.");
        return;
    }

    double rice        = benefits.getRiceSubsidy();
    double phone       = benefits.getPhoneAllowance();
    double clothing    = benefits.getClothingAllowance();

    PayrollLogic logic  = new PayrollLogic();
    double grossWeekly = logic.calculateGrossWeeklySalary(
            hourlyRate, totalHoursWorked, rice, phone, clothing);

    if (grossWeekly <= 0) {
        JOptionPane.showMessageDialog(this, "Gross salary computation resulted in an invalid value.");
        return;
    }

    Deductions deductions  = new Deductions();
    double sss  = deductions.calculateSSS(basicSalary);
    double ph   = deductions.calculatePhilHealth(basicSalary);
    double pi   = deductions.calculatePagIbig(basicSalary);
    double tax  = deductions.getMonthlyWithholdingTax(basicSalary);
    double weeklyDeductions = (sss + ph + pi + tax) / 4;
    double netMonthly = grossWeekly - weeklyDeductions;
    double netWeekly  = netMonthly / 4;

    // only displays if everything is valid
    rightTextPane.setText(String.format("""
            <html>
            <body style='font-family:Calibri; font-size:11px;'>
            <pre>
        <span style='font-size:16px; font-weight:bold;'>===== SALARY REPORT =====</span>

        <b>BENEFITS:</b>
        \u2022 Rice Subsidy: \u20b1%,.2f
        \u2022 Phone Allowance: \u20b1%,.2f
        \u2022 Clothing Allowance: \u20b1%,.2f
        \u2022 Total Benefits: \u20b1%,.2f

        <b>SALARY:</b>
        \u2022 Gross Monthly Salary (with benefits): \u20b1%,.2f

        <b>DEDUCTIONS (Monthly Basis):</b>
        \u2022 SSS: \u20b1%,.2f
        \u2022 PhilHealth: \u20b1%,.2f
        \u2022 Pag-IBIG: \u20b1%,.2f
        \u2022 Withholding Tax: \u20b1%,.2f
        \u2022 Weekly Deduction Total: \u20b1%,.2f

        <b><span style='color:green;'>NET MONTHLY SALARY: \u20b1%,.2f</span></b>
        <b><span style='color:green;'>NET WEEKLY SALARY: \u20b1%,.2f</span></b>
            </pre>
            </body>
            </html>
            """,
            rice, phone, clothing, rice + phone + clothing,
            grossWeekly,
            sss, ph, pi, tax, weeklyDeductions,
            netMonthly, netWeekly));

} catch (Exception ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(this, "Calculation error: " + ex.getMessage());
}
        });

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                contentScrollPane, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private double safeParseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("\"", "").replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // ── REMOVED: overtime from calculation (feedback) ────────────────────────
    private int calculateWorkedMinutes(String inStr, String outStr) {
        inStr  = sanitizeTime(inStr);
        outStr = sanitizeTime(outStr);

        List<DateTimeFormatter> formats = List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );

        try {
            LocalTime in  = tryParseTime(inStr,  formats);
            LocalTime out = tryParseTime(outStr, formats);
            // Simply return total minutes worked minus lunch break (60 min)
            int total = (int) Duration.between(in, out).toMinutes();
            return Math.max(total - 60, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    private LocalTime tryParseTime(String time, List<DateTimeFormatter> formats) {
        for (DateTimeFormatter f : formats) {
            try { return LocalTime.parse(time, f); }
            catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Invalid time: " + time);
    }

    private String sanitizeTime(String input) {
        return input.replaceAll("[^\\x20-\\x7E]", "").replace("\"", "").trim();
    }

    private BasicScrollBarUI createScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = Color.WHITE;
                this.trackColor = GRADIENT_END;
            }
            @Override protected Dimension getMinimumThumbSize() {
                return new Dimension(8, 30);
            }
        };
    }
}