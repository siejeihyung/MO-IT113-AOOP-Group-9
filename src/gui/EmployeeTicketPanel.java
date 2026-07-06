/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui;

import dao.TicketDAO;
import dao.EmployeeDAO;
import service.EmployeeService;
import model.Ticket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
/**
 *
 * @author anton
 * EmployeeTicketPanel — shown inside EmployeeDashboardPanel.
 * Employees can submit IT support tickets and view their own ticket history.
 */
public class EmployeeTicketPanel extends JPanel {

    private final String       employeeId;
    private final String       employeeName; // ── Use name as sender, not ID
    private final TicketDAO    ticketDAO = new TicketDAO();
    private DefaultTableModel  tableModel;

    private static final String[] HEADERS = {"Ticket ID", "Category", "Subject", "Status"};

    public EmployeeTicketPanel(String employeeId) {
        this.employeeId = employeeId;
        // ── Resolve actual name so sender shows name, not ID ──────────────────
        EmployeeService empService = new EmployeeService(new EmployeeDAO());
        this.employeeName = empService.getEmployeeName(employeeId);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setOpaque(false);

        add(buildTopSection(),   BorderLayout.NORTH);
        add(buildTableSection(), BorderLayout.CENTER);
        add(buildActionPanel(),  BorderLayout.SOUTH);

        refreshTable();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Top section
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTopSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel title = new JLabel("My IT Support Tickets");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        panel.add(title, BorderLayout.WEST);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Table section
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTableSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        tableModel = new DefaultTableModel(HEADERS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(100, 149, 237));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.getTableHeader().setBackground(new Color(20, 50, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 240), 1, true));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(scroll, BorderLayout.CENTER);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Action panel
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);

        JButton submitBtn = makeButton("+ Submit Ticket", new Color(56, 142, 60), 150, 38);
        JButton refreshBtn = makeButton("↻ Refresh",      new Color(30, 144, 255), 110, 38);

        submitBtn.addActionListener(e  -> openSubmitDialog());
        refreshBtn.addActionListener(e -> refreshTable());

        panel.add(submitBtn);
        panel.add(refreshBtn);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Submit Ticket Dialog
    // ════════════════════════════════════════════════════════════════════════
    private void openSubmitDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Submit IT Support Ticket", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
            "Hardware", "Software", "Network", "Account Access", "Other"
        });
        JTextField subjectField     = new JTextField(20);
        JTextArea  descriptionArea  = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);

        Object[][] rows = {
            {"Category *",     categoryBox},
            {"Subject *",      subjectField},
            {"Description",    descScroll}
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            form.add(new JLabel((String) rows[i][0]), gc);
            gc.gridx = 1; gc.weightx = 1;
            form.add((Component) rows[i][1], gc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        JButton submitBtn = makeButton("Submit",  new Color(33, 150, 243), 90, 34);
        JButton cancelBtn = makeButton("Cancel",  new Color(150, 150, 150), 80, 34);
        cancelBtn.addActionListener(e -> dialog.dispose());

        submitBtn.addActionListener(e -> {
            String category    = (String) categoryBox.getSelectedItem();
            String subject     = subjectField.getText().trim();
            String description = descriptionArea.getText().trim();

            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Subject is required.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean saved = ticketDAO.submitTicket(employeeName, category, subject, description);
            if (saved) {
                refreshTable();
                JOptionPane.showMessageDialog(dialog,
                        "Ticket submitted successfully!\nOur IT team will get back to you soon. 😊",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to submit ticket. Please try again.",
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
    //  Refresh table — shows only THIS employee's tickets
    // ════════════════════════════════════════════════════════════════════════
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Ticket> tickets = ticketDAO.getTicketsByEmployee(employeeName);
        for (Ticket t : tickets) {
            tableModel.addRow(new Object[]{
                t.getTicketID(), t.getCategory(), t.getSubject(), t.getStatus()
            });
        }
    }

    // ── Button factory ────────────────────────────────────────────────────────
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
}