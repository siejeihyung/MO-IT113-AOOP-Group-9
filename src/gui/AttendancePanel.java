package gui;

import service.AttendanceService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends JPanel {

    private final AttendanceService attendanceService;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private final Color gradientStart = new Color(255, 204, 229);
    private final Color gradientEnd = new Color(255, 229, 180);

    public AttendancePanel(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] columnNames = {"Employee #", "Date", "Log In", "Log Out"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        table.setRowHeight(25);
        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setDefaultRenderer(Object.class, new ResponsiveCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new JTableHeaderRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        add(createSearchPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        populateTable(attendanceService.getAllAttendance());
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(180, 30));
        searchField.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        JButton searchButton = new JButton("Search");
        styleColoredButton(searchButton, new Color(30, 144, 255), 80, 36);
        searchButton.addActionListener(e -> search());
        searchField.addActionListener(e -> search());

        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    private void search() {
        String input = searchField.getText().trim().toLowerCase();
        if (input.isEmpty()) {
            populateTable(attendanceService.getAllAttendance());
            return;
        }

        List<String[]> filtered = new ArrayList<>();
        for (String[] row : attendanceService.getAllAttendance()) {
            if (row[0].toLowerCase().contains(input)) {
                filtered.add(row);
            }
        }
        populateTable(filtered);
    }

    private void populateTable(List<String[]> data) {
        model.setRowCount(0);
        for (String[] row : data) {
            model.addRow(row);
        }
    }

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
    }

    private class ResponsiveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 240, 255));
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            c.setFont(new Font("SansSerif", Font.PLAIN, 13));
            return c;
        }
    }

    private static class JTableHeaderRenderer extends DefaultTableCellRenderer {
        public JTableHeaderRenderer() {
            setOpaque(true);
            setBackground(new Color(33, 150, 243));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return this;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = Color.WHITE;
            trackColor = new Color(0, 0, 0, 0);
        }
        @Override
        protected JButton createDecreaseButton(int orientation) { return createInvisibleButton(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return createInvisibleButton(); }
        private JButton createInvisibleButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, gradientStart, 0, getHeight(), gradientEnd);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}