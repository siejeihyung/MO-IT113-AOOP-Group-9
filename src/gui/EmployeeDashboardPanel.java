/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.*;
import service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.Vector;
import model.Deductions;

/**
 * EmployeeDashboardPanel — Limited dashboard for regular employees.
 * RBAC: Employees only see their own data.
 *  - My Profile
 *  - My Attendance (clock in/out)
 *  - My Leave (balance + file leave + history)
 *  - My Payslip
 *  - IT Support (submit + view tickets)
 */
public class EmployeeDashboardPanel extends JFrame {

    private final String          employeeId;
    private final EmployeeService employeeService;
    private final LeaveService    leaveService;
    private final Deductions      deductionsService = new Deductions();

    private final CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel;

    // ── Sidebar buttons ───────────────────────────────────────────────────────
    private JButton myInfoBtn;
    private JButton myAttendanceBtn;
    private JButton myLeaveBtn;
    private JButton myPayslipBtn;
    private JButton logoutBtn;

    public EmployeeDashboardPanel(String employeeId) {
        this.employeeId  = employeeId;
        employeeService  = new EmployeeService(new EmployeeDAO());
        leaveService     = new LeaveService(new LeaveDAO());

        String[] empData = employeeService.getEmployeeById(employeeId);
        String name = empData != null && empData.length > 2
                ? empData[2] + " " + empData[1] : employeeId;

        setTitle("MotorPH — " + name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Build panels ──────────────────────────────────────────────────────
        JPanel myInfoPanel                       = buildMyInfoPanel(empData);
        EmployeeAttendancePanel myAttendancePanel = new EmployeeAttendancePanel(employeeId);
        JPanel myLeavePanel                      = buildMyLeavePanel();

        // ── Content area ──────────────────────────────────────────────────────
        contentPanel = new JPanel(cardLayout) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(29, 69, 143));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        myAttendancePanel.setOpaque(false);

        contentPanel.add(myInfoPanel,         "MyInfo");
        contentPanel.add(myAttendancePanel,   "MyAttendance");
        contentPanel.add(myLeavePanel,        "MyLeave");

        // ── Build sidebar ─────────────────────────────────────────────────────
        JPanel sidebar = buildSidebar(name);

        // ── Wire buttons ──────────────────────────────────────────────────────
        myInfoBtn.addActionListener(e       -> cardLayout.show(contentPanel, "MyInfo"));
        myAttendanceBtn.addActionListener(e -> cardLayout.show(contentPanel, "MyAttendance"));
        myLeaveBtn.addActionListener(e      -> cardLayout.show(contentPanel, "MyLeave"));
        myPayslipBtn.addActionListener(e    -> openMyPayslip());
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        add(sidebar,      BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "MyInfo");
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Sidebar
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar(String name) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Profile
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.add(new JLabel(loadIcon("/assets/userprofile.png", 40, 40)), BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(name);
        JLabel roleLabel = new JLabel("Employee");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.GRAY);
        namePanel.add(nameLabel);
        namePanel.add(roleLabel);
        profilePanel.add(namePanel, BorderLayout.CENTER);

        // Nav
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel myDataLabel = new JLabel("My Data");
        myDataLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        myDataLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        myDataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── All 5 nav buttons ─────────────────────────────────────────────────
        myInfoBtn       = makeNavBtn("My Profile",    "employee.png");
        myAttendanceBtn = makeNavBtn("My Attendance", "attendance.png");
        myLeaveBtn      = makeNavBtn("My Leave",      "leave.png");
        myPayslipBtn    = makeNavBtn("My Payslip",    "Payslip Button.png");
        logoutBtn       = makeNavBtn("Log-out",       "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(Box.createVerticalStrut(20));
        navPanel.add(myDataLabel);
        navPanel.add(myInfoBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(myAttendanceBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(myLeaveBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(myPayslipBtn);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel,     BorderLayout.CENTER);
        sidebar.add(logoutBtn,    BorderLayout.SOUTH);
        return sidebar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  My Profile panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMyInfoPanel(String[] empData) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = new JPanel(new GridLayout(0, 2, 10, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        String[] labels  = {"Employee ID", "Last Name", "First Name", "Birthday",
                            "Address", "Phone", "SSS #", "PhilHealth #",
                            "TIN #", "Pag-IBIG #", "Status", "Position",
                            "Supervisor", "Basic Salary", "Hourly Rate"};
        int[]    indices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 18};

        for (int i = 0; i < labels.length; i++) {
            JLabel lbl = new JLabel(labels[i] + ":");
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String val = (empData != null && indices[i] < empData.length)
                    ? empData[indices[i]] : "—";
            JLabel valLbl = new JLabel(val);
            valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            card.add(lbl);
            card.add(valLbl);
        }

        wrapper.add(card);
        return wrapper;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  My Leave panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMyLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Leave");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        // ── Balance cards ─────────────────────────────────────────────────────
        JPanel balanceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        balanceRow.setOpaque(false);
        balanceRow.add(makeBalanceCard("Sick Leave",
                leaveService.getRemainingBalance(employeeId, "Sick"), 5));
        balanceRow.add(makeBalanceCard("Vacation Leave",
                leaveService.getRemainingBalance(employeeId, "Vacation"), 10));
        balanceRow.add(makeBalanceCard("Emergency Leave",
                leaveService.getRemainingBalance(employeeId, "Emergency"), 3));

        // ── File Leave button ─────────────────────────────────────────────────
        JButton fileLeaveBtn = makeActionButton("+ File Leave", new Color(56, 142, 60));
        fileLeaveBtn.addActionListener(e -> openFileLeaveDialog());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnRow.setOpaque(false);
        btnRow.add(fileLeaveBtn);

        // ── Leave history table ───────────────────────────────────────────────
        String[] cols = {"Leave ID", "Date", "Type", "Days", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : leaveService.getLeavesForEmployee(employeeId)) {
            model.addRow(new Object[]{row[1], row[2], row[3], row[4], row[5]});
        }

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(20, 50, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true));

        JPanel topArea = new JPanel(new BorderLayout(0, 8));
        topArea.setOpaque(false);
        topArea.add(title,      BorderLayout.NORTH);
        topArea.add(balanceRow, BorderLayout.CENTER);
        topArea.add(btnRow,     BorderLayout.SOUTH);

        panel.add(topArea, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  File Leave Dialog
    // ════════════════════════════════════════════════════════════════════════
    private void openFileLeaveDialog() {
        JDialog dialog = new JDialog(this, "File Leave Request", true);
        dialog.setSize(400, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField dateField  = new JTextField("YYYY-MM-DD", 15);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Sick", "Vacation", "Emergency"});
        JSpinner daysSpinner  = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));

        Object[][] rows = {
            {"Start Date *",     dateField},
            {"Leave Type *",     typeBox},
            {"Number of Days *", daysSpinner}
        };
        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            form.add(new JLabel((String) rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 1;
            form.add((java.awt.Component) rows[i][1], gc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton submitBtn = makeActionButton("Submit", new Color(33, 150, 243));
        JButton cancelBtn = makeActionButton("Cancel", new Color(150, 150, 150));
        cancelBtn.addActionListener(e -> dialog.dispose());

        submitBtn.addActionListener(e -> {
            String dateStr = dateField.getText().trim();
            String type    = (String) typeBox.getSelectedItem();
            int    days    = (int) daysSpinner.getValue();

            if (dateStr.isEmpty() || dateStr.equals("YYYY-MM-DD")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid date.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                int balance = leaveService.getRemainingBalance(employeeId, type);
                if (days > balance) {
                    JOptionPane.showMessageDialog(dialog,
                            "Not enough " + type + " leave balance.\nRemaining: " + balance + " day(s).",
                            "Insufficient Balance", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                boolean saved = leaveService.fileLeave(employeeId, date, type, days);
                if (saved) {
                    JOptionPane.showMessageDialog(dialog, "Leave request filed successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to save. Date cannot be in the past.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format. Use YYYY-MM-DD.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(submitBtn);
        btnPanel.add(cancelBtn);
        dialog.add(form,     BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  My Payslip
    // ════════════════════════════════════════════════════════════════════════
    private void openMyPayslip() {
        String[] empData = employeeService.getEmployeeById(employeeId);
        if (empData != null) {
            Vector<Object> dataVector = new Vector<>();
            for (String s : empData) dataVector.add(s);
            try {
                java.util.function.Function<String, Double> parse = (val) -> {
                    if (val == null || val.trim().isEmpty()) return 0.0;
                    return Double.parseDouble(val.replace(",", "").replace("\"", "").trim());
                };
                double basicSalary = parse.apply(empData[13]);
                double rice        = parse.apply(empData[14]);
                double phone       = parse.apply(empData[15]);
                double clothing    = parse.apply(empData[16]);
                double gross       = basicSalary + rice + phone + clothing;
                double totalDeductions = deductionsService.getTotalDeductions(basicSalary, 0);
                double netPay      = gross - totalDeductions;
                new PayslipFrame(dataVector, gross, totalDeductions, netPay);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Calculation Error: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Employee record not found.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JPanel makeBalanceCard(String type, int remaining, int total) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel typeLbl = new JLabel(type);
        typeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        typeLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel daysLbl = new JLabel(remaining + " / " + total + " days");
        daysLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        daysLbl.setForeground(remaining > 0 ? new Color(56, 142, 60) : Color.RED);
        daysLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("remaining");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(Color.GRAY);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        card.add(typeLbl);
        card.add(daysLbl);
        card.add(subLbl);
        return card;
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
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private JButton makeActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 34));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(
                new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}