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
import java.awt.event.*;
import java.net.URL;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import model.Ticket;

/**
 * AdminDashboard — Full-access dashboard for HR Admin.
 * RBAC: Admin sees Employee, Leave Management, Attendance, Payslip.
 */
public class AdminDashboard extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel;
    private final EmployeePanel employeePanel = new EmployeePanel();

    // ── IT Tickets table ──────────────────────────────────────────────────────
    private DefaultTableModel ticketTableModel;

    // ── Buttons ───────────────────────────────────────────────────────────────
    private JButton employeeBtn;
    private JButton leaveBtn;
    private JButton attendanceBtn;
    private JButton printPayslipBtn;
    private JButton ticketsBtn;
    private JButton logoutBtn;

    public AdminDashboard(String username) {
        setTitle("MotorPH — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 500));

        LeaveService    leaveService    = new LeaveService(new LeaveDAO());
        LeavePanel      leavePanel      = new LeavePanel(leaveService);
        AttendancePanel attendancePanel = new AttendancePanel(new model.FileHandler());
        JPanel          ticketsPanel   = buildTicketsPanel();

        contentPanel = new JPanel(cardLayout) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(29, 69, 143));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        employeePanel.setOpaque(false);
        leavePanel.setOpaque(false);
        attendancePanel.setOpaque(false);

        contentPanel.add(employeePanel,   "Employee");
        contentPanel.add(leavePanel,      "Leave");
        contentPanel.add(attendancePanel, "Attendance");
        contentPanel.add(ticketsPanel,    "Tickets");

        JPanel sidebar = buildSidebar();

        // ── Wire buttons ──────────────────────────────────────────────────────
        employeeBtn.addActionListener(e   -> cardLayout.show(contentPanel, "Employee"));
        leaveBtn.addActionListener(e      -> cardLayout.show(contentPanel, "Leave"));
        attendanceBtn.addActionListener(e -> cardLayout.show(contentPanel, "Attendance"));
        ticketsBtn.addActionListener(e    -> {
            refreshTicketTable();
            cardLayout.show(contentPanel, "Tickets");
        });

        printPayslipBtn.addActionListener(e -> {
            EmployeeTable table = (EmployeeTable) employeePanel.getDashboardTable();
            Vector<Object> selected = table.getSelectedEmployeeFullDetails();
            if (selected != null) {
                double gross = parseMoney(selected.get(13));
                new PayslipFrame(selected, gross, 0.0, gross);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an employee first!");
            }
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        add(sidebar,      BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  IT Tickets panel for Admin (view all + update status)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTicketsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("IT Support Tickets");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        String[] headers = {"Ticket ID", "Sender", "Category", "Subject", "Status"};
        ticketTableModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(ticketTableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 149, 237));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(new Color(20, 50, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 240), 1));
        scroll.getViewport().setBackground(Color.WHITE);

        // ── Action buttons ─────────────────────────────────────────────────────
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setOpaque(false);

       JButton submitBtn  = makeActionBtn("+ Submit Ticket",  new Color(33, 150, 243));
       JButton updateBtn  = makeActionBtn("✅ Update Status", new Color(56, 142, 60));
       JButton refreshBtn = makeActionBtn("↻ Refresh",        new Color(30, 144, 255));

        updateBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a ticket first.");
                return;
            }
            String ticketID = ticketTableModel.getValueAt(row, 0).toString();
            String[] statuses = {"Open", "In Progress", "Resolved", "Closed"};
            String newStatus = (String) JOptionPane.showInputDialog(this,
                    "Select new status:", "Update Ticket",
                    JOptionPane.QUESTION_MESSAGE, null, statuses,
                    ticketTableModel.getValueAt(row, 4));
            if (newStatus != null) {
                dao.TicketDAO dao = new dao.TicketDAO();
                if (dao.updateTicketStatus(ticketID, newStatus)) {
                    refreshTicketTable();
                    JOptionPane.showMessageDialog(this, "Status updated successfully.");
                }
            }
        });

        submitBtn.addActionListener(e -> openSubmitTicketDialog());
        refreshBtn.addActionListener(e -> refreshTicketTable());

        actionPanel.add(submitBtn);
        actionPanel.add(updateBtn);
        actionPanel.add(refreshBtn);

        panel.add(title,       BorderLayout.NORTH);
        panel.add(scroll,      BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshTicketTable() {
        ticketTableModel.setRowCount(0);
        dao.TicketDAO ticketDAO = new dao.TicketDAO();
        for (Ticket t : ticketDAO.getAllTickets()) {
            ticketTableModel.addRow(new Object[]{
                t.getTicketID(), t.getSenderName(),
                t.getCategory(), t.getSubject(), t.getStatus()
            });
        }
    }
    
    private void openSubmitTicketDialog() {
    JDialog dialog = new JDialog(this, "Submit IT Support Ticket", true);
    dialog.setSize(450, 320);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout());

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Color.WHITE);
    form.setBorder(new EmptyBorder(20, 25, 10, 25));
    GridBagConstraints gc = new GridBagConstraints();
    gc.insets = new Insets(8, 6, 8, 6);
    gc.anchor = GridBagConstraints.WEST;
    gc.fill   = GridBagConstraints.HORIZONTAL;

    JTextField senderField    = new JTextField(20);
    JComboBox<String> typeBox = new JComboBox<>(new String[]{
        "Hardware", "Software", "Network", "Account Access", "Other"
    });
    JTextField subjectField   = new JTextField(20);
    JTextArea  descArea       = new JTextArea(3, 20);
    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);

    Object[][] rows = {
        {"Reported By *",  senderField},
        {"Category *",     typeBox},
        {"Subject *",      subjectField},
        {"Description",    new JScrollPane(descArea)}
    };
    for (int i = 0; i < rows.length; i++) {
        gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
        form.add(new JLabel((String) rows[i][0]), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add((Component) rows[i][1], gc);
    }

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.setBackground(Color.WHITE);
    JButton submitBtn = makeActionBtn("Submit",  new Color(33, 150, 243));
    JButton cancelBtn = makeActionBtn("Cancel",  new Color(150, 150, 150));
    cancelBtn.addActionListener(e -> dialog.dispose());

    submitBtn.addActionListener(e -> {
        String sender      = senderField.getText().trim();
        String category    = (String) typeBox.getSelectedItem();
        String subject     = subjectField.getText().trim();
        String description = descArea.getText().trim();

        if (sender.isEmpty() || subject.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Reported By and Subject are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        dao.TicketDAO dao = new dao.TicketDAO();
        if (dao.submitTicket(sender, category, subject, description)) {
            refreshTicketTable();
            JOptionPane.showMessageDialog(dialog, "Ticket submitted successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog, "Failed to submit ticket.",
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
    //  Sidebar
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.add(new JLabel(loadIcon("/assets/userprofile.png", 40, 40)), BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("Admin");
        JLabel roleLabel = new JLabel("HR Manager");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

        employeeBtn     = makeNavBtn("Employee",         "employee.png");
        leaveBtn        = makeNavBtn("Leave Management", "leave.png");
        attendanceBtn   = makeNavBtn("Attendance",       "attendance.png");
        printPayslipBtn = makeNavBtn("Print Payslip",    "Payslip Button.png");
        ticketsBtn      = makeNavBtn("IT Tickets",       "IT Support.png");
        logoutBtn       = makeNavBtn("Log-out",          "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(Box.createVerticalStrut(20));
        navPanel.add(generalLabel);
        navPanel.add(employeeBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(leaveBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(attendanceBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(printPayslipBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(ticketsBtn);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel,     BorderLayout.CENTER);
        sidebar.add(logoutBtn,    BorderLayout.SOUTH);
        return sidebar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JButton makeNavBtn(String text, String iconFile) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + iconFile, 20, 20));
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

    private double parseMoney(Object value) {
        if (value == null) return 0.0;
        try {
            return Double.parseDouble(
                    value.toString().replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) { return 0.0; }
    }
}
