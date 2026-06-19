/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.TicketDAO;
import model.Ticket;
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author SunnyEljohn
 */
public class TicketSupportPanel extends JPanel {
    
    public TicketSupportPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setBackground(new Color(230, 235, 242));

        // Title
        JLabel title = new JLabel("Submit New Support Ticket");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // Form Area
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Ticket Details"));
        form.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fields
        String[] cats = {"Technical Issue", "Payroll Inquiry", "HR Systems", "Other"};
        JComboBox<String> categoryBox = new JComboBox<>(cats);
        JTextField subjectField = new JTextField(20);
        JTextArea messageArea = new JTextArea(5, 20);

        // Styling for medium-sized look
        Dimension fieldSize = new Dimension(300, 35);
        categoryBox.setPreferredSize(fieldSize);
        subjectField.setPreferredSize(fieldSize);
        
        // Add components to GridBag
        addField(form, "Category:", categoryBox, gbc, 0);
        addField(form, "Subject:", subjectField, gbc, 1);
        addField(form, "Message:", new JScrollPane(messageArea), gbc, 2);

        add(form, BorderLayout.CENTER);

        // Submit Button
        JButton submitBtn = new JButton("Submit Ticket");
        submitBtn.setPreferredSize(new Dimension(150, 40));
        submitBtn.setBackground(new Color(29, 69, 143)); 
        submitBtn.setForeground(Color.WHITE);
        add(submitBtn, BorderLayout.SOUTH);
    }

    private void addField(JPanel p, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        p.add(field, gbc);
    }
}