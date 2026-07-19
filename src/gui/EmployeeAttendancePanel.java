/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import service.AttendanceService;
import service.EmployeeService;
import dao.AttendanceDAO;
import dao.EmployeeDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * EmployeeAttendancePanel — shown inside EmployeeDashboardPanel.
 * Allows an employee to clock in/out and view their own attendance history cleanly.
 */
public class EmployeeAttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private final EmployeeService   employeeService;
    private final String            employeeId;

    // ── Table Components ──────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable table;

    // ── Status elements ───────────────────────────────────────────────────────
    private JLabel statusLabel;
    private JLabel totalHoursLabel;

    // ── Theme Design Palette ──────────────────────────────────────────────────
    private final Color HEADER_COLOR      = new Color(20, 50, 110);
    private final Color ROW_ALT           = new Color(248, 250, 252); // Ultra-soft slate alternate
    private final Color LATE_COLOR        = new Color(254, 242, 242); // Soft pastel crimson warning
    private final Color LATE_TEXT_COLOR   = new Color(185, 28, 28);   // Sharp red alert text
    private final Color GREEN             = new Color(56, 142, 60);
    private final Color RED               = new Color(211, 47, 47);
    private final Color BORDER_COLOR      = new Color(229, 231, 235); // Modern crisp border lines

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm[:ss]");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public EmployeeAttendancePanel(String employeeId) {
        this.employeeId        = employeeId;
        this.attendanceService = new AttendanceService(new AttendanceDAO());
        this.employeeService   = new EmployeeService(new EmployeeDAO());

        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(24, 24, 24, 24));
        setOpaque(false);

        add(buildTopSection(),    BorderLayout.NORTH);
        add(buildTableSection(),   BorderLayout.CENTER);

        refreshAll();
    }

    private JPanel buildTopSection() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        JLabel title = new JLabel("My Attendance");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(31, 41, 55)); // High contrast dark charcoal title text

        JPanel statusCard = new JPanel(new GridBagLayout());
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(16, 20, 16, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 10, 6, 10);
        gc.anchor = GridBagConstraints.CENTER;

        JLabel todayLabel = new JLabel("Today — " + LocalDate.now().toString());
        todayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        todayLabel.setForeground(HEADER_COLOR);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        statusCard.add(todayLabel, gc);

        statusLabel = new JLabel("Checking status...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(107, 114, 128));
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
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel summaryTitle = new JLabel("THIS MONTH");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        summaryTitle.setForeground(new Color(156, 163, 175));
        summaryTitle.setAlignmentX(CENTER_ALIGNMENT);

        totalHoursLabel = new JLabel("0.0 hrs");
        totalHoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        totalHoursLabel.setForeground(new Color(17, 24, 39));
        totalHoursLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hoursSubLabel = new JLabel("Total Hours Worked");
        hoursSubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hoursSubLabel.setForeground(new Color(107, 114, 128));
        hoursSubLabel.setAlignmentX(CENTER_ALIGNMENT);

        summaryCard.add(summaryTitle);
        summaryCard.add(Box.createVerticalStrut(8));
        summaryCard.add(totalHoursLabel);
        summaryCard.add(Box.createVerticalStrut(4));
        summaryCard.add(hoursSubLabel);

        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(statusCard);
        cardsRow.add(summaryCard);

        panel.add(title,    BorderLayout.NORTH);
        panel.add(cardsRow, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel historyTitle = new JLabel("Attendance History Logs");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTitle.setForeground(new Color(75, 85, 99));

        String[] cols = {"Date", "Clock In", "Clock Out", "Hours Worked", "Minutes Late", "Deduction"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(32); // Enhanced vertical breathing room
        table.setFillsViewportHeight(true);
        table.setShowGrid(true); // Enable clean structural lines
        table.setGridColor(BORDER_COLOR);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(239, 246, 255)); // Soft modern blue highlight selection
        table.setSelectionForeground(new Color(30, 64, 175));
        table.setDefaultRenderer(Object.class, new AttendanceCellRenderer());

        // Header Customization Layout Styling
        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(1, 1, 1, 1)
        ));
        tableCard.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(scrollPane,             BorderLayout.CENTER);

        panel.add(historyTitle, BorderLayout.NORTH);
        panel.add(tableCard,    BorderLayout.CENTER);
        return panel;
    }

    private void refreshAll() {
        tableModel.setRowCount(0);
        double hourlyRate = 0.0;
        
        String[] empData = employeeService.getEmployeeById(employeeId);
        if (empData != null && empData.length >= 19) {
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
                deduction > 0 ? String.format("₱%,.2f", deduction) : "₱0.00"
            });

            if (date.equals(today)) {
                if (!login.isEmpty() && logout.isEmpty()) {
                    todayStatus = "⏱ Clocked in at " + login + " — pending departure shift execution.";
                } else if (!login.isEmpty()) {
                    todayStatus = "✅ Attendance logged completely for today: " + login + " → " + logout;
                }
            }
        }

        statusLabel.setText(todayStatus);
        totalHoursLabel.setText(String.format("%.1f hrs", totalHours));
    }

    private JButton makeActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(135, 38));
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
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    private void handleClockIn() {
        throw new UnsupportedOperationException("Clock In persistence handling not configured yet."); 
    }

    private void handleClockOut() {
        throw new UnsupportedOperationException("Clock Out persistence handling not configured yet."); 
    }

    // ── Modern Table Cell Renderer ──────────────────────────────────────────
    private class AttendanceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            
            Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); // Horizontal element text padding margins

            // 1. Column Text Alignment Logic
            if (col == 0) {
                setHorizontalAlignment(SwingConstants.LEFT);   // Left align dates
            } else if (col == 5) {
                setHorizontalAlignment(SwingConstants.RIGHT);  // Right align cash figures
            } else {
                setHorizontalAlignment(SwingConstants.CENTER); // Center everything else
            }

            // 2. Row Background Color Status Layout Logic
            if (!sel) {
                String lateVal = (String) tableModel.getValueAt(row, 4);
                if (lateVal != null && lateVal.contains("min")) {
                    c.setBackground(LATE_COLOR);
                    c.setForeground(LATE_TEXT_COLOR);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    c.setForeground(new Color(55, 65, 81)); // Clean readability charcoal text
                }
            }
            return c;
        }
    }

    // ── ScrollBar Layout Skin Scrubber ───────────────────────────────────────
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(209, 213, 219);
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