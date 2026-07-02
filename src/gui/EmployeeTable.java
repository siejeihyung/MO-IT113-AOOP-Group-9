package gui;

import service.EmployeeService;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class EmployeeTable extends JPanel {

    private final JTable table;
    private final DefaultTableModel model;
    private final EmployeeService employeeService;

    private final String[] columnNames = {
        "Employee ID", "Last Name", "First Name",
        "SSS No.", "PhilHealth No.", "TIN", "Pag-IBIG No."
    };

    private final Color gradientStart = new Color(29, 69, 143);
    private final Color gradientEnd = new Color(20, 50, 110);

    public EmployeeTable(EmployeeService employeeService) {
        this.employeeService = employeeService;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setShowGrid(true);
        table.setRowHeight(25);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        add(scrollPane, BorderLayout.CENTER);

        // Fetch data from Database via Service
        refreshTable(employeeService.getAllEmployees());
    }

    public void refreshTable(List<String[]> data) {
        model.setRowCount(0);
        for (String[] row : data) {
            // Mapping: row[0]=ID, row[1]=Last, row[2]=First, row[6]=SSS, row[7]=Phil, row[8]=TIN, row[9]=Pag
            if (row.length >= 10) {
                model.addRow(new Object[]{row[0], row[1], row[2], row[6], row[7], row[8], row[9]});
            }
        }
    }

    public JTable getTable() { return table; }

    public Vector<Object> getSelectedEmployeeFullDetails() {
        int row = table.getSelectedRow();
        if (row == -1) return null;
        
        String employeeId = (String) table.getValueAt(row, 0);
        String[] fullData = employeeService.getEmployeeById(employeeId);
        
        if (fullData == null) return null;
        
        Vector<Object> details = new Vector<>();
        for (String item : fullData) details.add(item);
        return details;
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = Color.WHITE;
            trackColor = new Color(0, 0, 0, 0);
        }
        @Override protected JButton createDecreaseButton(int orientation) { return createInvisibleButton(); }
        @Override protected JButton createIncreaseButton(int orientation) { return createInvisibleButton(); }
        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
    }

    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}