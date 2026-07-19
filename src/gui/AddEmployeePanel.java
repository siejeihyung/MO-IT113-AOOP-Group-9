package gui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import service.EmployeeService; 
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;

public class AddEmployeePanel extends JPanel {

    private final EmployeeService employeeService; 
    private final Map<String, JComponent> fieldMap = new LinkedHashMap<>();
    private final JButton submitButton = new JButton("Add Employee");
    private final JButton backButton = new JButton("Back");
    private final Runnable onEmployeeAdded;

    private final String[] fields = {"Employee #", "Last Name", "First Name", "Birthday", "Phone Number", 
                                     "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", "Status", "Position", 
                                     "Basic Salary", "Gross Semi-monthly Rate"};

    public AddEmployeePanel(EmployeeService employeeService, Runnable onEmployeeAdded) {
        this.employeeService = employeeService;
        this.onEmployeeAdded = onEmployeeAdded;

        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setOpaque(false);

        for (String header : fields) {
            boolean required = isRequired(header);
            JPanel labelPanel = new JPanel(new BorderLayout());
            labelPanel.setOpaque(false);
            JLabel label = new JLabel(header + ":");
            
            if (required) {
                JLabel asterisk = new JLabel("*");
                asterisk.setForeground(Color.RED);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
                labelPanel.add(label, BorderLayout.WEST);
                labelPanel.add(asterisk, BorderLayout.EAST);
            } else {
                label.setForeground(Color.GRAY);
                labelPanel.add(label, BorderLayout.WEST);
            }

            JComponent inputField;
            switch (header.toLowerCase()) {
                case "employee #" -> {
                    JTextField field = new JTextField();
                    ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumericDocumentFilter(7));
                    inputField = field; 
                }
                case "birthday" -> {
                    DatePickerSettings settings = new DatePickerSettings();
                    settings.setFormatForDatesCommonEra("yyyy/MM/dd");
                    inputField = new DatePicker(settings);
                }
                case "status" -> inputField = new JComboBox<>(new String[]{"Regular", "Probationary"});
                case "position" -> inputField = new JComboBox<>(new String[]{"Staff", "HR Manager", "Finance Manager", "IT Support", "CEO"});
                default -> inputField = new JTextField();
            }
            formPanel.add(labelPanel);
            formPanel.add(inputField);
            fieldMap.put(header, inputField);
        }

        submitButton.addActionListener(this::addEmployee);
        backButton.addActionListener(e -> { if (onEmployeeAdded != null) onEmployeeAdded.run(); });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.add(backButton);
        bottomPanel.add(submitButton);

        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEmployee(ActionEvent e) {
        String[] newRow = new String[fieldMap.size()];
        int index = 0;
        
        for (Map.Entry<String, JComponent> entry : fieldMap.entrySet()) {
            JComponent comp = entry.getValue();
            String val = (comp instanceof JTextField tf) ? tf.getText().trim() :
                         (comp instanceof DatePicker dp && dp.getDate() != null) ? dp.getDate().toString() :
                         (comp instanceof JComboBox<?> cb) ? cb.getSelectedItem().toString() : "";
            newRow[index++] = val;
        }

        if (employeeService.addEmployee(newRow)) {
            JOptionPane.showMessageDialog(this, "✅ Employee added successfully!");
            clearFields();
            if (onEmployeeAdded != null) onEmployeeAdded.run();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error saving to database.");
        }
    }

    private boolean isRequired(String header) { return true; } // Simplified for logic
    
    private void clearFields() {
        for (JComponent f : fieldMap.values()) {
            if (f instanceof JTextField tf) tf.setText("");
            else if (f instanceof DatePicker dp) dp.clear();
        }
    }

    private static class NumericDocumentFilter extends DocumentFilter {
        private final int maxLength;
        public NumericDocumentFilter(int maxLength) { this.maxLength = maxLength; }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && text.matches("\\d+") && (fb.getDocument().getLength() - length + text.length() <= maxLength))
                super.replace(fb, offset, length, text, attrs);
        }
    }
}