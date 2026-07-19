package gui;

import service.LeaveService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ArrayList;

public class LeavePanel extends JPanel {

    private final LeaveService leaveService;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private static final int COL_EMP_ID   = 0;
    private static final int COL_LEAVE_ID = 1;
    private static final int COL_STATUS   = 5;

    private final Color HEADER_COLOR = new Color(20, 50, 110);
    private final Color ROW_ALT      = new Color(220, 230, 250);
    private final Color APPROVED_CLR = new Color(200, 240, 210);
    private final Color REJECTED_CLR = new Color(255, 210, 210);

    private JLabel totalLabel, pendingLabel, approvedLabel, rejectedLabel;

    public LeavePanel(LeaveService leaveService) {
        this.leaveService = leaveService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        String[] columns = {"Employee #", "Leave ID", "Date", "Type", "Days", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 149, 237));
        table.setSelectionForeground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new LeaveCellRenderer());

        table.getTableHeader().setBackground(HEADER_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true),
            new EmptyBorder(5, 5, 5, 5)
        ));
        tableCard.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(makeSummaryCard("Total Requests", "📋", null));
        cardsRow.add(makeSummaryCard("Pending", "⏳", "Pending"));
        cardsRow.add(makeSummaryCard("Approved", "✅", "Approved"));
        cardsRow.add(makeSummaryCard("Rejected", "❌", "Rejected"));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(180, 32));
        JButton searchBtn = makeButton("Search", new Color(30, 144, 255), 90, 32);
        JButton clearBtn = makeButton("Clear", new Color(120, 120, 120), 70, 32);
        JButton fileBtn = makeButton("+ File Leave", new Color(56, 142, 60), 120, 32);

        searchBtn.addActionListener(e -> search());
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshTable(); });
        
        searchRow.add(new JLabel("Search Employee ID:") {{ setForeground(Color.WHITE); }});
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(clearBtn);
        searchRow.add(Box.createHorizontalStrut(20));
        searchRow.add(fileBtn);

        panel.add(cardsRow, BorderLayout.NORTH);
        panel.add(searchRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);
        JButton approveBtn = makeButton("✔ Approve", new Color(56, 142, 60), 130, 38);
        JButton rejectBtn = makeButton("✘ Reject", new Color(211, 47, 47), 110, 38);
        approveBtn.addActionListener(e -> updateSelectedStatus("Approved"));
        rejectBtn.addActionListener(e -> updateSelectedStatus("Rejected"));
        panel.add(approveBtn);
        panel.add(rejectBtn);
        return panel;
    }

    private void updateSelectedStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        String leaveId = (String) model.getValueAt(row, COL_LEAVE_ID);
        boolean ok = "Approved".equals(newStatus) ? leaveService.approveLeave(leaveId) : leaveService.rejectLeave(leaveId);
        if (ok) {
            model.setValueAt(newStatus, row, COL_STATUS);
            refreshSummaryCards();
        }
    }

    private void search() {
        String input = searchField.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (String[] r : leaveService.getAllLeaves()) {
            if (r[COL_EMP_ID].toLowerCase().contains(input)) model.addRow(r);
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        for (String[] r : leaveService.getAllLeaves()) model.addRow(r);
        refreshSummaryCards();
    }

    private void refreshSummaryCards() {
        if (totalLabel != null) totalLabel.setText(String.valueOf(leaveService.countByStatus(null)));
        if (pendingLabel != null) pendingLabel.setText(String.valueOf(leaveService.countByStatus("Pending")));
        if (approvedLabel != null) approvedLabel.setText(String.valueOf(leaveService.countByStatus("Approved")));
        if (rejectedLabel != null) rejectedLabel.setText(String.valueOf(leaveService.countByStatus("Rejected")));
    }

    private JPanel makeSummaryCard(String title, String icon, String statusFilter) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(180, 200, 240), 1, true), new EmptyBorder(8, 16, 8, 16)));
        JLabel titleLbl = new JLabel(icon + " " + title) {{ setFont(new Font("Segoe UI", Font.PLAIN, 11)); setAlignmentX(Component.CENTER_ALIGNMENT); }};
        JLabel countLbl = new JLabel(String.valueOf(leaveService.countByStatus(statusFilter))) {{ setFont(new Font("Segoe UI", Font.BOLD, 20)); setAlignmentX(Component.CENTER_ALIGNMENT); }};
        if (title.contains("Total")) totalLabel = countLbl;
        if (title.contains("Pending")) pendingLabel = countLbl;
        if (title.contains("Approved")) approvedLabel = countLbl;
        if (title.contains("Rejected")) rejectedLabel = countLbl;
        card.add(titleLbl); card.add(countLbl);
        return card;
    }

    private JButton makeButton(String text, Color bg, int w, int h) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    private class LeaveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            if (!sel) {
                String status = (String) model.getValueAt(row, COL_STATUS);
                if ("Approved".equals(status)) c.setBackground(APPROVED_CLR);
                else if ("Rejected".equals(status)) c.setBackground(REJECTED_CLR);
                else c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(150, 180, 220); trackColor = Color.WHITE; }
        @Override protected JButton createDecreaseButton(int o) { return invisible(); }
        @Override protected JButton createIncreaseButton(int o) { return invisible(); }
        private JButton invisible() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
    }
}