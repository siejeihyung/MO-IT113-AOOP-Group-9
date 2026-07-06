/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package gui;

import dao.TicketDAO;
import model.Ticket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.List;

/**
 */
public class ITSupportDashboard extends JFrame {

    private JButton ticketListBtn;
    private JButton logoutBtn;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> rowSorter;
    
    // Summary Labels
    private JLabel totalLabel, openLabel, resolvedLabel;

    private static final String[] HEADERS = {"Ticket ID", "Sender", "Category", "Subject", "Status"};

    public ITSupportDashboard(String username) {
        setTitle("MotorPH — IT Support Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720); // Slightly taller for summary cards
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(950, 600));

        // Layout Components
        add(buildSidebar(username), BorderLayout.WEST);
        add(buildContentPanel(), BorderLayout.CENTER);

        // Functional Wiring
        logoutBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
        });

        refreshTicketTable();
        setVisible(true);
    }

    private JPanel buildSidebar(String username) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel profilePanel = new JPanel(new BorderLayout(10, 0));
        profilePanel.setBackground(Color.WHITE);
        profilePanel.add(new JLabel(loadIcon("/assets/userprofile.png", 40, 40)), BorderLayout.WEST);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel(username);
        JLabel roleLabel = new JLabel("IT Support Staff");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.GRAY);
        
        namePanel.add(nameLabel);
        namePanel.add(roleLabel);
        profilePanel.add(namePanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(new EmptyBorder(25, 0, 0, 0));

        JLabel generalLabel = new JLabel("General");
        generalLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        generalLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        generalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ticketListBtn = makeNavBtn("Support Tickets", "IT Support.png");
        logoutBtn = makeNavBtn("Log-out", "logout.png");
        logoutBtn.setForeground(Color.GRAY);

        navPanel.add(generalLabel);
        navPanel.add(ticketListBtn);

        sidebar.add(profilePanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(logoutBtn, BorderLayout.SOUTH);
        
        return sidebar;
    }

    private JPanel buildContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(new Color(29, 69, 143)); 
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- TOP HEADER: Title & Search ---
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        headerPanel.setOpaque(false);

        JPanel titleAndSearch = new JPanel(new BorderLayout());
        titleAndSearch.setOpaque(false);

        JLabel title = new JLabel("IT Ticket Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        searchField = new JTextField(15);
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                rowSorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
            }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        JLabel sLabel = new JLabel("Search: "); sLabel.setForeground(Color.WHITE);
        searchPanel.add(sLabel); searchPanel.add(searchField);

        titleAndSearch.add(title, BorderLayout.WEST);
        titleAndSearch.add(searchPanel, BorderLayout.EAST);

        // --- SUMMARY CARDS (Like Finance) ---
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        summaryPanel.setOpaque(false);
        totalLabel = createSummaryCard(summaryPanel, "Total Tickets");
        openLabel = createSummaryCard(summaryPanel, "Open/Pending");
        resolvedLabel = createSummaryCard(summaryPanel, "Resolved");

        headerPanel.add(titleAndSearch);
        headerPanel.add(summaryPanel);

        // --- TABLE SECTION ---
        tableModel = new DefaultTableModel(HEADERS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 215, 240), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // --- ACTION BUTTONS ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        actionPanel.setOpaque(false);

        JButton viewBtn = makeActionBtn("🔍 View Details", new Color(30, 144, 255));
        JButton updateBtn = makeActionBtn("✅ Update Status", new Color(56, 142, 60));

        viewBtn.addActionListener(e -> viewTicketDetails());
        updateBtn.addActionListener(e -> updateTicketStatus());

        actionPanel.add(viewBtn);
        actionPanel.add(updateBtn);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JLabel createSummaryCard(JPanel parent, String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(Color.GRAY);
        
        JLabel valLbl = new JLabel("0");
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(new Color(29, 69, 143));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        parent.add(card);
        return valLbl;
    }

    private void viewTicketDetails() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int modelRow = table.convertRowIndexToModel(row);
            String msg = "Ticket ID: " + tableModel.getValueAt(modelRow, 0) +
                         "\nFrom: " + tableModel.getValueAt(modelRow, 1) +
                         "\nCategory: " + tableModel.getValueAt(modelRow, 2) +
                         "\nSubject: " + tableModel.getValueAt(modelRow, 3) +
                         "\nStatus: " + tableModel.getValueAt(modelRow, 4);
            JOptionPane.showMessageDialog(this, msg, "Ticket Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Select a ticket first.");
        }
    }

    private void updateTicketStatus() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int modelRow = table.convertRowIndexToModel(row);
            String ticketID = tableModel.getValueAt(modelRow, 0).toString();
            String[] statuses = {"Open", "In Progress", "Resolved", "Closed"};
            
            String newStatus = (String) JOptionPane.showInputDialog(this, "Select Status:", 
                "Update Ticket #" + ticketID, JOptionPane.QUESTION_MESSAGE, null, statuses, tableModel.getValueAt(modelRow, 4));

            if (newStatus != null) {
                TicketDAO dao = new TicketDAO();
                if (dao.updateTicketStatus(ticketID, newStatus)) { // Now calling the DAO update
                    refreshTicketTable();
                    JOptionPane.showMessageDialog(this, "Status updated successfully.");
                }
            }
        }
    }

    private void refreshTicketTable() {
        tableModel.setRowCount(0);
        TicketDAO dao = new TicketDAO();
        List<Ticket> tickets = dao.getAllTickets();
        
        int openCount = 0;
        int resolvedCount = 0;

        for (Ticket t : tickets) {
            tableModel.addRow(new Object[]{
                t.getTicketID(), t.getSenderName(), t.getCategory(), t.getSubject(), t.getStatus()
            });
            if (t.getStatus().equalsIgnoreCase("Open") || t.getStatus().equalsIgnoreCase("In Progress")) openCount++;
            if (t.getStatus().equalsIgnoreCase("Resolved")) resolvedCount++;
        }
        
        // Update summary cards
        totalLabel.setText(String.valueOf(tickets.size()));
        openLabel.setText(String.valueOf(openCount));
        resolvedLabel.setText(String.valueOf(resolvedCount));
    }

    // --- (Keep makeNavBtn, makeActionBtn, styleTable, and loadIcon exactly as you had them) ---
    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(100, 149, 237));
        table.getTableHeader().setBackground(new Color(20, 50, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(new Color(230, 230, 230));
        table.setShowVerticalLines(false);
    }

    private JButton makeNavBtn(String text, String icon) {
        JButton btn = new JButton(text);
        btn.setIcon(loadIcon("/assets/" + icon, 20, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(240, 240, 240)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 40));
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }
}