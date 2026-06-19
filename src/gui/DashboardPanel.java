package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Vector;
import model.FileHandler;

public class DashboardPanel extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(); 
    private final EmployeePanel employeePanel = new EmployeePanel();

    public DashboardPanel(String user) {
        setTitle("MotorPH Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 500));
        
        Color sidebarColor = Color.WHITE;
        Font boldFont = new Font("Segoe UI", Font.BOLD, 16);
        Font regularFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(sidebarColor);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Profile panel
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(sidebarColor);
        JLabel profileIcon = new JLabel(loadImageIcon("/assets/userprofile.png", 40, 40));
        profilePanel.add(profileIcon, BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(sidebarColor);
        JLabel userName = new JLabel("Admin");
        JLabel userRole = new JLabel("HR Manager");
        userName.setFont(boldFont);
        userRole.setFont(regularFont);
        userRole.setForeground(Color.GRAY);
        namePanel.add(userName);
        namePanel.add(userRole);
        profilePanel.add(namePanel, BorderLayout.CENTER);

        // Nav panel
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(sidebarColor);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        navPanel.add(Box.createVerticalStrut(30));

        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(boldFont);
        generalLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(generalLabel);

        // Nav buttons
        JButton employeeBtn = createNavButton("Employee", "employee.png");
        JButton leaveBtn = createNavButton("Leave Management", "leave.png"); // NEW
        JButton adminPrintBtn = createNavButton("Print Payslip", "printer.png");

        navPanel.add(employeeBtn);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(leaveBtn);          // NEW
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(adminPrintBtn);
        navPanel.add(Box.createVerticalStrut(5));

        // Print Payslip action
        adminPrintBtn.addActionListener(e -> {
            EmployeeTable table = (EmployeeTable) employeePanel.getDashboardTable();
            Vector<Object> selected = table.getSelectedEmployeeFullDetails();
            if (selected != null) {
                double gross = parseMoney(selected.get(13));
                double deductions = 0.0;
                double net = gross - deductions;
                new PayslipFrame(selected, gross, deductions, net);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an employee first!");
            }
        });

        // Logout
        JButton logoutButton = createNavButton("Log-out", "logout.png");
        logoutButton.setForeground(Color.GRAY);
        logoutButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(logoutButton, BorderLayout.SOUTH);

        // Main content — solid blue background (matches new GUI style)
        contentPanel = new JPanel(cardLayout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(29, 69, 143));
                g.fillRect(0, 0, getWidth(), getHeight());
                employeePanel.setOpaque(false);
            }
        };

        // Initialize FileHandler and all panels
        FileHandler fileHandler = new FileHandler();
        AttendancePanel attendancePanel = new AttendancePanel(fileHandler);
        LeavePanel leavePanel = new LeavePanel(fileHandler); // NEW

        employeePanel.setOpaque(false);
        attendancePanel.setOpaque(false);
        leavePanel.setOpaque(false); // NEW — lets blue background show through

        // Register panels with CardLayout
        contentPanel.add(employeePanel, "Employee");
        contentPanel.add(leavePanel, "Leave"); // NEW

        // Wire buttons to panels
        employeeBtn.addActionListener(e -> cardLayout.show(contentPanel, "Employee"));
        leaveBtn.addActionListener(e -> cardLayout.show(contentPanel, "Leave")); // NEW

        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createNavButton(String text, String iconFileName) {
        JButton button = new JButton(text);
        button.setIcon(loadImageIcon("/assets/" + iconFileName, 20, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(15);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(Color.WHITE); }
        });
        return button;
    }

    private ImageIcon loadImageIcon(String path, int width, int height) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        System.err.println("Image not found: " + path);
        return null;
    }

    private double parseMoney(Object value) {
        if (value == null || value.toString().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.toString().replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}