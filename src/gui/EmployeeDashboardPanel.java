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
import gui.PayslipFrame;
import model.PayrollLogic;

/**
 * EmployeeDashboardPanel — Limited dashboard for regular employees.
 * RBAC: Employees only see their own data.
 * - My Profile (Upgraded to Modern Card Summary Tables with Profile Badge)
 * - My Attendance (clock in/out)
 * - My Leave (balance + file leave + history)
 * - My Payslip
 */
public class EmployeeDashboardPanel extends JFrame {

    private final String          employeeId;
    private final EmployeeService employeeService;
    private final LeaveService     leaveService;
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

        // Extract Position String early so it can be passed safely to the Sidebar
        String positionStr = (empData != null && empData.length > 11) ? empData[11] : "Employee";

        setTitle("MotorPH — " + name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── Build panels ──────────────────────────────────────────────────────
        JPanel myInfoPanel                        = buildMyInfoPanel(empData);
        EmployeeAttendancePanel myAttendancePanel = new EmployeeAttendancePanel(employeeId);
        JPanel myLeavePanel                       = buildMyLeavePanel();

        // ── Content area ──────────────────────────────────────────────────────
        contentPanel = new JPanel(cardLayout) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(243, 244, 246)); // Matching soft dashboard background canvas
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        myAttendancePanel.setOpaque(false);

        contentPanel.add(myInfoPanel,         "MyInfo");
        contentPanel.add(myAttendancePanel,   "MyAttendance");
        contentPanel.add(myLeavePanel,        "MyLeave");

        // ── Build sidebar (With Position Argument) ────────────────────────────
        JPanel sidebar = buildSidebar(name, positionStr);

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
    private JPanel buildSidebar(String name, String position) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Profile Panel Left Top
        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.add(new JLabel(loadIcon("/assets/userprofile.png", 40, 40)), BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(name);
        
        // Dynamically translate or fall back if structural value is "0"
        String displayRole = position;
        if (position == null || position.trim().isEmpty() || position.trim().equals("0")) {
            displayRole = "Regular Employee";
        } else if (position.trim().equals("1")) {
            displayRole = "System Administrator";
        } else if (position.trim().equals("2")) {
            displayRole = "HR Generalist";
        }

        JLabel roleLabel = new JLabel(displayRole);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.GRAY);
        namePanel.add(nameLabel);
        namePanel.add(roleLabel);
        profilePanel.add(namePanel, BorderLayout.CENTER);

        // Navigation Menu Panel
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel myDataLabel = new JLabel("My Data");
        myDataLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        myDataLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        myDataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Navigation Buttons
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
        sidebar.add(navPanel,      BorderLayout.CENTER);
        sidebar.add(logoutBtn,    BorderLayout.SOUTH);
        return sidebar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  My Profile Panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMyInfoPanel(String[] empData) {
        Color colorBg = new Color(243, 244, 246);       
        Color colorCard = Color.WHITE;                  
        Color colorPrimaryText = new Color(31, 41, 55); 
        Color colorMutedText = new Color(107, 114, 128); 
        Color colorHeaderBg = new Color(20, 50, 110);   
        Color colorBorder = new Color(229, 231, 235);

        JPanel wrapper = new JPanel(new BorderLayout(0, 16));
        wrapper.setBackground(colorBg);
        wrapper.setBorder(new EmptyBorder(24, 24, 24, 24));

        // --- 1. TOP HEADER BANNER ---
        JPanel headerCard = new JPanel(new BorderLayout());
        headerCard.setBackground(colorHeaderBg);
        headerCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        
        String firstName = (empData != null && empData.length > 2) ? empData[2] : "";
        String lastName = (empData != null && empData.length > 1) ? empData[1] : "";
        JLabel welcomeLabel = new JLabel("Welcome back, " + firstName + " " + lastName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(Color.WHITE);
        
        String positionStr = (empData != null && empData.length > 11) ? empData[11] : "Employee";
        
        // Dynamic Role Translation to clean structural "0" values
        String structuralRole = positionStr;
        if (positionStr.trim().equals("0") || positionStr.trim().isEmpty()) {
            structuralRole = "Regular Employee";
        } else if (positionStr.trim().equals("1")) {
            structuralRole = "System Administrator";
        } else if (positionStr.trim().equals("2")) {
            structuralRole = "HR Generalist";
        }

        JLabel subHeaderLabel = new JLabel("Role Profile: " + structuralRole);
        subHeaderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subHeaderLabel.setForeground(new Color(219, 234, 254));
        
        headerCard.add(welcomeLabel, BorderLayout.WEST);
        headerCard.add(subHeaderLabel, BorderLayout.EAST);
        wrapper.add(headerCard, BorderLayout.NORTH);

        // --- 2. MAIN SPLIT LAYOUT CONTAINER ---
        JPanel mainContentGrid = new JPanel(new GridBagLayout());
        mainContentGrid.setBackground(colorBg);
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.weighty = 1.0;

        // --- LEFT COLUMN: EMPLOYEE BADGE CARD ---
        JPanel badgeCard = new JPanel();
        badgeCard.setLayout(new BoxLayout(badgeCard, BoxLayout.Y_AXIS));
        badgeCard.setBackground(colorCard);
        badgeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorder, 1),
                new EmptyBorder(30, 20, 30, 20)
        ));

        JLabel avatarLabel = new JLabel(loadIcon("/assets/userprofile.png", 80, 80));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLbl = new JLabel(firstName + " " + lastName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLbl.setForeground(colorPrimaryText);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel posLbl = new JLabel(structuralRole);
        posLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        posLbl.setForeground(colorMutedText);
        posLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel idLbl = new JLabel("ID: " + cleanData(empData, 0));
        idLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        idLbl.setForeground(colorHeaderBg);
        idLbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 234, 254), 1),
                new EmptyBorder(4, 12, 4, 12)
        ));
        idLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        badgeCard.add(avatarLabel);
        badgeCard.add(Box.createVerticalStrut(15));
        badgeCard.add(nameLbl);
        badgeCard.add(Box.createVerticalStrut(4));
        badgeCard.add(posLbl);
        badgeCard.add(Box.createVerticalStrut(15));
        badgeCard.add(idLbl);

        mainGbc.gridx = 0; mainGbc.gridy = 0;
        mainGbc.weightx = 0.25; // 25% of content width
        mainGbc.insets = new Insets(0, 0, 0, 16);
        mainContentGrid.add(badgeCard, mainGbc);

        // --- RIGHT COLUMN: THE SUMMARY TABLES ---
        JPanel dataTablesContainer = new JPanel(new GridLayout(2, 1, 0, 16));
        dataTablesContainer.setBackground(colorBg);

        // Table 1: Primary Identification
        JPanel primaryTableCard = createTableCard("Primary Identification Records", colorCard, colorMutedText);
        addTableRow(primaryTableCard, "Employee ID", cleanData(empData, 0), colorPrimaryText, colorBorder, 0);
        addTableRow(primaryTableCard, "Full Name", firstName + " " + lastName, colorPrimaryText, colorBorder, 1);
        addTableRow(primaryTableCard, "Birthday", cleanData(empData, 3), colorPrimaryText, colorBorder, 2);
        addTableRow(primaryTableCard, "Phone Number", cleanData(empData, 5), colorPrimaryText, colorBorder, 3);
        addTableRow(primaryTableCard, "Home Address", cleanData(empData, 4), colorPrimaryText, colorBorder, 4);
        dataTablesContainer.add(primaryTableCard);

        // Table 2: Government & Finance
        JPanel financeTableCard = createTableCard("Government statutory IDs & Payroll", colorCard, colorMutedText);
        addTableRow(financeTableCard, "SSS #", cleanData(empData, 6), colorPrimaryText, colorBorder, 0);
        addTableRow(financeTableCard, "PhilHealth #", cleanData(empData, 7), colorPrimaryText, colorBorder, 1);
        addTableRow(financeTableCard, "TIN #", cleanData(empData, 8), colorPrimaryText, colorBorder, 2);
        addTableRow(financeTableCard, "Pag-IBIG #", cleanData(empData, 9), colorPrimaryText, colorBorder, 3);
        addTableRow(financeTableCard, "Basic Salary", formatToCurrency(cleanData(empData, 13)), colorPrimaryText, colorBorder, 4);
        addTableRow(financeTableCard, "Hourly Rate", formatToCurrency(cleanData(empData, 18)), colorPrimaryText, colorBorder, 5);
        dataTablesContainer.add(financeTableCard);

        mainGbc.gridx = 1; mainGbc.gridy = 0;
        mainGbc.weightx = 0.75; // 75% of content width
        mainGbc.insets = new Insets(0, 0, 0, 0);
        mainContentGrid.add(dataTablesContainer, mainGbc);

        wrapper.add(mainContentGrid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTableCard(String title, Color cardBg, Color headerColor) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(headerColor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(titleLabel, gbc);
        return card;
    }

    private void addTableRow(JPanel card, String itemKey, String itemValue, Color txtColor, Color borderColor, int rowIdx) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.gridy = rowIdx + 1;

        JPanel rowLineContainer = new JPanel(new BorderLayout());
        rowLineContainer.setBackground(Color.WHITE);
        rowLineContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));

        JLabel keyLabel = new JLabel(itemKey);
        keyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        keyLabel.setForeground(new Color(107, 114, 128));
        keyLabel.setPreferredSize(new Dimension(160, 35));

        JLabel valueLabel = new JLabel(itemValue);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(txtColor);

        rowLineContainer.add(keyLabel, BorderLayout.WEST);
        rowLineContainer.add(valueLabel, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        card.add(rowLineContainer, gbc);
        
        GridBagConstraints pushGbc = new GridBagConstraints();
        pushGbc.gridx = 0; pushGbc.gridy = rowIdx + 2;
        pushGbc.weighty = 1.0;
        pushGbc.fill = GridBagConstraints.BOTH;
        card.add(Box.createGlue(), pushGbc);
    }

    private String cleanData(String[] data, int index) {
        if (data == null || index >= data.length || data[index] == null) return "—";
        String raw = data[index].trim().replace("\"", "");
        if (raw.endsWith(".0")) {
            raw = raw.substring(0, raw.length() - 2);
        }
        if (raw.equals("0") || raw.isEmpty()) {
            return "—";
        }
        return raw;
    }

    private String formatToCurrency(String amount) {
        if (amount.equals("—")) return "—";
        try {
            double parsed = Double.parseDouble(amount);
            return java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "PH")).format(parsed);
        } catch (Exception e) {
            return "₱" + amount;
        }
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
        title.setForeground(new Color(20, 50, 110));

        JPanel balanceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        balanceRow.setOpaque(false);
        balanceRow.add(makeBalanceCard("Sick Leave", leaveService.getRemainingBalance(employeeId, "Sick"), 5));
        balanceRow.add(makeBalanceCard("Vacation Leave", leaveService.getRemainingBalance(employeeId, "Vacation"), 10));
        balanceRow.add(makeBalanceCard("Emergency Leave", leaveService.getRemainingBalance(employeeId, "Emergency"), 3));

        JButton fileLeaveBtn = makeActionButton("+ File Leave", new Color(56, 142, 60));
        fileLeaveBtn.addActionListener(e -> openFileLeaveDialog());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnRow.setOpaque(false);
        btnRow.add(fileLeaveBtn);

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
        if (empData == null || empData.length < 19) {
            JOptionPane.showMessageDialog(this, "Unable to pull comprehensive payroll data mapping schema.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Vector<Object> dataVector = new Vector<>();
        for (String s : empData) {
            dataVector.add(s != null ? s.trim().replace("\"", "") : "");
        }

        try {
            java.util.function.Function<String, Double> safeParse = (val) -> {
                if (val == null || val.trim().isEmpty() || val.trim().equals("—")) return 0.0;
                try {
                    return Double.parseDouble(val.replace(",", "").replace("\"", "").trim());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            };

            // 1. Extract database compensation metrics
            double basicSalary = safeParse.apply(empData[13]);
            double rice        = safeParse.apply(empData[14]);
            double phone       = safeParse.apply(empData[15]);
            double clothing    = safeParse.apply(empData[16]);
            double hourlyRate  = safeParse.apply(empData[18]);
            
            // Assuming full standard baseline hours if running a fixed simulation snapshot
            double hoursWorked = 40.0; 

            // 2. Initialize your genuine engine calculator class
            PayrollLogic logicEngine = new PayrollLogic();

            // 3. Process accurate calculations matching your console/system structure
            double gross = logicEngine.calculateGrossWeeklySalary(hourlyRate, hoursWorked, rice, phone, clothing);
            double netPay = logicEngine.calculateNetWeeklySalary(basicSalary, gross);
            double totalDeductions = gross - netPay;

            System.out.println("[Payroll Execution Log] Gross Total calculated: " + gross + " | Net: " + netPay);

            SwingUtilities.invokeLater(() -> {
                try {
                    PayslipFrame printableFrame = new PayslipFrame(dataVector, gross, totalDeductions, netPay);
                    printableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    printableFrame.setVisible(true);
                } catch (Exception frameEx) {
                    frameEx.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to load printable component: " + frameEx.getMessage());
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payroll Engine Failure:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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