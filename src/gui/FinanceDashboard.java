/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.EmployeeDAO;
import service.EmployeeService;
import model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.time.*;
import java.time.format.*;
import java.time.format.TextStyle;
import java.util.*;

/**
 *
 * @author anton
 * FinanceDashboard — Dashboard for Finance role.
 * RBAC: Finance can view all payslips and compute salary for any employee.
 * Cannot add, update, or delete employees.
 */
public class FinanceDashboard extends JFrame {

    private final EmployeeService employeeService;
    private final FileHandler     fileHandler;

    // ── Sidebar buttons ───────────────────────────────────────────────────────
    private JButton payslipBtn;
    private JButton logoutBtn;

    // ── Table ─────────────────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String[] HEADERS = {
        "Employee #", "Last Name", "First Name", "Position", "Basic Salary", "Hourly Rate"
    };

    public FinanceDashboard(String username) {
        employeeService = new EmployeeService(new EmployeeDAO());
        fileHandler     = new FileHandler();
        fileHandler.readEmployeeFile();
        fileHandler.readAttendanceFile();

        setTitle("MotorPH — Finance Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 500));

        add(buildSidebar(),       BorderLayout.WEST);
        add(buildContentPanel(),  BorderLayout.CENTER);

        payslipBtn.addActionListener(e -> refreshTable());
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        refreshTable();
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Sidebar
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.add(new JLabel(loadIcon("/assets/userprofile.png", 40, 40)), BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("Finance Staff");
        JLabel roleLabel = new JLabel("Finance");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.GRAY);
        namePanel.add(nameLabel);
        namePanel.add(roleLabel);
        profilePanel.add(namePanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        generalLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        payslipBtn = makeNavBtn("Payroll",   "Payslip Button.png");
        logoutBtn  = makeNavBtn("Log-out",   "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(Box.createVerticalStrut(20));
        navPanel.add(generalLabel);
        navPanel.add(payslipBtn);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel,     BorderLayout.CENTER);
        sidebar.add(logoutBtn,    BorderLayout.SOUTH);
        return sidebar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Content: employee list + compute button
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(29, 69, 143));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Payroll Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(HEADERS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 149, 237));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(new Color(20, 50, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 240), 1));

        // ── Action buttons ─────────────────────────────────────────────────────
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

        JButton computeBtn = makeActionBtn("💰 Compute Salary", new Color(56, 142, 60));
        JButton printBtn   = makeActionBtn("🖨 Print Payslip",  new Color(30, 144, 255));

        computeBtn.addActionListener(e -> openComputeDialog());
        printBtn.addActionListener(e   -> printPayslip());

        actionPanel.add(computeBtn);
        actionPanel.add(printBtn);

        panel.add(title,       BorderLayout.NORTH);
        panel.add(scrollPane,  BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Compute Salary Dialog
    // ════════════════════════════════════════════════════════════════════════
    private void openComputeDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String employeeId = tableModel.getValueAt(selectedRow, 0).toString();
        String empName    = tableModel.getValueAt(selectedRow, 2) + " " +
                            tableModel.getValueAt(selectedRow, 1);

        JDialog dialog = new JDialog(this, "Salary Computation — " + empName, true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // ── Month selector ────────────────────────────────────────────────────
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(Color.WHITE);

        JComboBox<String> monthBox = new JComboBox<>();
        monthBox.addItem("Select Month");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        Set<Month> availableMonths = new TreeSet<>();
        for (String[] record : fileHandler.getAttendanceData()) {
            if (record[0].equals(employeeId)) {
                try {
                    availableMonths.add(LocalDate.parse(record[1], formatter).getMonth());
                } catch (DateTimeParseException ignored) {}
            }
        }
        for (Month month : availableMonths)
            monthBox.addItem(month.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        JButton computeBtn = makeActionBtn("Compute", new Color(56, 142, 60));
        topPanel.add(new JLabel("Month:"));
        topPanel.add(monthBox);
        topPanel.add(computeBtn);

        // ── Result pane ───────────────────────────────────────────────────────
        JTextPane resultPane = new JTextPane();
        resultPane.setContentType("text/html");
        resultPane.setEditable(false);
        resultPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultPane.setBackground(Color.WHITE);
        JScrollPane resultScroll = new JScrollPane(resultPane);
        resultScroll.setBorder(new EmptyBorder(5, 10, 5, 10));

        computeBtn.addActionListener(e -> {
            String selectedMonth = (String) monthBox.getSelectedItem();
            if (selectedMonth == null || selectedMonth.equals("Select Month")) {
                JOptionPane.showMessageDialog(dialog, "Please select a month.");
                return;
            }
            computeAndDisplay(employeeId, selectedMonth, resultPane);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton closeBtn = makeActionBtn("Close", new Color(150, 150, 150));
        closeBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(closeBtn);

        dialog.add(topPanel,     BorderLayout.NORTH);
        dialog.add(resultScroll, BorderLayout.CENTER);
        dialog.add(btnPanel,     BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Salary computation logic
    // ════════════════════════════════════════════════════════════════════════
    private void computeAndDisplay(String employeeId, String selectedMonth, JTextPane pane) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        int totalWorkedMinutes = 0;

        for (String[] record : fileHandler.getAttendanceData()) {
            if (record[0].equals(employeeId)) {
                try {
                    LocalDate date = LocalDate.parse(record[1], formatter);
                    if (date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                            .equals(selectedMonth)) {
                        totalWorkedMinutes += calculateWorkedMinutes(record[2], record[3]);
                    }
                } catch (DateTimeParseException ignored) {}
            }
        }

        String[] emp = fileHandler.getEmployeeById(employeeId);
        if (emp == null) {
            JOptionPane.showMessageDialog(this, "Employee not found.");
            return;
        }

        try {
            double hourlyRate  = safeParseDouble(emp[18]);
            double basicSalary = safeParseDouble(emp[13]);
            double totalHours  = totalWorkedMinutes / 60.0;

            Benefits benefits = fileHandler.getBenefitsByEmployeeId(employeeId);
            double rice     = benefits.getRiceSubsidy();
            double phone    = benefits.getPhoneAllowance();
            double clothing = benefits.getClothingAllowance();

            PayrollLogic logic     = new PayrollLogic();
            double grossWeekly     = logic.calculateGrossWeeklySalary(hourlyRate, totalHours, rice, phone, clothing);

            Deductions deductions  = new Deductions();
            double sss             = deductions.calculateSSS(basicSalary);
            double ph              = deductions.calculatePhilHealth(basicSalary);
            double pi              = deductions.calculatePagIbig(basicSalary);
            double tax             = deductions.getMonthlyWithholdingTax(basicSalary);
            double weeklyDeductions = (sss + ph + pi + tax) / 4;
            double netMonthly      = grossWeekly - weeklyDeductions;
            double netWeekly       = netMonthly / 4;

            pane.setText(String.format("""
                    <html><body style='font-family:Segoe UI; font-size:11px; padding:10px;'>
                    <b style='font-size:14px;'>===== SALARY REPORT =====</b><br><br>
                    <b>BENEFITS:</b><br>
                    &nbsp;&nbsp;Rice Subsidy: &#8369;%,.2f<br>
                    &nbsp;&nbsp;Phone Allowance: &#8369;%,.2f<br>
                    &nbsp;&nbsp;Clothing Allowance: &#8369;%,.2f<br>
                    &nbsp;&nbsp;Total Benefits: &#8369;%,.2f<br><br>
                    <b>SALARY:</b><br>
                    &nbsp;&nbsp;Gross Monthly Salary (with benefits): &#8369;%,.2f<br><br>
                    <b>DEDUCTIONS (Monthly):</b><br>
                    &nbsp;&nbsp;SSS: &#8369;%,.2f<br>
                    &nbsp;&nbsp;PhilHealth: &#8369;%,.2f<br>
                    &nbsp;&nbsp;Pag-IBIG: &#8369;%,.2f<br>
                    &nbsp;&nbsp;Withholding Tax: &#8369;%,.2f<br>
                    &nbsp;&nbsp;Weekly Deduction Total: &#8369;%,.2f<br><br>
                    <b><span style='color:green; font-size:13px;'>NET MONTHLY SALARY: &#8369;%,.2f</span></b><br>
                    <b><span style='color:green; font-size:13px;'>NET WEEKLY SALARY: &#8369;%,.2f</span></b>
                    </body></html>
                    """,
                    rice, phone, clothing, rice + phone + clothing,
                    grossWeekly,
                    sss, ph, pi, tax, weeklyDeductions,
                    netMonthly, netWeekly));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Calculation error: " + ex.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Print Payslip
    // ════════════════════════════════════════════════════════════════════════
    private void printPayslip() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String empId = tableModel.getValueAt(selectedRow, 0).toString();
        String[] emp = fileHandler.getEmployeeById(empId);
        if (emp == null) return;

        java.util.Vector<Object> empVector = new java.util.Vector<>();
        for (String s : emp) empVector.add(s);

        double basicSalary = safeParseDouble(emp[13]);
        new PayslipFrame(empVector, basicSalary, 0.0, basicSalary);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════
    private void refreshTable() {
        tableModel.setRowCount(0);
        for (String[] emp : employeeService.getAllEmployees()) {
            if (emp.length >= 19) {
                tableModel.addRow(new Object[]{
                    emp[0], emp[1], emp[2], emp[11], emp[13], emp[18]
                });
            }
        }
    }

    private int calculateWorkedMinutes(String inStr, String outStr) {
        var formats = java.util.List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        try {
            inStr  = inStr.replaceAll("[^\\x20-\\x7E]", "").replace("\"", "").trim();
            outStr = outStr.replaceAll("[^\\x20-\\x7E]", "").replace("\"", "").trim();
            LocalTime in = null, out = null;
            for (DateTimeFormatter f : formats) {
                try { if (in  == null) in  = LocalTime.parse(inStr,  f); } catch (Exception ignored) {}
                try { if (out == null) out = LocalTime.parse(outStr, f); } catch (Exception ignored) {}
            }
            if (in == null || out == null) return 0;
            return Math.max((int) Duration.between(in, out).toMinutes() - 60, 0);
        } catch (Exception e) { return 0; }
    }

    private double safeParseDouble(String value) {
        try {
            return Double.parseDouble(value.replace("\"", "").replace(",", "").trim());
        } catch (NumberFormatException e) { return 0.0; }
    }

    private JButton makeNavBtn(String text, String icon) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + icon, 20, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
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

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(
                new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}
