package gui;

// Importing the FileHandler class from the 'model' package
// This class is likely used to read/write employee or attendance data from files
import model.FileHandler;

// Importing core Swing components for building the GUI
import javax.swing.*;

// Importing custom scrollbar UI from the basic look and feel package
// Allows customization of the appearance and behavior of scrollbars
import javax.swing.plaf.basic.BasicScrollBarUI;

// Importing default cell renderer to allow customization of how table cells are displayed
import javax.swing.table.DefaultTableCellRenderer;

// Importing table model class to manage the data of a JTable
// Used to populate, update, and control how the table handles data
import javax.swing.table.DefaultTableModel;

// Importing AWT classes for GUI layout, colors, fonts, and event handling
import java.awt.*;

// Importing ActionEvent to handle button or component action events
import java.awt.event.ActionEvent;

// Importing ArrayList and List for storing dynamic lists of data (e.g., employee records)
import java.util.ArrayList;
import java.util.List;

// Importing Vector for table data model (commonly used in Swing table structures)
import java.util.Vector;


public class EmployeeTable extends JPanel {

    // JTable for displaying employee data
    private JTable table;

    // Model to manage table data
    private DefaultTableModel model;

    // File handler to read/write employee data from/to file
    private FileHandler fileHandler;

    // List to store employee data rows from file
    private List<String[]> employeeData;

    // Column names to display in the JTable
    private final String[] columnNames = {
        "Employee ID", "Last Name", "First Name",
        "SSS No.", "PhilHealth No.", "TIN", "Pag-IBIG No."
    };

    // Indices of data fields to show (used to extract relevant data from each row)
    private final int[] indices = {0, 1, 2, 6, 7, 8, 9};

    // Gradient background colors
    private final Color gradientStart = new Color(29, 69, 143); 
    private final Color gradientEnd = new Color(29, 69, 143);

    public EmployeeTable(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        setLayout(new BorderLayout());

        try {
            // Load employee data from file
            fileHandler.readEmployeeFile();
            employeeData = fileHandler.getEmployeeData();
        } catch (Exception e) {
            // Show error message if loading fails
            JOptionPane.showMessageDialog(this, "Failed to load employee data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Initialize table model with column headers
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        // Set table appearance
        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setShowGrid(true);
        table.setRowHeight(25);
        table.setBorder(BorderFactory.createEmptyBorder());

        // Custom cell renderer for styling rows
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        // Set custom header renderer
        JTableHeaderRenderer headerRenderer = new JTableHeaderRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Wrap table in scroll pane and apply custom scrollbar UI
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        // Add scroll pane to panel
        add(scrollPane, BorderLayout.CENTER);

        // Populate table with employee data
        refreshTable(employeeData);

        // View Employee button GUI is removed but logic remains
    }

    // Getter for JTable (useful for other classes to access table)
    public JTable getTable() {
        return table;
    }

    // Custom header cell renderer for JTable
    private static class JTableHeaderRenderer extends DefaultTableCellRenderer {
        public JTableHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }

    // Custom cell renderer for dynamic row coloring and font adjustment
    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Alternating row colors
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            }

            // Adjust font size based on column type and window width
            String colName = table.getColumnName(column).toLowerCase();
            boolean isNumberField = colName.contains("sss") || colName.contains("phil")
                    || colName.contains("tin") || colName.contains("pag");
            int width = EmployeeTable.this.getWidth();
            int fontSize = width < 600 ? 10 : (width < 800 ? 12 : 14);
            c.setFont(new Font("SansSerif", Font.PLAIN, isNumberField ? fontSize : 14));

            return c;
        }
    }

    // Custom scroll bar UI with invisible buttons
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = Color.WHITE;
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    // Override to paint gradient background on the panel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // Add a new employee to both memory and file, and refresh the table
    public boolean addEmployee(String[] newEmployee) {
        if (newEmployee == null || newEmployee.length == 0) return false;

        // Check for duplicate Employee ID
        String newId = newEmployee[0].trim();
        for (String[] existing : employeeData) {
            if (existing.length > 0 && existing[0].trim().equals(newId)) {
                return false;
            }
        }

        // Append to in-memory list
        employeeData.add(newEmployee);
        try {
            // Save to file
            fileHandler.appendEmployeeToFile(newEmployee);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save employee to file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Refresh table display
        refreshTable(employeeData);
        return true;
    }

    // Filters table rows by search query
    public void filterTable(String query) {
        if (query == null || query.isEmpty()) {
            refreshTable(employeeData);
            return;
        }

        List<String[]> filtered = new ArrayList<>();
        for (String[] row : employeeData) {
            for (int i : indices) {
                if (i < row.length && row[i].toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(row);
                    break;
                }
            }
        }

        refreshTable(filtered);
    }

    // Updates the table model with new data rows
    public final void refreshTable(List<String[]> rows) {
        model.setRowCount(0); // Clear current data
        for (String[] row : rows) {
            if (row.length >= 10) {
                String[] displayRow = new String[indices.length];
                for (int i = 0; i < indices.length; i++) {
                    displayRow[i] = row[indices[i]].trim();
                }
                model.addRow(displayRow);
            }
        }
    }

    // === Logic for "View Employee" kept without GUI ===

    // Triggers detail view of selected employee (if implemented in UI)
    private void handleShowDetails(ActionEvent e) {
        Vector<Object> selected = getSelectedEmployeeFullDetails();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an employee first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showDetailDialog(selected);
    }

    // Retrieves full details of selected employee based on selected row
    public Vector<Object> getSelectedEmployeeFullDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) return null;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String employeeId = (String) table.getModel().getValueAt(modelRow, 0);

        // Look through the file data to get all columns (not just the ones in the table)
        for (String[] employee : employeeData) {
            if (employee.length > 0 && employee[0].equals(employeeId)) {
                Vector<Object> fullData = new Vector<>();
                for (String item : employee) {
                    fullData.add(item);
                }
                return fullData;
            }
        }
        return null;
    }

    // Displays a dialog showing all employee fields and their values
    private void showDetailDialog(Vector<Object> row) {
        String[] headers = fileHandler.getEmployeeHeaders();
        JPanel detailPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        for (int i = 0; i < headers.length && i < row.size(); i++) {
            detailPanel.add(new JLabel(headers[i] + ": " + row.get(i)));
        }

        JScrollPane scrollPane = new JScrollPane(detailPanel);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "Employee Full Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
