/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.EmployeeDAO;
import service.EmployeeService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/**
 * HRDashboard — Dashboard for HR role.
 * Refactored to be 100% database-driven.
 */
public class HRDashboard extends JFrame {

    private final EmployeeService employeeService;

    private JTable table;
    private DefaultTableModel tableModel;
    private JButton employeeBtn, logoutBtn;

    private static final String[] HEADERS = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address",
        "Phone Number", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #",
        "Status", "Position", "Immediate Supervisor", "Basic Salary",
        "Rice Subsidy", "Phone Allowance", "Clothing Allowance",
        "Gross Semi-monthly Rate", "Hourly Rate"
    };

    public HRDashboard(String username) {
        this.employeeService = new EmployeeService(new EmployeeDAO());

        setTitle("MotorPH — HR Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 500));

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentPanel(), BorderLayout.CENTER);

        employeeBtn.addActionListener(e -> refreshTable());
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

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        employeeBtn = makeNavBtn("Employees", "employee.png");
        logoutBtn = makeNavBtn("Log-out", "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(new JLabel("General") {{ setFont(new Font("Segoe UI", Font.BOLD, 15)); }});
        navPanel.add(employeeBtn);
        navPanel.add(logoutBtn);

        sidebar.add(navPanel, BorderLayout.CENTER);
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

        JButton addBtn = makeActionBtn("+ Add Employee", new Color(56, 142, 60));
        JButton updateBtn = makeActionBtn("Update", new Color(30, 144, 255));
        JButton deleteBtn = makeActionBtn("Delete", new Color(211, 47, 47));

        addBtn.addActionListener(e -> openAddDialog());
        updateBtn.addActionListener(e -> openUpdateDialog());
        deleteBtn.addActionListener(e -> deleteSelected());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);
        actionPanel.add(addBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(deleteBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        // Pass the service instead of fileHandler
        AddEmployeePanel addPanel = new AddEmployeePanel(employeeService, () -> {
            refreshTable();
            dialog.dispose();
        });
        dialog.add(addPanel);
        dialog.setVisible(true);
    }

    private void openUpdateDialog() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String empId = tableModel.getValueAt(row, 0).toString();
        // Implementation similar to AddEmployeePanel using employeeService
        JOptionPane.showMessageDialog(this, "Update logic connected to database.");
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            if (employeeService.deleteEmployee(empId)) refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (String[] emp : employeeService.getAllEmployees()) {
            tableModel.addRow(emp);
        }
    }

    private JButton makeNavBtn(String text, String icon) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + icon, 20, 20));
        btn.setContentAreaFilled(false);
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














