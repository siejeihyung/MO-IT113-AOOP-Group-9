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
 * Allows an employee to clock in/out and view their own attendance history.
 *
 * Features:
 *  - Clock In / Clock Out buttons
 *  - Shows today's status (clocked in / not yet / done)
 *  - Table with date, login, logout, minutes late, hours worked, deduction
 *  - Summary: total hours worked this month
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
    private JLabel clockInBtn;   // styled as button
    private JLabel clockOutBtn;

    // ── Colors ────────────────────────────────────────────────────────────────
    private final Color HEADER_COLOR  = new Color(20, 50, 110);
    private final Color ROW_ALT       = new Color(220, 230, 250);
    private final Color LATE_COLOR    = new Color(255, 200, 200);
    private final Color ON_TIME_COLOR = new Color(200, 240, 210);
    private final Color GREEN         = new Color(56, 142, 60);
    private final Color RED           = new Color(211, 47, 47);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // ════════════════════════════════════════════════════════════════════════
    public EmployeeAttendancePanel(String employeeId) {
        this.employeeId       = employeeId;
        this.attendanceService = new AttendanceService(new AttendanceDAO());
        this.employeeService   = new EmployeeService(new EmployeeDAO());

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        add(buildTopSection(),    BorderLayout.NORTH);
        add(buildTableSection(),  BorderLayout.CENTER);

        refreshAll();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Top section: Clock in/out + today's status + summary
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTopSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("My Attendance");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        // ── Today's status card ───────────────────────────────────────────────
        JPanel statusCard = new JPanel(new GridBagLayout());
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 10, 5, 10);
        gc.anchor = GridBagConstraints.CENTER;

        // Today label
        JLabel todayLabel = new JLabel("Today — " + LocalDate.now().toString());
        todayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        todayLabel.setForeground(new Color(29, 69, 143));
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        statusCard.add(todayLabel, gc);

        // Status text
        statusLabel = new JLabel("Checking...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gc.gridy = 1;
        statusCard.add(statusLabel, gc);

        // Clock In button
        gc.gridy = 2; gc.gridwidth = 1; gc.gridx = 0;
        JButton clockIn  = makeActionButton("🟢 Clock In",  GREEN);
        JButton clockOut = makeActionButton("🔴 Clock Out", RED);

        clockIn.addActionListener(e  -> handleClockIn());
        clockOut.addActionListener(e -> handleClockOut());

        statusCard.add(clockIn,  gc);
        gc.gridx = 1;
        statusCard.add(clockOut, gc);

        // ── Summary card ──────────────────────────────────────────────────────
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

        // ── Combine cards ─────────────────────────────────────────────────────
        JPanel cardsRow = new JPanel(new GridLayout(1, 2, 15, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(statusCard);
        cardsRow.add(summaryCard);

        panel.add(title,    BorderLayout.NORTH);
        panel.add(cardsRow, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Table section: attendance history
    // ════════════════════════════════════════════════════════════════════════
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

    // ════════════════════════════════════════════════════════════════════════
    //  Clock In
    // ════════════════════════════════════════════════════════════════════════
    private void handleClockIn() {
        String today = LocalDate.now().format(DATE_FMT);

        // Check if already clocked in today
        for (String[] row : attendanceService.getAttendanceForEmployee(employeeId)) {
            if (row[1].equals(today)) {
                JOptionPane.showMessageDialog(this,
                        "You have already clocked in today at " + row[2] + ".",
                        "Already Clocked In", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        boolean ok = attendanceService.clockIn(employeeId, "");
        if (ok) {
            String timeNow = LocalTime.now().format(TIME_FMT);
            int late = attendanceService.calculateLateMinutes(timeNow);
            String msg = late > 0
                    ? "Clocked in at " + timeNow + ".\nYou are " + late + " minute(s) late."
                    : "Clocked in at " + timeNow + ". You are on time! ✅";
            JOptionPane.showMessageDialog(this, msg, "Clock In", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to clock in. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Clock Out
    // ════════════════════════════════════════════════════════════════════════
    private void handleClockOut() {
        String today = LocalDate.now().format(DATE_FMT);
        boolean foundToday = false;

        for (String[] row : attendanceService.getAttendanceForEmployee(employeeId)) {
            if (row[1].equals(today)) {
                foundToday = true;
                if (!row[3].isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "You have already clocked out today at " + row[3] + ".",
                            "Already Clocked Out", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                break;
            }
        }

        if (!foundToday) {
            JOptionPane.showMessageDialog(this,
                    "You haven't clocked in today yet!",
                    "No Clock In Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = attendanceService.clockOut(employeeId);
        if (ok) {
            String timeNow = LocalTime.now().format(TIME_FMT);
            JOptionPane.showMessageDialog(this,
                    "Clocked out at " + timeNow + ". See you tomorrow! 👋",
                    "Clock Out", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to clock out. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Refresh table + status + summary
    // ════════════════════════════════════════════════════════════════════════
    private void refreshAll() {
        tableModel.setRowCount(0);

        // Get employee hourly rate for deduction calculation
        double hourlyRate = 0.0;
        String[] empData = employeeService.getEmployeeById(employeeId);
        if (empData != null && empData.length > 18) {
            try { hourlyRate = Double.parseDouble(
                    empData[18].replace(",", "").replace("\"", "").trim());
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

            // Hours worked
            double hours = 0.0;
            if (!login.isEmpty() && !logout.isEmpty()) {
                try {
                    LocalTime in  = LocalTime.parse(login,  TIME_FMT);
                    LocalTime out = LocalTime.parse(logout, TIME_FMT);
                    hours = java.time.Duration.between(in, out).toMinutes() / 60.0;
                    totalHours += hours;
                } catch (Exception ignored) {}
            }

            // Late minutes + deduction
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

            // Update today's status card
            if (date.equals(today)) {
                if (!login.isEmpty() && logout.isEmpty()) {
                    todayStatus = "⏱ Clocked in at " + login + " — not yet clocked out";
                } else if (!login.isEmpty()) {
                    todayStatus = "✅ Done for today — " + login + " → " + logout;
                }
            }
        }

        // Update labels
        statusLabel.setText(todayStatus);
        totalHoursLabel.setText(String.format("%.1f hrs", totalHours));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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

    // ── Cell renderer: red rows for late, green for on time ──────────────────
    private class AttendanceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            if (!sel) {
                String lateVal = (String) tableModel.getValueAt(row, 4);
                if (lateVal != null && lateVal.contains("min")) {
                    c.setBackground(LATE_COLOR);    // late → light red
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                }
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    // ── Scrollbar ─────────────────────────────────────────────────────────────
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
