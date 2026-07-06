/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.*;
import service.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import model.Ticket;
import java.net.URL; 

public class AdminDashboard extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel;
    private final EmployeePanel employeePanel = new EmployeePanel(); 
    private DefaultTableModel ticketTableModel;

    private JButton employeeBtn, leaveBtn, attendanceBtn, printPayslipBtn, ticketsBtn, logoutBtn;

    public AdminDashboard(String username) {
        setTitle("MotorPH — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 500));

        LeaveService leaveService = new LeaveService(new LeaveDAO());
        AttendanceService attendanceService = new AttendanceService(new AttendanceDAO());
        
        LeavePanel leavePanel = new LeavePanel(leaveService);
        AttendancePanel attendancePanel = new AttendancePanel(attendanceService);
        JPanel ticketsPanel = buildTicketsPanel();

        contentPanel = new JPanel(cardLayout) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(29, 69, 143));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // Initialize and setup panels
        employeePanel.setOpaque(false);
        leavePanel.setOpaque(false);
        attendancePanel.setOpaque(false);

        contentPanel.add(employeePanel, "Employee");
        contentPanel.add(leavePanel, "Leave");
        contentPanel.add(attendancePanel, "Attendance");
        contentPanel.add(ticketsPanel, "Tickets");

        // Now trigger the load
        employeePanel.refreshTable();

        JPanel sidebar = buildSidebar();

        employeeBtn.addActionListener(e -> cardLayout.show(contentPanel, "Employee"));
        leaveBtn.addActionListener(e -> cardLayout.show(contentPanel, "Leave"));
        attendanceBtn.addActionListener(e -> cardLayout.show(contentPanel, "Attendance"));
        
        printPayslipBtn.addActionListener( e -> {
        java.util.List<reports.PayslipModel> testList = new java.util.ArrayList<>();
        testList.add(new reports.PayslipModel("PS-001", "Test Employee", 0.0));
        reports.ReportGenerator.generatePayslip(testList);
    });
        
        ticketsBtn.addActionListener(e -> {
            refreshTicketTable();
            cardLayout.show(contentPanel, "Tickets");
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);

        employeeBtn = makeNavBtn("Employee", "employee.png");
        leaveBtn = makeNavBtn("Leave Management", "leave.png");
        attendanceBtn = makeNavBtn("Attendance", "attendance.png");
        printPayslipBtn = makeNavBtn("Print Payslip", "Payslip Button.png");
        ticketsBtn = makeNavBtn("IT Tickets", "IT Support.png");
        logoutBtn = makeNavBtn("Log-out", "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(employeeBtn);
        navPanel.add(leaveBtn);
        navPanel.add(attendanceBtn);
        navPanel.add(printPayslipBtn);
        navPanel.add(ticketsBtn);
        navPanel.add(logoutBtn);

        sidebar.add(navPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] headers = {"Ticket ID", "Sender", "Subject", "Priority", "Status"};
        ticketTableModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(ticketTableModel);
        
        JButton updateBtn = makeActionBtn("✅ Update Status", new Color(56, 142, 60));
        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String ticketID = ticketTableModel.getValueAt(row, 0).toString();
                String[] statuses = {"Open", "In Progress", "Resolved", "Closed"};
                String newStatus = (String) JOptionPane.showInputDialog(this, "Select status:", "Update", 
                                   JOptionPane.QUESTION_MESSAGE, null, statuses, ticketTableModel.getValueAt(row, 4));
                if (newStatus != null) {
                    TicketDAO dao = new TicketDAO();
                    if (dao.updateTicketStatus(ticketID, newStatus)) refreshTicketTable();
                }
            }
        });

        panel.add(new JLabel("IT Support Tickets") {{ setForeground(Color.WHITE); }}, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(updateBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshTicketTable() {
        ticketTableModel.setRowCount(0);
        TicketDAO ticketDAO = new TicketDAO();
        for (Ticket t : ticketDAO.getAllTickets()) {
            ticketTableModel.addRow(new Object[]{
                t.getTicketID(), t.getEmployeeID(), t.getSubject(), t.getPriority(), t.getStatus()
            });
        }
    }

    private JButton makeNavBtn(String text, String iconFile) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + iconFile, 20, 20));
        btn.setContentAreaFilled(false);
        return btn;
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        return (url == null) ? null : new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}