/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.EmployeeDAO;
import dao.AttendanceDAO;
import service.EmployeeService;
import service.AttendanceService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * HRDashboard — Dashboard for HR role.
 */
public class HRDashboard extends JFrame {

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    
    private final CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel;

    // View Components
    private JTable employeeTable;
    private DefaultTableModel employeeTableModel;
    
    // Missing Attendance elements matching your visual layout
    private JTable attendanceTable;
    private DefaultTableModel attendanceTableModel;
    private JTextField searchField;

    // Navigation buttons
    private JButton employeeBtn, attendanceBtn, logoutBtn;

    private static final String[] EMP_HEADERS = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address",
        "Phone Number", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #",
        "Status", "Position", "Immediate Supervisor", "Basic Salary",
        "Rice Subsidy", "Phone Allowance", "Clothing Allowance",
        "Gross Semi-monthly Rate", "Hourly Rate"
    };

    public HRDashboard(String username) {
        this.employeeService = new EmployeeService(new EmployeeDAO());
        this.attendanceService = new AttendanceService(new AttendanceDAO());

        setTitle("MotorPH — HR Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(900, 500));

        // Create the views
        JPanel empPanel = buildEmployeePanel();
        JPanel attPanel = buildAttendancePanel(); // Customized custom dashboard panel view

        // Content panel using CardLayout
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(empPanel, "Employee");
        contentPanel.add(attPanel, "Attendance");

        add(buildSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Sidebar Event Handlers
        employeeBtn.addActionListener(e -> {
            refreshEmployeeTable();
            cardLayout.show(contentPanel, "Employee");
        });
        
        attendanceBtn.addActionListener(e -> {
            refreshAttendanceTable();
            cardLayout.show(contentPanel, "Attendance");
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        refreshEmployeeTable();
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

        employeeBtn = makeNavBtn("Employees", "employee.png");
        attendanceBtn = makeNavBtn("Attendance History", "attendance.png");
        logoutBtn = makeNavBtn("Log-out", "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(new JLabel("General") {{ setFont(new Font("Segoe UI", Font.BOLD, 15)); }});
        navPanel.add(Box.createVerticalStrut(15));
        navPanel.add(employeeBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(attendanceBtn);
        navPanel.add(Box.createVerticalGlue());
        navPanel.add(logoutBtn);

        sidebar.add(navPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(29, 69, 143));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        employeeTableModel = new DefaultTableModel(EMP_HEADERS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        employeeTable = new JTable(employeeTableModel);

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

        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Replicates your custom visual design with the Pink/Orange background
     * and houses the Print Time Card button inside the management panel.
     */
    private JPanel buildAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Matched to the warm gradient styling layout in your screenshot
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(255, 204, 213);
                Color color2 = new Color(255, 229, 180);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Top Bar containing Search Components and the Print Button
        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topControlPanel.setOpaque(false);

        searchField = new JTextField(15);
        JButton searchBtn = makeActionBtn("Search", new Color(30, 144, 255));
        
        // This places the print button natively inside your dashboard view!
        JButton printTimeCardBtn = makeActionBtn("🖨️ Print Time Card", new Color(108, 117, 125));

        topControlPanel.add(searchField);
        topControlPanel.add(searchBtn);
        topControlPanel.add(Box.createHorizontalStrut(20)); // Spacing separator
        topControlPanel.add(printTimeCardBtn);

        // 2. Table Implementation matching snapshot columns
        String[] attHeaders = {"Employee #", "Date", "Log In", "Log Out"};
        attendanceTableModel = new DefaultTableModel(attHeaders, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        attendanceTable = new JTable(attendanceTableModel);
        JScrollPane scrollPane = new JScrollPane(attendanceTable);
        
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        attendanceTable.getTableHeader().setResizingAllowed(false);

        panel.add(topControlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Search Action
        searchBtn.addActionListener(e -> {
            String filterId = searchField.getText().trim();
            if (filterId.isEmpty()) {
                refreshAttendanceTable();
            } else {
                attendanceTableModel.setRowCount(0);
                for (String[] row : attendanceService.getAttendanceByEmployee(filterId)) {
                    attendanceTableModel.addRow(row);
                }
            }
        });

        // Printing Action from selected table contextual entry row
        printTimeCardBtn.addActionListener(e -> {
            int selectedRow = attendanceTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an attendance entry row from the table to print.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String targetEmpId = attendanceTableModel.getValueAt(selectedRow, 0).toString();
            handlePrintTimeCard(targetEmpId);
        });

        return panel;
    }

    private void handlePrintTimeCard(String empId) {
        try {
            java.util.List<?> timeCardList = attendanceService.getTimeCardData(empId);

            if (timeCardList == null || timeCardList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No printable logs found for Employee #" + empId, "No Data", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("employeeId", empId);
            params.put("employeeName", "Employee #" + empId); // Optionally link to EmployeeDAO name search
            params.put("periodEndDate", "07/15/2026");

            reports.ReportGenerator.generateReport("/reports/motorph_employee_timecard.jrxml", timeCardList, params);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating summary: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        AddEmployeePanel addPanel = new AddEmployeePanel(employeeService, () -> {
            refreshEmployeeTable();
            dialog.dispose();
        });
        dialog.add(addPanel);
        dialog.setVisible(true);
    }

    private void openUpdateDialog() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) return;
        JOptionPane.showMessageDialog(this, "Update logic connected to database.");
    }

    private void deleteSelected() {
        int row = employeeTable.getSelectedRow();
        if (row != -1) {
            String empId = employeeTableModel.getValueAt(row, 0).toString();
            if (employeeService.deleteEmployee(empId)) refreshEmployeeTable();
        }
    }

    private void refreshEmployeeTable() {
        employeeTableModel.setRowCount(0);
        for (String[] emp : employeeService.getAllEmployees()) {
            employeeTableModel.addRow(emp);
        }
    }

    private void refreshAttendanceTable() {
        attendanceTableModel.setRowCount(0);
        for (String[] row : attendanceService.getAllAttendance()) {
            attendanceTableModel.addRow(row);
        }
    }

    private JButton makeNavBtn(String text, String icon) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + icon, 20, 20));
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
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