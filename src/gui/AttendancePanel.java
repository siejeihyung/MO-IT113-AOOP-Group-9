package gui;

import model.FileHandler;

// Importing the FileHandler class from the model package
// Used to manage file operations like reading attendance data
import model.FileHandler;

// Swing components for building the user interface (e.g., JPanel, JTable, JScrollPane, etc.)
import javax.swing.*;

// Provides EmptyBorder for adding padding around components (e.g., spacing around panels)
import javax.swing.border.EmptyBorder;

// UI class to customize scrollbar appearance (colors, size, thumb style)
import javax.swing.plaf.basic.BasicScrollBarUI;

// Renderer class to customize how table cells are displayed (e.g., center alignment)
import javax.swing.table.DefaultTableCellRenderer;

// Table model class for managing and updating JTable data dynamically
import javax.swing.table.DefaultTableModel;

// Represents the table header component, which can also be customized
import javax.swing.table.JTableHeader;

// Imports core AWT classes used for UI layout, colors, fonts, etc.
import java.awt.*;

// Provides data structure to store dynamic list of arrays (e.g., attendance rows)
import java.util.ArrayList;

// Interface for creating strongly typed lists (e.g., List<String[]>)
import java.util.List;

public class AttendancePanel extends JPanel {
    // Instance of FileHandler to access attendance-related file operations
    private FileHandler fileHandler;

    // JTable to display attendance records in tabular form
    private JTable table;

    // Table model to control the structure and content of the JTable
    private DefaultTableModel model;

    // Text field for searching/filtering attendance data
    private JTextField searchField;

    // Holds all attendance data read from the file for reference and filtering
    private List<String[]> allData;

    // Gradient background colors
    private final Color gradientStart = new Color(255, 204, 229);
    private final Color gradientEnd = new Color(255, 229, 180);

    // Constructor initializes and sets up the panel
    public AttendancePanel(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Load attendance data
        fileHandler.readAttendanceFile();
        allData = fileHandler.getAttendanceData();

        // Define table columns
        String[] columnNames = {"Employee #", "Date", "Log In", "Log Out"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        // Table UI settings
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setShowGrid(true);
        table.setBorder(BorderFactory.createEmptyBorder());

        // Custom cell renderer (striped rows and center-aligned)
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        // Custom header styling
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new JTableHeaderRenderer());

        // Add scroll pane with custom scrollbar styling
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        // Add search panel and table to layout
        add(createSearchPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Populate the table with data
        populateTable(allData);
    }

    // Creates the top search bar panel
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);

        // Text input for search
        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        // Search button with consistent UI styling
        JButton searchButton = new JButton("Search");
        styleColoredButton(searchButton, new Color(30, 144, 255), 80, 36);
        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());

        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        return searchPanel;
    }

    // Filters attendance records based on Employee ID search
    private void search() {
        String input = searchField.getText().trim().toLowerCase();
        if (input.isEmpty()) {
            populateTable(allData);
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID.", "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Filter matching employee records
        List<String[]> filtered = new ArrayList<>();
        for (String[] row : allData) {
            if (row[0].toLowerCase().contains(input)) {
                filtered.add(row);
            }
        }

        if (filtered.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records found for: " + input, "No Results", JOptionPane.INFORMATION_MESSAGE);
        }

        // Update table view
        populateTable(filtered);
    }

    // Applies custom UI and styling to buttons
    private void styleColoredButton(JButton button, Color bgColor, int width, int height) {
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Custom paint for rounded, colored buttons
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

    // Fills the table with provided data
    private void populateTable(List<String[]> data) {
        model.setRowCount(0); // Clear table
        for (String[] row : data) {
            model.addRow(row);
        }
    }

    // Custom cell renderer for center alignment and striped row styling
    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255)); // Alternate row colors
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            c.setFont(new Font("SansSerif", Font.PLAIN, 13));
            return c;
        }
    }

    // Custom header renderer for table
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

    // Custom scrollbar UI for minimalist design
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = Color.WHITE;
            trackColor = new Color(0, 0, 0, 0); // Transparent track
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createInvisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createInvisibleButton();
        }

        // Removes default scrollbar arrows
        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }

    // Paints the background of the panel with a gradient
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
