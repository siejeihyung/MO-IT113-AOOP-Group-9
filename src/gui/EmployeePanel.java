package gui;

import service.EmployeeService;
import dao.EmployeeDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class EmployeePanel extends JPanel {

    private final Color gradientStart = new Color(29, 69, 143);
    private final Color gradientEnd = new Color(20, 50, 110);
    
    private final EmployeeService employeeService;
    private final EmployeeTable dashboardTable;

    public EmployeePanel() {
        // Initialize service and DAO
        this.employeeService = new EmployeeService(new EmployeeDAO());
        
        setLayout(new BorderLayout());
        setOpaque(false);

        // Initialize table
        dashboardTable = new EmployeeTable(employeeService);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(20, 50, 0, 50));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JButton viewButton = new JButton("View Employee");
        styleMinimalButton(viewButton, 120, 36);
        viewButton.addActionListener(e -> showSelectedEmployeeDetails());
        rightPanel.add(viewButton);

        JButton addButton = new JButton("Add Employee");
        styleMinimalButton(addButton, 120, 36);
        addButton.addActionListener(e -> showAddEmployeeDialog());
        rightPanel.add(addButton);

        topPanel.add(rightPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        dashboardTable.setBorder(new EmptyBorder(20, 50, 10, 50));
        add(dashboardTable, BorderLayout.CENTER);

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomButtonPanel.setOpaque(false);
        bottomButtonPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        JButton updateButton = new JButton("Update");
        styleColoredButton(updateButton, Color.BLACK, 120, 36);
        updateButton.setEnabled(false);
        bottomButtonPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        styleColoredButton(deleteButton, new Color(220, 20, 60), 120, 36);
        deleteButton.setEnabled(false);
        bottomButtonPanel.add(deleteButton);

        add(bottomButtonPanel, BorderLayout.SOUTH);

        dashboardTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = dashboardTable.getTable().getSelectedRow() != -1;
            updateButton.setEnabled(isSelected);
            deleteButton.setEnabled(isSelected);
        });
    }

    private void showAddEmployeeDialog() {
        JFrame frame = new JFrame("Add Employee");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(550, 600);
        frame.setLocationRelativeTo(null);
        // Pass the service instead of fileHandler
        frame.add(new AddEmployeePanel(employeeService, () -> {
            dashboardTable.refreshTable(employeeService.getAllEmployees());
            frame.dispose();
        }));
        frame.setVisible(true);
    }

    private void showSelectedEmployeeDetails() {
        java.util.Vector<Object> selected = dashboardTable.getSelectedEmployeeFullDetails();
        if (selected != null) {
            JFrame detailFrame = new JFrame("Employee Information");
            detailFrame.setSize(1000, 700);
            detailFrame.setLocationRelativeTo(null);
            detailFrame.add(new ViewEmployeePanel(selected));
            detailFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee first.");
        }
    }

    private void styleMinimalButton(JButton button, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 100, 180));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    private void styleColoredButton(JButton button, Color bgColor, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
    }

    public EmployeeTable getDashboardTable() { return this.dashboardTable; }
    
    @Override protected void paintComponent(Graphics g) { super.paintComponent(g); }
}