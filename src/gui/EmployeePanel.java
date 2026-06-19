// Package declaration; this class belongs to the 'gui' package
package gui;

// Imports the FileHandler class from the 'model' package
import model.FileHandler;
// Imports Swing components for UI (e.g., JPanel, JButton, etc.)
import javax.swing.*;
// Imports the EmptyBorder class for border customization
import javax.swing.border.EmptyBorder;
// Imports AWT components for layout and design
import java.awt.*;
// Imports ActionEvent class for handling button actions
import java.awt.event.ActionEvent;
// Imports Vector class for handling table row data
import java.util.Vector;
// Imports DefaultTableModel class for table data model
import javax.swing.table.DefaultTableModel;

// Class declaration for the EmployeePanel GUI component
public class EmployeePanel extends JPanel {

   private final Color gradientStart; // Your MotorPH Blue
    // Your MotorPH Blue
   private final Color gradientEnd = new Color(20, 50, 110);   // A slightly darker blue

    // Declares a search input field with a fixed column width of 20
//    private final JTextField searchField = new JTextField(20);
    // Declares a FileHandler object to manage file I/O operations
    private final FileHandler fileHandler;
    // Declares an EmployeeTable to display employee data
    private final EmployeeTable dashboardTable;
    private Object table;
    
    
    
    // Constructor for the EmployeePanel class
    public EmployeePanel() {
        this.gradientStart = new Color(29, 69, 143);
        this.setBackground(Color.BLUE); // To see if this layer is even the one causing the pink
        // Sets the layout of this panel to BorderLayout
        setLayout(new BorderLayout());
        // Makes this panel transparent (for gradient background)
        setOpaque(false);
        // Initializes the file handler
        fileHandler = new FileHandler();
        // Initializes the dashboard table with employee data
        dashboardTable = new EmployeeTable(fileHandler);

        // Creates the top panel containing search and action buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        // Makes the top panel transparent
        topPanel.setOpaque(false);
        // Adds padding around the top panel
        topPanel.setBorder(new EmptyBorder(20, 50, 0, 50));

        // Left section of the top panel (for search)
//        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
//        leftPanel.setOpaque(false); // Make it transparent
//        leftPanel.add(buildSearchPanel()); // Adds the search bar

        // Right section of the top panel (for buttons)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false); // Make it transparent

        // Create and style the "View Employee" button
        JButton viewButton = new JButton("View Employee");
        styleMinimalButton(viewButton, 120, 36);
        // Add click event handler to show employee details
        viewButton.addActionListener(e -> showSelectedEmployeeDetails());
        rightPanel.add(viewButton); // Add to the right panel

        // Create and style the "Add Employee" button
        JButton addButton = new JButton("Add Employee");
        styleMinimalButton(addButton, 120, 36);
        // Add click event handler to show add form
        addButton.addActionListener(e -> showAddEmployeeDialog());
        rightPanel.add(addButton); // Add to the right panel

        // Add both panels to the top panel
//        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(rightPanel, BorderLayout.EAST);
        // Add the top panel to the north of the main layout
        add(topPanel, BorderLayout.NORTH);

        // Adds padding to the table and places it in the center
        dashboardTable.setBorder(new EmptyBorder(20, 50, 10, 50));
        add(dashboardTable, BorderLayout.CENTER); // Show table in center

        // Bottom panel for Update and Delete buttons
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Right-aligned
        bottomButtonPanel.setOpaque(false); // Transparent
        bottomButtonPanel.setBorder(new EmptyBorder(10, 50, 20, 50)); // Padding

        // Create and style Update button (black background)
        JButton updateButton = new JButton("Update");
        styleColoredButton(updateButton, Color.BLACK, 120, 36);
        updateButton.setEnabled(false); // Initially disabled
        bottomButtonPanel.add(updateButton); // Add to bottom panel

        // Create and style Delete button (red background)
        JButton deleteButton = new JButton("Delete");
        styleColoredButton(deleteButton, new Color(220, 20, 60), 120, 36);
        deleteButton.setEnabled(false); // Initially disabled
        bottomButtonPanel.add(deleteButton); // Add to bottom panel

        // Add bottom panel to the south of the main layout
        add(bottomButtonPanel, BorderLayout.SOUTH);

        // Enable/disable buttons based on row selection in the table
        dashboardTable.getTable().getSelectionModel().addListSelectionListener(e -> {
            boolean isSelected = dashboardTable.getTable().getSelectedRow() != -1;
            updateButton.setEnabled(isSelected);
            deleteButton.setEnabled(isSelected);
        });

        // Add click event for Update button
        updateButton.addActionListener(e -> {
                // Get selected row index
                int selectedRow = dashboardTable.getTable().getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "⚠ Please select a row to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Get Employee ID from the first column
                String employeeId = dashboardTable.getTable().getValueAt(selectedRow, 0).toString();
                // Get full employee details using ID
                String[] fullRow = fileHandler.getEmployeeById(employeeId);

                // Show error if not found
                if (fullRow == null) {
                    JOptionPane.showMessageDialog(null, "❌ Employee record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Fields editable in the update form
                String[] editableFields = {
                    "Last Name", "First Name", "Birthday", "Address", "Phone Number",
                    "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position",
                    "Immediate Supervisor", "Basic Salary", "Rice Subsidy", "Phone Allowance",
                    "Clothing Allowance", "Gross Semi-monthly Rate", "Hourly Rate"
                };

                // Get header titles from file
                String[] headers = fileHandler.getEmployeeHeaders();

                 // Form panel with gradient background
                JPanel panel = new JPanel(new SpringLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };

            // Add Employee ID field (non-editable)
            JLabel lblId = new JLabel("Employee ID *:");
            lblId.setForeground(Color.RED); // Red asterisk
            JTextField txtId = new JTextField(employeeId, 20);
            txtId.setEditable(false); // Lock field
            txtId.setBackground(new Color(240, 240, 240));
            txtId.setToolTipText("🔒 This field is locked for all employees.");
            panel.add(lblId);
            panel.add(txtId);

            // Check if employee is "Regular" to lock salary fields
            int statusIndex = java.util.Arrays.asList(headers).indexOf("Status");
            boolean isRegular = statusIndex != -1 && fullRow[statusIndex].equalsIgnoreCase("Regular");

            // Create text fields and labels for each editable field
            JTextField[] textFields = new JTextField[editableFields.length];
            String[] originalValues = new String[editableFields.length];

            for (int i = 0; i < editableFields.length; i++) {
                String fieldName = editableFields[i];
                int fieldIndex = java.util.Arrays.asList(headers).indexOf(fieldName);

                // Lock salary fields for Regular employees
                boolean isLockedField = isRegular && (
                    fieldName.equals("Basic Salary") ||
                    fieldName.equals("Rice Subsidy") ||
                    fieldName.equals("Phone Allowance") ||
                    fieldName.equals("Clothing Allowance") ||
                    fieldName.equals("Gross Semi-monthly Rate") ||
                    fieldName.equals("Hourly Rate")
                );

                JLabel label = new JLabel(isLockedField ? fieldName + " *" : fieldName + ":");
                if (isLockedField) label.setForeground(Color.RED);

                JTextField textField = new JTextField(20);
                textField.setEditable(!isLockedField);

                // Pre-fill data
                if (fieldIndex != -1 && fieldIndex < fullRow.length) {
                    textField.setText(fullRow[fieldIndex]);
                    originalValues[i] = fullRow[fieldIndex];
                }

                if (isLockedField) {
                    textField.setBackground(new Color(240, 240, 240));
                    textField.setToolTipText("🔒 This field is locked for Regular employees.");
                }

                panel.add(label);
                panel.add(textField);
                textFields[i] = textField;
            }

            // Add note explaining locked fields
            JLabel note = new JLabel("* Locked field — cannot be edited.");
            note.setForeground(Color.RED);
            panel.add(note);
            panel.add(new JLabel()); // Spacer

            // Use SpringUtilities to layout the panel neatly
            SpringUtilities.makeCompactGrid(panel, editableFields.length + 2, 2, 10, 10, 10, 10);

                // Modal dialog setup
            JDialog dialog = new JDialog((Frame) null, "Update Employee", true);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setSize(650, 750);
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(true);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JScrollPane(panel), BorderLayout.CENTER);

            // Save and cancel buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(new Color(0, 240, 240)); // 👈 replace with your desired color
            
            buttonPanel.setOpaque(false); // 🔹 Make panel transparent
            JButton saveBtn = new JButton("Save");
            styleMinimalButton(saveBtn, 120, 36);
            saveBtn.addActionListener(evt -> {
                boolean anyChanged = false;
                for (int i = 0; i < editableFields.length; i++) {
                    String newVal = textFields[i].getText().trim();
                    if (!newVal.equals(originalValues[i]) && textFields[i].isEditable()) {
                        boolean updated = fileHandler.updateEmployeeField(employeeId, editableFields[i], newVal);
                        anyChanged |= updated;
                    }
                }
                if (anyChanged) { 
                    showCustomMessage("Employee record updated successfully.", "Updated");
                    dashboardTable.refreshTable(fileHandler.getEmployeeData());
                } else {
                    showCustomMessage("No changes were made.", "Message");
                }
                dialog.dispose();
            });

            JButton cancelBtn = new JButton("Cancel");
            styleColoredButton(cancelBtn, new Color(128, 128, 128), 100, 36);
            cancelBtn.addActionListener(evt -> dialog.dispose());

            buttonPanel.add(saveBtn);
            buttonPanel.add(cancelBtn);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);


            });

        // Add click event for Delete button
        deleteButton.addActionListener(e -> {
                int selectedRow = dashboardTable.getTable().getSelectedRow();
                if (selectedRow == -1) { 
                    showCustomMessage("Please select an employee to delete.", "Message");
                    return;
                }

                String employeeId = (String) dashboardTable.getTable().getValueAt(selectedRow, 0); // <-- KEEP THIS HERE

                // Custom delete confirmation dialog
                    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Warning", true);
                    dialog.setSize(400, 180);
                    dialog.setLocationRelativeTo(this);
                    dialog.setLayout(new BorderLayout());

                    // Gradient background panel
                    JPanel content = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2d = (Graphics2D) g;
                            g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                            g2d.fillRect(0, 0, getWidth(), getHeight());
                        }
                    };
                    content.setBorder(new EmptyBorder(20, 20, 20, 20));
                    content.setLayout(new BorderLayout(10, 10));

                    // Confirmation message
                    JLabel message = new JLabel("<html><center>Are you sure you want to delete<br>employee ID <b>" + employeeId + "</b>?</center></html>", JLabel.CENTER);
                    message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    content.add(message, BorderLayout.CENTER);

                    // Button panel
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
                    buttonPanel.setOpaque(false);

                    JButton yesButton = new JButton("Yes");
                    styleColoredButton(yesButton, new Color(220, 20, 60), 80, 36);
                    yesButton.addActionListener(ev -> {
                        try {
                            fileHandler.deleteEmployeeById(employeeId);
                            dashboardTable.refreshTable(fileHandler.getEmployeeData());
                                showCustomMessage("Record Deleted Successfully", "Deleted");

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this, "❌ Failed to delete: " + ex.getMessage());
                        }
                        dialog.dispose();
                    });

                    JButton noButton = new JButton("No");
                    styleColoredButton(noButton, Color.GRAY, 80, 36);
                    noButton.addActionListener(ev -> dialog.dispose());

                    buttonPanel.add(yesButton);
                    buttonPanel.add(noButton);

                    content.add(buttonPanel, BorderLayout.SOUTH);
                    dialog.setContentPane(content);
                    dialog.setVisible(true);

            });
    }

    // Method to reload and update table with latest data
    private void refreshEmployeeTable() {
        fileHandler.readEmployeeFile();
        java.util.List<String[]> updatedData = fileHandler.getEmployeeData();

        DefaultTableModel model = (DefaultTableModel) dashboardTable.getTable().getModel();
        model.setRowCount(0); // Clear old data

        for (String[] row : updatedData) {
            model.addRow(new Object[]{row[0], row[1], row[2], row[6], row[7], row[8], row[9]});
        }
    }

    // Builds the search bar panel
//    private JPanel buildSearchPanel() {
//        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
//        panel.setOpaque(false);
//
//        searchField.setToolTipText("Search by ID, Last Name, or First Name");
//        searchField.setPreferredSize(new Dimension(200, 36));
//        searchField.setBackground(Color.WHITE);
//        searchField.setForeground(Color.BLACK);
//        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
//        searchField.setBorder(BorderFactory.createCompoundBorder(
//                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
//                BorderFactory.createEmptyBorder(5, 10, 5, 10)
//        ));
//
//        JButton searchButton = new JButton("Search");
//        styleColoredButton(searchButton, new Color(30, 144, 255), 80, 36);
//        searchButton.addActionListener(this::performSearch);
//
//        panel.add(searchField);
//        panel.add(searchButton);
//        return panel;
//    }

    // Filters the table using search input
//    private void performSearch(ActionEvent e) {
//        String query = searchField.getText().trim().toLowerCase();
//        dashboardTable.filterTable(query);
//    }

    // Displays full employee details in a new panel
    private void showSelectedEmployeeDetails() {
     // 1. Get the selected employee's full data from the table
     Vector<Object> selectedEmployee = dashboardTable.getSelectedEmployeeFullDetails();

     if (selectedEmployee != null) {
         // 2. Create a new window (JFrame) to hold the details
         JFrame detailFrame = new JFrame("Employee Information - " + selectedEmployee.get(0));
         detailFrame.setSize(1000, 700);
         detailFrame.setLocationRelativeTo(null);
         detailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

         // 3. Add the ViewEmployeePanel to this frame
         ViewEmployeePanel detailPanel = new ViewEmployeePanel(selectedEmployee);
         detailFrame.add(detailPanel);

         // 4. Show the window
         detailFrame.setVisible(true);
     } else {
         // Warning if no row is clicked
         JOptionPane.showMessageDialog(this, "Please select an employee from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
     }
 }
        private void showCustomMessage(String message, String title) {
        JDialog customDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        customDialog.setSize(400, 260);
        customDialog.setLocationRelativeTo(this);
        customDialog.setLayout(new BorderLayout());

        // Gradient background panel
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        contentPanel.setLayout(new BorderLayout(10, 10));

        // Message label
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        contentPanel.add(messageLabel, BorderLayout.CENTER);

        // OK Button panel
        JPanel dialogButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dialogButtonPanel.setOpaque(false);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(80, 30));
        okButton.setFocusPainted(false);
        okButton.setContentAreaFilled(false);
        okButton.setBorderPainted(false);
        okButton.setOpaque(false);

        // Rounded button UI
        okButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        okButton.addActionListener(e -> customDialog.dispose());
        dialogButtonPanel.add(okButton);

        contentPanel.add(dialogButtonPanel, BorderLayout.SOUTH);
        customDialog.setContentPane(contentPanel);
        customDialog.setVisible(true);
    }
        

    // Opens Add Employee window with callback to refresh table
    private void showAddEmployeeDialog() {
        FileHandler fileHandler = new FileHandler();
        fileHandler.readEmployeeFile();

        JFrame frame = new JFrame("Add Employee Panel with Date Picker");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(550, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.add(new AddEmployeePanel(fileHandler, () -> {
            dashboardTable.refreshTable(fileHandler.getEmployeeData());
            frame.setVisible(false);
        }));
    }

    // Styling method for minimal black buttons
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
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(60, 100, 180));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        button.setMargin(new Insets(0, 15, 0, 15));
    }

    // Styling method for colored buttons (like delete, search)
    private void styleColoredButton(JButton button, Color bgColor, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });

        button.setMargin(new Insets(0, 15, 0, 15));
    }

    // Overrides paintComponent to add gradient background to panel
    @Override
    protected void paintComponent(Graphics g) {
        // Leave this empty or just call the super method.
        // This removes the pink/orange gradient and lets the Dashboard's blue show.
    super.paintComponent(g);
    }
    // REPLACING THE BROKEN METHOD AT THE BOTTOM
    public EmployeeTable getDashboardTable() {
        return this.dashboardTable; 
    }
}

 //   Object getDashboardTable() {
 //       return this.dashboardTable; // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
 //   }
// }