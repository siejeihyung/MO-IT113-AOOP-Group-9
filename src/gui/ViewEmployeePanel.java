package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.Vector;

public class ViewEmployeePanel extends JPanel {

    private static final Font UI_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Color GRADIENT_START = new Color(255, 204, 229);
    private static final Color GRADIENT_END = new Color(255, 229, 180);
    
    public ViewEmployeePanel(Vector<Object> employeeData) {
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, GRADIENT_START, 0, getHeight(), GRADIENT_END));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setOpaque(false);
        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        String[][] sections = {
            {"Personal Information", "Employee No.", "Last Name", "First Name", "Birthday", "Address", "Phone Number"},
            {"Government Identifications", "SSS No.", "PhilHealth No.", "TIN No.", "PAG-IBIG No."},
            {"Job Information", "Status", "Position", "Immediate Supervisor"},
            {"Compensation & Benefits", "Basic Salary", "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"}
        };

        int dataIndex = 0;
        gbc.gridy = 0;

        for (String[] section : sections) {
            JLabel sectionTitle = new JLabel(section[0]);
            sectionTitle.setFont(HEADER_FONT);
            sectionTitle.setForeground(new Color(70, 70, 70));
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            leftPanel.add(sectionTitle, gbc);
            gbc.gridy++;
            gbc.gridwidth = 1;

            for (int i = 1; i < section.length; i++) {
                gbc.gridx = 0;
                JLabel label = new JLabel(section[i] + ":");
                label.setFont(UI_FONT);
                leftPanel.add(label, gbc);

                gbc.gridx = 1;
                JTextArea dataField = new JTextArea(employeeData.get(dataIndex++).toString());
                dataField.setEditable(false);
                dataField.setOpaque(false);
                dataField.setFont(UI_FONT);
                dataField.setBorder(null);
                leftPanel.add(dataField, gbc);
                gbc.gridy++;
            }
            gbc.gridy++;
        }

        JScrollPane scrollPane = new JScrollPane(leftPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUI(createScrollBarUI());

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navigationPanel.setOpaque(false);
        navigationPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.setFocusPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });

        navigationPanel.add(backBtn);
        add(scrollPane, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);
    }

    private BasicScrollBarUI createScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = Color.WHITE;
                this.trackColor = GRADIENT_END;
            }
        };
    }
}