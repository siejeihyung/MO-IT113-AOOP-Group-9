package gui;

import service.AuthService;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class UserManagementPanel extends JFrame {

    private final AuthService authService;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton addButton;
    private final JButton updateButton;
    private final JLabel feedbackLabel;

    public UserManagementPanel(AuthService authService) {
        this.authService = authService;
        setTitle("User Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        JPanel card = createCardPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 2, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Title
        JLabel titleLabel = new JLabel("Manage Users", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.DARK_GRAY);
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Username
        gbc.gridy++;
        card.add(new JLabel("Username") {{ setForeground(Color.GRAY); }}, gbc);
        gbc.gridy++;
        usernameField = new JTextField();
        usernameField.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        card.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        card.add(new JLabel("Password") {{ setForeground(Color.GRAY); }}, gbc);
        gbc.gridy++;
        passwordField = new JPasswordField();
        passwordField.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        card.add(passwordField, gbc);

        // Feedback
        gbc.gridy++;
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setForeground(Color.RED);
        card.add(feedbackLabel, gbc);

        // Buttons
        gbc.gridy++;
        addButton = createStyledButton("Add User");
        card.add(addButton, gbc);
        gbc.gridy++;
        updateButton = createStyledButton("Update Password");
        card.add(updateButton, gbc);

        add(card);
        setVisible(true);

        addButton.addActionListener(e -> handleAddUser());
        updateButton.addActionListener(e -> handleUpdatePassword());
    }

    private void handleAddUser() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            feedbackLabel.setText("Username and password cannot be empty.");
        } else if (authService.userExists(user)) {
            feedbackLabel.setText("Username already exists.");
        } else if (authService.addUser(user, pass)) {
            feedbackLabel.setForeground(new Color(34, 139, 34));
            feedbackLabel.setText("User added successfully.");
        } else {
            feedbackLabel.setForeground(Color.RED);
            feedbackLabel.setText("Failed to add user.");
        }
    }

    private void handleUpdatePassword() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (authService.updateUserPassword(user, pass)) {
            feedbackLabel.setForeground(new Color(34, 139, 34));
            feedbackLabel.setText("Password updated.");
        } else {
            feedbackLabel.setText("User not found or update failed.");
        }
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(400, 320));
        return card;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 191, 255));
        button.setFocusPainted(false);
        return button;
    }
}