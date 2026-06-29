package gui;

// Importing the FileHandler class from the model package
import model.FileHandler;

// Importing date picker components from external library
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

// Swing components
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

// AWT classes for layout and graphics
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;

// This panel handles adding a new employee via GUI
public class AddEmployeePanel extends JPanel {

    // FileHandler instance to read/write employee data
    private final FileHandler fileHandler;

    // Stores form input components mapped to field names
    private final Map<String, JComponent> fieldMap = new LinkedHashMap<>();

    // Buttons for submitting the form or going back
    private final JButton submitButton = new JButton("Add Employee");
    private final JButton backButton = new JButton("Back");

    // Callback to be run after an employee is added
    private final Runnable onEmployeeAdded;

    // Additional fields not always in file headers
    private final String[] additionalFields = {"Birthday", "Phone Number"};

    // Panels to organize the form and bottom buttons
    private JPanel formPanel;
    private JPanel bottomPanel;

    // Constructor initializes UI and logic
    public AddEmployeePanel(FileHandler fileHandler, Runnable onEmployeeAdded) {
        this.fileHandler = fileHandler;
        this.onEmployeeAdded = onEmployeeAdded;

        // Set transparent background and layout
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // Add padding to panel edges
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create form layout with spacing
        formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setOpaque(false);

        // Container to hold form with extra padding
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        formContainer.add(formPanel, BorderLayout.NORTH);

        // Add scrolling capability to the form
        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setPreferredSize(new Dimension(500, 450));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        // Read employee headers from file
        fileHandler.readEmployeeFile();
        String[] headersFromFile = fileHandler.getEmployeeHeaders();

        // Combine headers from file and additional fields
        LinkedHashMap<String, Boolean> finalHeaders = new LinkedHashMap<>();
        for (String header : headersFromFile) {
            finalHeaders.put(header, isRequired(header));
        }
        for (String extra : additionalFields) {
            finalHeaders.putIfAbsent(extra, true);
        }

        // Generate form fields based on headers
        for (Map.Entry<String, Boolean> entry : finalHeaders.entrySet()) {
            String header = entry.getKey();
            boolean required = entry.getValue();

            JPanel labelPanel = new JPanel(new BorderLayout());
            labelPanel.setOpaque(false);

            JLabel label = new JLabel(header + ":");

            // Add red asterisk and bold if required
            if (required) {
                JLabel asterisk = new JLabel("*");
                asterisk.setForeground(Color.RED);
                asterisk.setFont(asterisk.getFont().deriveFont(Font.BOLD));
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                label.setToolTipText("Required field");
                labelPanel.add(label, BorderLayout.WEST);
                labelPanel.add(asterisk, BorderLayout.EAST);
            } else {
                // Grey italic style for optional fields
                label.setForeground(Color.GRAY);
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
                labelPanel.add(label, BorderLayout.WEST);
            }

            // Create appropriate input field
            JComponent inputField;
               switch (header.toLowerCase()) {
                case "employee #" -> {
                    JTextField field = new JTextField();
                    // Enforces numeric only and a 7-digit maximum
                    ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter(7));
                    addTooltipOnFocus(field, "Enter 5 to 7-digit ID");
                    inputField = field; 
                }
                case "birthday" -> {
                    DatePickerSettings settings = new DatePickerSettings();
                    settings.setFormatForDatesCommonEra("yyyy/MM/dd");
                    DatePicker datePicker = new DatePicker(settings);
                    addTooltipOnFocus(datePicker.getComponentDateTextField(), "Format: yyyy/MM/dd");
                    inputField = datePicker; 
                }
                case "phone number" -> {
                    inputField = createFormattedField("####-###-####");
                    addTooltipOnFocus((JTextField) inputField, "Format: 0000-000-0000");
                }
                case "sss #" -> {
                    inputField = createFormattedField("##-#######-#");
                    addTooltipOnFocus((JTextField) inputField, "Format: 00-0000000-0");
                }
                case "philhealth #" -> {
                    inputField = createFormattedField("####-####-####");
                    addTooltipOnFocus((JTextField) inputField, "Format: 0000-0000-0000");
                }
                case "tin #" -> {
                    inputField = createFormattedField("###-###-###-###");
                    addTooltipOnFocus((JTextField) inputField, "Format: 000-000-000-000");
                }
                case "pag-ibig #" -> {
                    inputField = createFormattedField("####-####-####");
                    addTooltipOnFocus((JTextField) inputField, "Format: 0000-0000-0000");
                }
                case "status" -> {
                    inputField = new JComboBox<>(new String[]{"Regular", "Probationary"});
                }
                case "position" -> {
                    inputField = new JComboBox<>(new String[]{"Staff", "HR Manager", "Finance Manager", "IT Support", "CEO"});
                }
                case "basic salary", "gross semi-monthly rate" -> {
                    JTextField salaryField = new JTextField();
                    // Blocks letters and extra decimals while typing
                    ((AbstractDocument) salaryField.getDocument()).setDocumentFilter(new MoneyDocumentFilter());

                    salaryField.addFocusListener(new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            String text = salaryField.getText().trim();
                            if (!text.isEmpty()) {
                                // Adds commas and .00 when the user clicks away
                                salaryField.setText(formatCurrency(text));
                            }
                        }
                        @Override
                        public void focusGained(FocusEvent e) {
                            // Removes commas to make editing easier when the user clicks back in
                            String text = salaryField.getText().replace(",", "");
                            salaryField.setText(text);
                        }
                    });

                    addTooltipOnFocus(salaryField, "Format: 25000.00 (Commas added automatically)");
                    inputField = salaryField;
                }
                default -> {
                    JTextField textField = new JTextField();
                    addTooltipOnFocus(textField, "Enter " + header);
                    inputField = textField;
                }
            }
               
            // Add label and input to form
            formPanel.add(labelPanel);
            formPanel.add(inputField);
            fieldMap.put(header, inputField);
        }

        // Customize button appearance
        submitButton.setBackground(Color.BLACK);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);

        backButton.setBackground(Color.BLACK);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);

        // Attach event listeners
        submitButton.addActionListener(this::addEmployee);
        backButton.addActionListener(e -> {
            if (onEmployeeAdded != null) onEmployeeAdded.run();
        });

        // Create bottom panel for buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.add(new JLabel("* Required fields"));
        bottomPanel.add(backButton);
        bottomPanel.add(submitButton);

        // Add scroll and buttons to main layout
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Draw gradient background for panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0xFFD1DC),
                0, h / 2, new Color(0xFFE4CC)
        );

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        g2d.dispose();
    }

    // Add employee logic with validation
    private void addEmployee(ActionEvent e) {
        String[] newRow = new String[fieldMap.size()];
        int index = 0;
        boolean hasError = false;
        StringBuilder errorMessages = new StringBuilder();

        for (JComponent field : fieldMap.values()) {
            field.setBorder(UIManager.getBorder("TextField.border"));
        }

        for (Map.Entry<String, JComponent> entry : fieldMap.entrySet()) {
            String header = entry.getKey();
            JComponent component = entry.getValue();
            String value = "";

            if (component instanceof JTextField) {
                value = ((JTextField) component).getText().trim();
            } else if (component instanceof DatePicker) {
                value = ((DatePicker) component).getDate() != null
                        ? ((DatePicker) component).getDate().toString()
                        : "";
            } else if (component instanceof JComboBox<?>) {
                value = ((JComboBox<?>) component).getSelectedItem().toString();
            }

            // --- VALIDATION LOGIC ---

            if (isRequired(header) && value.isEmpty()) {
                component.setBorder(new LineBorder(Color.RED, 2));
                hasError = true;
                errorMessages.append("- ").append(header).append(" is required.\n");
            }

            if (header.equalsIgnoreCase("Employee #")) {
                if (!value.isEmpty()) {
                    if (value.length() > 7) {
                        component.setBorder(new LineBorder(Color.RED, 2));
                        hasError = true;
                        errorMessages.append("- Employee # cannot exceed 7 digits.\n");
                    } else if (employeeNumberExists(value)) {
                        component.setBorder(new LineBorder(Color.RED, 2));
                        hasError = true;
                        errorMessages.append("- Employee ID ").append(value).append(" already exists.\n");
                    }
                }
            }

            if (header.equalsIgnoreCase("Basic Salary") || header.equalsIgnoreCase("Gross Semi-monthly Rate")) {
                String cleanValue = value.replace(",", ""); 

                if (!cleanValue.isEmpty() && !cleanValue.matches("^[0-9]*\\.?[0-9]+$")) {
                    component.setBorder(new LineBorder(Color.RED, 2));
                    hasError = true;
                    errorMessages.append("- ").append(header).append(" must be a valid numeric amount.\n");
                } else {
                    
                    value = cleanValue;
                    
                }
            }

            String numericOnlyValue = value.replaceAll("[^0-9]", "");
            if (header.equalsIgnoreCase("Phone Number") || header.equalsIgnoreCase("SSS #") ||
                header.equalsIgnoreCase("Philhealth #") || header.equalsIgnoreCase("TIN #") ||
                header.equalsIgnoreCase("Pag-ibig #")) {

                if (!value.isEmpty() && !numericOnlyValue.matches("\\d+")) {
                    component.setBorder(new LineBorder(Color.RED, 2));
                    hasError = true;
                    errorMessages.append("- ").append(header).append(" must contain only numbers.\n");
                }
            }

            if (index < newRow.length) {
                newRow[index++] = value;
            }
        }

        // Final Error Reporting
        if (hasError) {
            JOptionPane.showMessageDialog(this, "Please fix the following:\n" + errorMessages,
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        // Save to file
        if (fileHandler.appendEmployeeToFile(newRow)) {
            fileHandler.readEmployeeFile();
            JOptionPane.showMessageDialog(this, "✅ Employee added successfully!");
            clearFields();
            if (onEmployeeAdded != null) onEmployeeAdded.run();
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Utility method to add a tooltip that shows on focus
        private void addTooltipOnFocus(JTextField textField, String tooltipText) {
            textField.setToolTipText(tooltipText);
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    ToolTipManager.sharedInstance().mouseMoved(
                        new java.awt.event.MouseEvent(
                            textField,
                            java.awt.event.MouseEvent.MOUSE_MOVED,
                            System.currentTimeMillis(),
                            0,
                            1, 1,
                            0, false
                        )
                    );
                }
            });
        }
        
    // Shows tooltip text when the user focuses into the field
        private void addTooltipOnFocus2(JTextField textField, String tooltipText) {
            textField.setToolTipText(tooltipText);
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    ToolTipManager.sharedInstance().mouseMoved(
                        new java.awt.event.MouseEvent(
                            textField,
                            java.awt.event.MouseEvent.MOUSE_MOVED,
                            System.currentTimeMillis(),
                            0,
                            1, 1,
                            0, false
                        )
                    );
                }
            });
        }

        
    // Check if employee number already exists in file
    private boolean employeeNumberExists(String empNum) {
        // 1. Refresh the data from the CSV to ensure we're checking against the latest save
        fileHandler.readEmployeeFile(); 

        // 2. Sanitize the input ID: remove spaces and any non-numeric mask characters
        String cleanInput = empNum.trim().replaceAll("[^0-9]", "");

        // If the input is somehow empty after cleaning, don't flag as duplicate
        if (cleanInput.isEmpty()) return false;

        // 3. Compare against every row in the file
        for (String[] row : fileHandler.getEmployeeData()) {
            if (row != null && row.length > 0) {
                // Sanitize the ID found in the CSV row (usually the first column)
                String cleanRowId = row[0].trim().replaceAll("[^0-9]", "");

                if (cleanRowId.equals(cleanInput)) {
                    return true; // Match found - this is a duplicate!
                }
            }
        }
        return false;
    }
    // Clear all input fields in the form
    private void clearFields() {
        for (Map.Entry<String, JComponent> entry : fieldMap.entrySet()) {
            JComponent field = entry.getValue();
            if (field instanceof JTextField) {
                ((JTextField) field).setText("");
            } else if (field instanceof DatePicker) {
                ((DatePicker) field).clear();
            } else if (field instanceof JComboBox<?>) {
                ((JComboBox<?>) field).setSelectedIndex(0);
            }
            field.setBorder(UIManager.getBorder("TextField.border"));
        }
    }

    // Determine if a field is required
    private boolean isRequired(String header) {
    return header.equalsIgnoreCase("Employee #")
            || header.equalsIgnoreCase("Last Name")
            || header.equalsIgnoreCase("First Name")
            || header.equalsIgnoreCase("Birthday")
            || header.equalsIgnoreCase("Phone Number")
            || header.equalsIgnoreCase("Basic Salary") 
            || header.equalsIgnoreCase("Gross Semi-monthly Rate")
            || header.equalsIgnoreCase("SSS #")
            || header.equalsIgnoreCase("Philhealth #")
            || header.equalsIgnoreCase("TIN #")
            || header.equalsIgnoreCase("Pag-ibig #");
    }

    // Filter to allow only numeric input
    private static class NumericDocumentFilter extends DocumentFilter {
        private final int maxLength;
        
        public NumericDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }
        
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null && string.matches("\\d+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            int currentLength = fb.getDocument().getLength();
            if (text != null && text.matches("\\d+") && (currentLength - length + text.length() <= maxLength)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    // Ufor testing purpose and run standalone version of this panel
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            FileHandler fileHandler = new FileHandler();
//            fileHandler.readEmployeeFile();
//
//            JFrame frame = new JFrame("Add Employee Panel");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(600, 650);
//            frame.setLocationRelativeTo(null);
//            frame.add(new AddEmployeePanel(fileHandler, null));
//            frame.setVisible(true);
//        });
//    }
    private JFormattedTextField createFormattedField(String mask) {
        try {
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setPlaceholderCharacter('_'); 
            formatter.setAllowsInvalid(false);      
            formatter.setOverwriteMode(true);
            return new JFormattedTextField(formatter);
        
        } catch (java.text.ParseException e) {
            
            return new JFormattedTextField(); 
        }
    }
    private String formatCurrency(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            double amount = Double.parseDouble(value.replace(",", ""));
            return String.format("%,.2f", amount);
        } catch (NumberFormatException e) {
            return value;
        }
    }
    // Filter to allow only numbers and a single decimal point
    private static class MoneyDocumentFilter extends DocumentFilter {
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) 
                throws BadLocationException {

            String currentContent = fb.getDocument().getText(0, fb.getDocument().getLength());
            String result = currentContent.substring(0, offset) + text + currentContent.substring(offset + length);

            // Updated Regex: Allows digits, exactly one decimal point, and commas
            if (result.isEmpty() || result.matches("^[0-9,]*\\.?[0-9]*$")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) 
                throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }
    }
}
