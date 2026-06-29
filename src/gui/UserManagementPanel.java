package gui;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import model.FileHandler;

public class UserManagementPanel extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton addButton;
    private final JButton updateButton;
    private final JLabel feedbackLabel;

    public UserManagementPanel() {
        setTitle("User Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null); // Center on screen

        // Main layout (no background image)
        setLayout(new GridBagLayout());

        // Card-style Panel
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30); // Rounded panel
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(400, 320));
        card.setLayout(new GridBagLayout());

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
        JLabel userLabel = new JLabel("Username");
        userLabel.setForeground(Color.GRAY);
        card.add(userLabel, gbc);

        gbc.gridy++;
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setOpaque(false);
        usernameField.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        card.add(usernameField, gbc);

        // Password
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(Color.GRAY);
        card.add(passLabel, gbc);

        gbc.gridy++;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setOpaque(false);
        passwordField.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        card.add(passwordField, gbc);

        // Feedback Label
        gbc.gridy++;
        feedbackLabel = new JLabel(" ", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        feedbackLabel.setForeground(Color.RED);
        card.add(feedbackLabel, gbc);

        // Add Button
        gbc.gridy++;
        addButton = createStyledButton("Add User");
        card.add(addButton, gbc);

        // Update Button
        gbc.gridy++;
        updateButton = createStyledButton("Update Password");
        card.add(updateButton, gbc);

        // Add card panel to JFrame
        add(card);
        setVisible(true);

        // Button logic
        FileHandler fileHandler = new FileHandler();

        addButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            // Validate empty fields
            if (user.isEmpty() || pass.isEmpty()) {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText("Username and password cannot be empty.");
                return;
            }

            if (fileHandler.userExists(user)) {
                feedbackLabel.setForeground(Color.RED);
                feedbackLabel.setText("Username already exists.");
            } else {
                if (fileHandler.addUser(user, pass)) {
                    feedbackLabel.setForeground(new Color(34, 139, 34));
                    feedbackLabel.setText("User added successfully.");
                    usernameField.setText("");
                    passwordField.setText("");
                } else {
                    feedbackLabel.setForeground(Color.RED);
                    feedbackLabel.setText("Failed to add user.");
                }
            }
        });


        updateButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (!fileHandler.userExists(user)) {
                feedbackLabel.setText("User not found.");
            } else {
                if (fileHandler.updateUserPassword(user, pass)) {
                    feedbackLabel.setForeground(new Color(34, 139, 34));
                    feedbackLabel.setText("Password updated.");
                } else {
                    feedbackLabel.setForeground(Color.RED);
                    feedbackLabel.setText("Failed to update password.");
                }
            }
        });
    }

    // Button design copied from LoginPanel
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) {
                    g2.setColor(Color.GRAY);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(30, 144, 255));
                } else {
                    g2.setColor(new Color(0, 191, 255));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {}
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
