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
import java.util.List;

/**
 * FinanceDashboard — Dashboard for Finance role.
 * Refactored to be 100% database-driven.
 */
public class FinanceDashboard extends JFrame {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;

    private JButton payslipBtn, logoutBtn;
    private JTable table;
    private DefaultTableModel tableModel;

    private static final String[] HEADERS = {
        "Employee #", "Last Name", "First Name", "Position", "Basic Salary", "Hourly Rate"
    };

    public FinanceDashboard(String username) {
        this.employeeService = new EmployeeService(new EmployeeDAO());
        this.attendanceService = new AttendanceService(new AttendanceDAO());

        setTitle("MotorPH — Finance Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 500));

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentPanel(), BorderLayout.CENTER);

        payslipBtn.addActionListener(e -> refreshTable());
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        refreshTable();
        setVisible(true);
    }

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
        namePanel.add(new JLabel("Finance Staff") {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); }});
        namePanel.add(new JLabel("Finance") {{ setFont(new Font("Segoe UI", Font.PLAIN, 12)); setForeground(Color.GRAY); }});
        profilePanel.add(namePanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        payslipBtn = makeNavBtn("Payroll", "Payslip Button.png");
        logoutBtn = makeNavBtn("Log-out", "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(payslipBtn);
        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(logoutBtn, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(29, 69, 143));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        tableModel = new DefaultTableModel(HEADERS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);

        JButton computeBtn = makeActionBtn("💰 Compute Salary", new Color(56, 142, 60));
        JButton printBtn = makeActionBtn("🖨 Print Payslip", new Color(30, 144, 255));

        computeBtn.addActionListener(e -> openComputeDialog());
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);
        actionPanel.add(computeBtn);
        actionPanel.add(printBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void openComputeDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee first.");
            return;
        }

        String employeeId = tableModel.getValueAt(selectedRow, 0).toString();
        // Database fetch via services
        String[] emp = employeeService.getEmployeeById(employeeId);
        List<String[]> attendance = attendanceService.getAttendanceByEmployee(employeeId);

        JOptionPane.showMessageDialog(this, "Computation logic connected to database. Loaded records: " + attendance.size());
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (String[] emp : employeeService.getAllEmployees()) {
            if (emp.length >= 19) {
                tableModel.addRow(new Object[]{emp[0], emp[1], emp[2], emp[11], emp[13], emp[18]});
            }
        }
    }

    private JButton makeNavBtn(String text, String icon) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + icon, 20, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        return (url == null) ? null : new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}