package gui;

import service.LeaveService;
import dao.LeaveDAO;

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

/**
 * LeavePanel — Admin GUI for leave management.
 * Reason field removed as per team feedback — leave type is self-explanatory.
 * Row format: employeeId, leaveID, date, type, days, status
 */
public class LeavePanel extends JPanel {

    private final LeaveService leaveService;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    // ── Column indices (reason removed, status is now col 5) ─────────────────
    private static final int COL_EMP_ID   = 0;
    private static final int COL_LEAVE_ID = 1;
    private static final int COL_DATE     = 2;
    private static final int COL_TYPE     = 3;
    private static final int COL_DAYS     = 4;
    private static final int COL_STATUS   = 5;

    // ── Colors ────────────────────────────────────────────────────────────────
    private final Color HEADER_COLOR = new Color(20, 50, 110);
    private final Color ROW_ALT      = new Color(220, 230, 250);
    private final Color APPROVED_CLR = new Color(200, 240, 210);
    private final Color REJECTED_CLR = new Color(255, 210, 210);

    // ── Summary card labels ───────────────────────────────────────────────────
    private JLabel totalLabel, pendingLabel, approvedLabel, rejectedLabel;

    // ════════════════════════════════════════════════════════════════════════
    public LeavePanel(LeaveService leaveService) {
        this.leaveService = leaveService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        // ── Table — no Reason column ──────────────────────────────────────────
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

        add(buildTopPanel(),  BorderLayout.NORTH);
        add(tableCard,        BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);

        refreshTable();
    }

    // ── Backward-compatible constructor ───────────────────────────────────────
    public LeavePanel(model.FileHandler fileHandler) {
        this(new LeaveService(new LeaveDAO()));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Top panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Summary cards
        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cardsRow.setOpaque(false);
        cardsRow.add(makeSummaryCard("Total Requests", "📋", null));
        cardsRow.add(makeSummaryCard("Pending",        "⏳", "Pending"));
        cardsRow.add(makeSummaryCard("Approved",       "✅", "Approved"));
        cardsRow.add(makeSummaryCard("Rejected",       "❌", "Rejected"));

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);

        JLabel searchLbl = new JLabel("Search Employee ID:");
        searchLbl.setForeground(Color.WHITE);
        searchLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(180, 32));

        JButton searchBtn = makeButton("Search",      new Color(30, 144, 255), 90,  32);
        JButton clearBtn  = makeButton("Clear",       new Color(120, 120, 120), 70, 32);
        JButton fileBtn   = makeButton("+ File Leave", new Color(56, 142, 60), 120, 32);

        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshTable(); });
        fileBtn.addActionListener(e -> openFileLeaveDialog());

        searchRow.add(searchLbl);
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(clearBtn);
        searchRow.add(Box.createHorizontalStrut(20));
        searchRow.add(fileBtn);

        panel.add(cardsRow,  BorderLayout.NORTH);
        panel.add(searchRow, BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Action panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);

        JButton approveBtn = makeButton("✔ Approve", new Color(56, 142, 60),  130, 38);
        JButton rejectBtn  = makeButton("✘ Reject",  new Color(211, 47, 47),  110, 38);

        approveBtn.addActionListener(e -> updateSelectedStatus("Approved"));
        rejectBtn.addActionListener(e  -> updateSelectedStatus("Rejected"));

        panel.add(approveBtn);
        panel.add(rejectBtn);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  File Leave dialog — no Reason field
    // ════════════════════════════════════════════════════════════════════════
    private void openFileLeaveDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "File Leave Request", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField empIdField = new JTextField(15);
        JTextField dateField  = new JTextField("YYYY-MM-DD", 15);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Sick", "Vacation", "Emergency"});
        JSpinner daysSpinner  = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));

        // ── No Reason field ───────────────────────────────────────────────────
        Object[][] rows = {
            {"Employee ID *",    empIdField},
            {"Start Date *",     dateField},
            {"Leave Type *",     typeBox},
            {"Number of Days *", daysSpinner}
        };
        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            form.add(new JLabel((String) rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 1;
            form.add((Component) rows[i][1], gc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton submitBtn = makeButton("Submit", new Color(33, 150, 243), 90, 34);
        JButton cancelBtn = makeButton("Cancel", new Color(150, 150, 150), 80, 34);
        cancelBtn.addActionListener(e -> dialog.dispose());

        submitBtn.addActionListener(e -> {
            String empId  = empIdField.getText().trim();
            String dateStr = dateField.getText().trim();
            String type   = (String) typeBox.getSelectedItem();
            int    days   = (int) daysSpinner.getValue();

            if (empId.isEmpty() || dateStr.isEmpty() || dateStr.equals("YYYY-MM-DD")) {
                JOptionPane.showMessageDialog(dialog, "Employee ID and Date are required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate date;
            try { date = LocalDate.parse(dateStr); }
            catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Date must be YYYY-MM-DD format.",
                        "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check balance
            int balance = leaveService.getRemainingBalance(empId, type);
            if (days > balance) {
                JOptionPane.showMessageDialog(dialog,
                        "Not enough " + type + " leave balance.\nRemaining: " + balance + " day(s).",
                        "Insufficient Balance", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // ── No reason passed ──────────────────────────────────────────────
            boolean saved = leaveService.fileLeave(empId, date, type, days);
            if (saved) {
                refreshTable();
                JOptionPane.showMessageDialog(dialog, "Leave request filed successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "Failed to save. Check that the date is not in the past.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(submitBtn);
        btnPanel.add(cancelBtn);
        dialog.add(form,     BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Approve / Reject
    // ════════════════════════════════════════════════════════════════════════
    private void updateSelectedStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String currentStatus = (String) model.getValueAt(row, COL_STATUS);
        if (!"Pending".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Only Pending requests can be updated.",
                    "Already Processed", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String leaveId = (String) model.getValueAt(row, COL_LEAVE_ID);
        boolean ok = "Approved".equals(newStatus)
                ? leaveService.approveLeave(leaveId)
                : leaveService.rejectLeave(leaveId);

        if (ok) {
            model.setValueAt(newStatus, row, COL_STATUS);
            refreshSummaryCards();
            JOptionPane.showMessageDialog(this,
                    "Leave request " + newStatus.toLowerCase() + ".",
                    "Updated", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Search
    // ════════════════════════════════════════════════════════════════════════
    private void search() {
        String input = searchField.getText().trim().toLowerCase();
        if (input.isEmpty()) { refreshTable(); return; }

        List<String[]> filtered = new ArrayList<>();
        for (String[] r : leaveService.getAllLeaves())
            if (r[COL_EMP_ID].toLowerCase().contains(input)) filtered.add(r);

        if (filtered.isEmpty())
            JOptionPane.showMessageDialog(this, "No records found for: " + input,
                    "No Results", JOptionPane.INFORMATION_MESSAGE);

        model.setRowCount(0);
        for (String[] r : filtered) model.addRow(r);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════
    private void refreshTable() {
        model.setRowCount(0);
        for (String[] r : leaveService.getAllLeaves()) model.addRow(r);
        refreshSummaryCards();
    }

    private void refreshSummaryCards() {
        if (totalLabel    != null) totalLabel.setText(String.valueOf(leaveService.countByStatus(null)));
        if (pendingLabel  != null) pendingLabel.setText(String.valueOf(leaveService.countByStatus("Pending")));
        if (approvedLabel != null) approvedLabel.setText(String.valueOf(leaveService.countByStatus("Approved")));
        if (rejectedLabel != null) rejectedLabel.setText(String.valueOf(leaveService.countByStatus("Rejected")));
    }

    private JPanel makeSummaryCard(String title, String icon, String statusFilter) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 200, 240), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel titleLbl = new JLabel(icon + " " + title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel countLbl = new JLabel(String.valueOf(leaveService.countByStatus(statusFilter)));
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        countLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (title.contains("Total"))    totalLabel    = countLbl;
        if (title.contains("Pending"))  pendingLabel  = countLbl;
        if (title.contains("Approved")) approvedLabel = countLbl;
        if (title.contains("Rejected")) rejectedLabel = countLbl;

        card.add(titleLbl);
        card.add(countLbl);
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

    // ── Cell renderer ─────────────────────────────────────────────────────────
    private class LeaveCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            if (!sel) {
                String status = model.getRowCount() > row
                        ? (String) model.getValueAt(row, COL_STATUS) : "";
                if ("Approved".equals(status))      c.setBackground(APPROVED_CLR);
                else if ("Rejected".equals(status)) c.setBackground(REJECTED_CLR);
                else c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    // ── Scrollbar ─────────────────────────────────────────────────────────────
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(150, 180, 220);
            trackColor = Color.WHITE;
        }
        @Override protected JButton createDecreaseButton(int o) { return invisible(); }
        @Override protected JButton createIncreaseButton(int o) { return invisible(); }
        private JButton invisible() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}