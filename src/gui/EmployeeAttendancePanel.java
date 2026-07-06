/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import service.AttendanceService;
import service.EmployeeService;
import dao.AttendanceDAO;
import dao.EmployeeDAO;

// ── IMPORT JASPER / REPORT UTILITIES (Adjust packages if necessary) ──────────
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reports.TimeCardModel;

/**
 * EmployeeAttendancePanel — shown inside EmployeeDashboardPanel.
 * Allows an employee to clock in/out and view their own attendance history.
 */
public class EmployeeAttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private final EmployeeService   employeeService;
    private final String            employeeId;

    // ── Table ─────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable table;

    // ── Status labels ─────────────────────────────────────────────────────────
    private JLabel statusLabel;
    private JLabel totalHoursLabel;
    private JLabel clockInBtn;   
    private JLabel clockOutBtn;

    // ── Colors ────────────────────────────────────────────────────────────────
    private final Color HEADER_COLOR  = new Color(20, 50, 110);
    private final Color ROW_ALT       = new Color(220, 230, 250);
    private final Color LATE_COLOR    = new Color(255, 200, 200);
    private final Color ON_TIME_COLOR = new Color(200, 240, 210);
    private final Color GREEN         = new Color(56, 142, 60);
    private final Color RED           = new Color(211, 47, 47);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm[:ss]");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public EmployeeAttendancePanel(String employeeId) {
        this.employeeId       = employeeId;
        this.attendanceService = new AttendanceService(new AttendanceDAO());
        this.employeeService   = new EmployeeService(new EmployeeDAO());

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        add(buildTopSection(),    BorderLayout.NORTH);
        add(buildTableSection(),  BorderLayout.CENTER);
        
        // 🟢 ADDED: Bottom Control Panel to house the Print Time Card layout trigger
        add(buildBottomControlSection(), BorderLayout.SOUTH);

        refreshAll();
    }

    private JPanel buildTopSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel title = new JLabel("My Attendance");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JPanel statusCard = new JPanel(new GridBagLayout());
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 10, 5, 10);
        gc.anchor = GridBagConstraints.CENTER;

        JLabel todayLabel = new JLabel("Today — " + LocalDate.now().toString());
        todayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        todayLabel.setForeground(new Color(29, 69, 143));
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        statusCard.add(todayLabel, gc);

        statusLabel = new JLabel("Checking...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gc.gridy = 1;
        statusCard.add(statusLabel, gc);

        gc.gridy = 2; gc.gridwidth = 1; gc.gridx = 0;
        JButton clockIn  = makeActionButton("🟢 Clock In",  GREEN);
        JButton clockOut = makeActionButton("🔴 Clock Out", RED);

        clockIn.addActionListener(e  -> handleClockIn());
        clockOut.addActionListener(e -> handleClockOut());

        statusCard.add(clockIn,  gc);
        gc.gridx = 1;
        statusCard.add(clockOut, gc);

        JPanel summaryCard = new JPanel();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));
        summaryCard.setBackground(Color.WHITE);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel summaryTitle = new JLabel("This Month");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryTitle.setForeground(new Color(29, 69, 143));
        summaryTitle.setAlignmentX(CENTER_ALIGNMENT);

        totalHoursLabel = new JLabel("0.0 hrs");
        totalHoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalHoursLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hoursSubLabel = new JLabel("Total Hours Worked");
        hoursSubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hoursSubLabel.setForeground(Color.GRAY);
        hoursSubLabel.setAlignmentX(CENTER_ALIGNMENT);

        summaryCard.add(summaryTitle);
        summaryCard.add(Box.createVerticalStrut(5));
        summaryCard.add(totalHoursLabel);
        summaryCard.add(hoursSubLabel);

        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(statusCard);
        cardsRow.add(summaryCard);

        panel.add(title,    BorderLayout.NORTH);
        panel.add(cardsRow, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel historyTitle = new JLabel("Attendance History");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTitle.setForeground(Color.WHITE);

        String[] cols = {"Date", "Clock In", "Clock Out", "Hours Worked", "Minutes Late", "Deduction (₱)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 149, 237));
        table.setSelectionForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new AttendanceCellRenderer());

        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
            new EmptyBorder(5, 5, 5, 5)
        ));
        tableCard.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(scrollPane,             BorderLayout.CENTER);

        panel.add(historyTitle, BorderLayout.NORTH);
        panel.add(tableCard,    BorderLayout.CENTER);
        return panel;
    }

    // ── 🟢 NEW: Bottom section panel containing the operational Print button ──
    private JPanel buildBottomControlSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setOpaque(false);

        JButton printTimeCardBtn = new JButton("🖨 Print Time Card");
        printTimeCardBtn.setPreferredSize(new Dimension(160, 38));
        printTimeCardBtn.setForeground(Color.WHITE);
        printTimeCardBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printTimeCardBtn.setFocusPainted(false);
        printTimeCardBtn.setContentAreaFilled(false);
        printTimeCardBtn.setBorderPainted(false);
        printTimeCardBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        Color buttonBlue = new Color(29, 69, 143);
        printTimeCardBtn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(buttonBlue);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        printTimeCardBtn.addActionListener(e -> generateTimeCardReport());
        panel.add(printTimeCardBtn);
        return panel;
    }

    // ── 🟢 NEW: Compiles Table items directly to JRXML Data engine layout ──────
    // 🟢 UPDATED: Load pre-compiled .jasper binary to avoid Jackson/XML parser errors
    private void generateTimeCardReport() {
        try {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No attendance entries found to print.", 
                        "Empty Record", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Gather employee basic metadata
            String employeeName = "Employee Name";
            String departmentPosition = "N/A";
            
            String[] empData = employeeService.getEmployeeById(employeeId);
            if (empData != null) {
                if (empData.length > 2) {
                    employeeName = empData[2] + " " + empData[1];
                }
                if (empData.length > 4 && empData.length <= 11) {
                    departmentPosition = empData[4]; 
                } else if (empData.length > 13) {
                    departmentPosition = empData[13];
                }
            }

            // 2. Map parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("employeeId", employeeId);
            parameters.put("employeeName", employeeName);
            parameters.put("employeePositionDepartment", departmentPosition);
            parameters.put("periodEndDate", LocalDate.now().format(DATE_FMT));

            // 3. Map rows into data collection
            List<TimeCardModel> reportDataList = new ArrayList<>();
            java.text.SimpleDateFormat parsingFormatter = new java.text.SimpleDateFormat("MM/dd/yyyy");

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String rawDateStr = String.valueOf(tableModel.getValueAt(i, 0));
                java.util.Date parsedDate = null;
                try {
                    parsedDate = parsingFormatter.parse(rawDateStr);
                } catch (Exception ignored) {
                    parsedDate = new java.util.Date();
                }

                reportDataList.add(new TimeCardModel(
                    parsedDate,
                    "",
                    String.valueOf(tableModel.getValueAt(i, 1)),
                    "—",
                    "—",
                    String.valueOf(tableModel.getValueAt(i, 2)),
                    String.valueOf(tableModel.getValueAt(i, 3)),
                    String.valueOf(tableModel.getValueAt(i, 4)).contains("On time") ? "" : "Late: " + tableModel.getValueAt(i, 4)
                ));
            }

            // 4. LOAD THE PRE-COMPILED .JASPER FILE
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(reportDataList);
            java.io.InputStream reportStream = getClass().getResourceAsStream("/reports/timecard.jasper");
            
            if (reportStream == null) {
                throw new java.io.FileNotFoundException("Could not find 'timecard.jasper' in /reports/ path. Please ensure it is compiled and moved there.");
            }

            // 5. Fill and View
            JasperPrint printPreview = JasperFillManager.fillReport(reportStream, parameters, dataSource);

            JasperViewer viewer = new JasperViewer(printPreview, false);
            viewer.setTitle("MotorPH Time Card Preview - ID " + employeeId);
            viewer.setLocationRelativeTo(null);
            viewer.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Print Error: " + ex.getMessage(),
                    "Print Layout Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshAll() {
        tableModel.setRowCount(0);
        double hourlyRate = 0.0;
        
        String[] empData = employeeService.getEmployeeById(employeeId);
        // Added strong length boundary check before parsing index 18
        if (empData != null && empData.length > 18) {
            try { 
                hourlyRate = Double.parseDouble(empData[18].replace(",", "").replace("\"", "").trim());
            } catch (NumberFormatException ignored) {}
        }

        double totalHours = 0.0;
        String today = LocalDate.now().format(DATE_FMT);
        String todayStatus = "Not clocked in yet";

        List<String[]> records = attendanceService.getAttendanceForEmployee(employeeId);

        for (String[] row : records) {
            String date    = row.length > 1 ? row[1] : "";
            String login   = row.length > 2 ? row[2] : "";
            String logout  = row.length > 3 ? row[3] : "";

            double hours = 0.0;
            if (!login.isEmpty() && !logout.isEmpty()) {
                try {
                    LocalTime in  = LocalTime.parse(login,  TIME_FMT);
                    LocalTime out = LocalTime.parse(logout, TIME_FMT);
                    hours = java.time.Duration.between(in, out).toMinutes() / 60.0;
                    totalHours += hours;
                } catch (Exception ignored) {}
            }

            int lateMin = attendanceService.calculateLateMinutes(login);
            double deduction = attendanceService.calculateLateDeduction(lateMin, hourlyRate);

            tableModel.addRow(new Object[]{
                date,
                login.isEmpty()  ? "—" : login,
                logout.isEmpty() ? "—" : logout,
                hours > 0 ? String.format("%.2f hrs", hours) : "—",
                lateMin > 0 ? lateMin + " min" : "On time ✅",
                deduction > 0 ? String.format("%.2f", deduction) : "0.00"
            });

            if (date.equals(today)) {
                if (!login.isEmpty() && logout.isEmpty()) {
                    todayStatus = "⏱ Clocked in at " + login + " — not yet clocked out";
                } else if (!login.isEmpty()) {
                    todayStatus = "✅ Done for today — " + login + " → " + logout;
                }
            }
        }

        statusLabel.setText(todayStatus);
        totalHoursLabel.setText(String.format("%.1f hrs", totalHours));
    }

    private JButton makeActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    private void handleClockIn() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void handleClockOut() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private class AttendanceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            if (!sel) {
                String lateVal = (String) tableModel.getValueAt(row, 4);
                if (lateVal != null && lateVal.contains("min")) {
                    c.setBackground(LATE_COLOR);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(150, 180, 220);
            trackColor = Color.WHITE;
        }
        @Override protected JButton createDecreaseButton(int o) { return invisible(); }
        @Override protected JButton createIncreaseButton(int o) { return invisible(); }
        private JButton invisible() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}