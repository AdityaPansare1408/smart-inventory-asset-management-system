package com.inventory.ui;

import com.inventory.model.Asset;
import com.inventory.model.Item;
import com.inventory.service.AssetService;
import com.inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class DashboardUI extends JFrame {
    private InventoryService inventoryService;
    private AssetService assetService;
    private JPanel workspacePanel;
    private CardLayout cardLayout;

    // Tables
    private JTable inventoryTable, assetTable, restockTable, healthTable;
    private DefaultTableModel inventoryModel, assetModel, restockModel, healthModel;
    private TableRowSorter<DefaultTableModel> invSorter, astSorter;

    // Action Buttons (Stored for State Management)
    private JButton btnUpdateInv, btnDeleteInv, btnUpdateAst, btnDeleteAst;

    // Dashboard Cards & Analytics Labels
    private JLabel lblItemsCount, lblAssetsCount, lblLowStock, lblTotalValue;
    private JLabel lblTotalInvValue, lblHighestPriced, lblMostValuable, lblLowestStock, lblAvgPrice, lblCategories;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DashboardUI() {
        inventoryService = new InventoryService();
        assetService = new AssetService();

        setTitle("Smart Inventory & Asset Management System - V1");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createNorthPanel();
        createWorkspace();
        createSidebar();

        setInventoryViewMode(); // Default State
        refreshAllData();
    }

    // --- UI STATE MANAGEMENT ---

    private void setInventoryViewMode() {
        if(btnUpdateInv != null) {
            btnUpdateInv.setEnabled(true); btnDeleteInv.setEnabled(true);
            btnUpdateAst.setEnabled(false); btnDeleteAst.setEnabled(false);
            cardLayout.show(workspacePanel, "Inventory");
        }
    }

    private void setAssetViewMode() {
        if(btnUpdateAst != null) {
            btnUpdateInv.setEnabled(false); btnDeleteInv.setEnabled(false);
            btnUpdateAst.setEnabled(true); btnDeleteAst.setEnabled(true);
            cardLayout.show(workspacePanel, "Assets");
        }
    }

    private void setReportViewMode(String cardName) {
        if(btnUpdateInv != null) {
            btnUpdateInv.setEnabled(false); btnDeleteInv.setEnabled(false);
            btnUpdateAst.setEnabled(false); btnDeleteAst.setEnabled(false);
            cardLayout.show(workspacePanel, cardName);
        }
    }

    // --- UI RENDERERS ---

    class CurrencyRenderer extends DefaultTableCellRenderer {
        public CurrencyRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
        @Override
        public void setValue(Object value) {
            if (value instanceof Double || value instanceof Integer) setText(currencyFormat.format(value));
            else super.setValue(value);
        }
    }

    class RightAlignRenderer extends DefaultTableCellRenderer {
        public RightAlignRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
    }

    class HealthStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            if (status.contains("Excellent")) { setText("🟢 " + status); setForeground(new Color(39, 174, 96)); }
            else if (status.contains("Good") || status.contains("On Schedule")) { setText("🟡 " + status); setForeground(new Color(241, 196, 15)); }
            else if (status.contains("Fair") || status.contains("Due Soon")) { setText("🟠 " + status); setForeground(new Color(230, 126, 34)); }
            else { setText("🔴 " + status); setForeground(new Color(192, 57, 43)); }
            if (isSelected) setForeground(Color.WHITE);
            return c;
        }
    }

    class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = value != null ? value.toString() : "";
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            if (status.equals("Critical")) setForeground(new Color(192, 57, 43));
            else if (status.equals("High")) setForeground(new Color(230, 126, 34));
            else if (status.equals("Medium")) setForeground(new Color(241, 196, 15));
            else setForeground(new Color(41, 128, 185));
            if (isSelected) setForeground(Color.WHITE);
            return c;
        }
    }

    // --- BOILERPLATE UI BUILDERS ---

    private void createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(44, 62, 80));

        JLabel titleLabel = new JLabel(" Workspace Dashboard", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(15, 15, 10, 15));

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(10, 15, 15, 15));

        lblItemsCount = createDashboardCard("📦 Inventory Items");
        lblAssetsCount = createDashboardCard("💻 Asset Count");
        lblLowStock = createDashboardCard("⚠ Low Stock Count");
        lblTotalValue = createDashboardCard("📊 Total Value");

        cardsPanel.add(lblItemsCount); cardsPanel.add(lblAssetsCount);
        cardsPanel.add(lblLowStock); cardsPanel.add(lblTotalValue);

        northPanel.add(titleLabel, BorderLayout.NORTH);
        northPanel.add(cardsPanel, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);
    }

    private JLabel createDashboardCard(String title) {
        JLabel label = new JLabel(title + ": 0", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94), 2, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        label.setOpaque(true); label.setBackground(new Color(52, 73, 94));
        return label;
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(236, 240, 241));
        sidebar.setBorder(new EmptyBorder(15, 15, 15, 15));

        sidebar.add(createSidebarHeader("INVENTORY MANAGEMENT"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createSidebarButton("📦 View Inventory", this::setInventoryViewMode));
        sidebar.add(createSidebarButton("➕ Add Item", this::showAddItemDialog));
        btnUpdateInv = createSidebarButton("✏ Update Item", this::showUpdateItemDialog); sidebar.add(btnUpdateInv);
        btnDeleteInv = createSidebarButton("🗑 Delete Item", this::showDeleteItemDialog); sidebar.add(btnDeleteInv);

        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarHeader("ASSET MANAGEMENT"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createSidebarButton("💻 View Assets", this::setAssetViewMode));
        sidebar.add(createSidebarButton("➕ Add Asset", this::showAddAssetDialog));
        btnUpdateAst = createSidebarButton("✏ Update Asset", this::showUpdateAssetDialog); sidebar.add(btnUpdateAst);
        btnDeleteAst = createSidebarButton("🗑 Delete Asset", this::showDeleteAssetDialog); sidebar.add(btnDeleteAst);

        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(createSidebarHeader("INTELLIGENCE & REPORTS"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createSidebarButton("📊 Inventory Analytics", () -> setReportViewMode("Analytics")));
        sidebar.add(createSidebarButton("⚠ Restock Priority", () -> setReportViewMode("Restock")));
        sidebar.add(createSidebarButton("❤ Asset Health Engine", () -> setReportViewMode("Health")));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarButton("🚪 Exit System", () -> System.exit(0)));

        add(sidebar, BorderLayout.WEST);
    }

    private JLabel createSidebarHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11)); label.setForeground(new Color(127, 140, 141));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(200, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false); button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }

    private void createWorkspace() {
        cardLayout = new CardLayout();
        workspacePanel = new JPanel(cardLayout);
        workspacePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        workspacePanel.setBackground(Color.WHITE);

        workspacePanel.add(buildInventoryView(), "Inventory");
        workspacePanel.add(buildAssetView(), "Assets");
        workspacePanel.add(buildAnalyticsView(), "Analytics");
        workspacePanel.add(buildRestockView(), "Restock");
        workspacePanel.add(buildHealthView(), "Health");
        add(workspacePanel, BorderLayout.CENTER);
    }

    private JPanel buildInventoryView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        JTextField searchField = new JTextField(25); searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
            private void search() {
                String t = searchField.getText();
                invSorter.setRowFilter(t.trim().length() == 0 ? null : RowFilter.regexFilter("(?i)" + t));
            }
        });
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("🔍 Search Inventory:")); searchPanel.add(searchField);

        String[] cols = {"Item ID", "Name", "Category", "Qty", "Price", "Min Stock"};
        inventoryModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        inventoryTable = styleTable(new JTable(inventoryModel));

        inventoryTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());
        inventoryTable.getColumnModel().getColumn(5).setCellRenderer(new RightAlignRenderer());

        invSorter = new TableRowSorter<>(inventoryModel); inventoryTable.setRowSorter(invSorter);
        panel.add(searchPanel, BorderLayout.NORTH); panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAssetView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        JTextField searchField = new JTextField(25); searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
            private void search() {
                String t = searchField.getText();
                astSorter.setRowFilter(t.trim().length() == 0 ? null : RowFilter.regexFilter("(?i)" + t));
            }
        });
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("🔍 Search Assets:")); searchPanel.add(searchField);

        String[] cols = {"Asset ID", "Name", "Purchased", "Last Maint.", "Cost", "Condition", "Score", "Status"};
        assetModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        assetTable = styleTable(new JTable(assetModel));

        assetTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());
        assetTable.getColumnModel().getColumn(7).setCellRenderer(new HealthStatusRenderer());

        astSorter = new TableRowSorter<>(assetModel); assetTable.setRowSorter(astSorter);
        panel.add(searchPanel, BorderLayout.NORTH); panel.add(new JScrollPane(assetTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAnalyticsView() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 20, 20));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Strategic Inventory Analytics", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 18)));
        panel.setOpaque(false);

        lblTotalInvValue = createAnalyticsLabel("Total Portfolio Value", "₹0.00");
        lblHighestPriced = createAnalyticsLabel("Premium Item", "N/A");
        lblMostValuable = createAnalyticsLabel("Most Valuable Asset Class", "N/A");
        lblLowestStock = createAnalyticsLabel("Critical Stock Warning", "N/A");
        lblAvgPrice = createAnalyticsLabel("Average Item Price", "N/A");
        lblCategories = createAnalyticsLabel("Category Breakdown", "N/A");

        panel.add(lblTotalInvValue); panel.add(lblMostValuable);
        panel.add(lblHighestPriced); panel.add(lblLowestStock);
        panel.add(lblAvgPrice); panel.add(lblCategories);

        JPanel c = new JPanel(new BorderLayout()); c.setOpaque(false); c.add(panel, BorderLayout.CENTER); return c;
    }

    private JLabel createAnalyticsLabel(String title, String value) {
        JLabel label = new JLabel("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>" + title + "</span><br/><span style='font-size:18px; font-weight:bold; color:#2c3e50;'>" + value + "</span></div></html>", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        label.setBackground(new Color(250, 250, 250)); label.setOpaque(true);
        return label;
    }

    private JPanel buildRestockView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        String[] cols = {"Priority", "Item Name", "Current Qty", "Min Qty", "Stock Deficit"};
        restockModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        restockTable = styleTable(new JTable(restockModel));

        restockTable.getColumnModel().getColumn(0).setCellRenderer(new PriorityRenderer());
        restockTable.getColumnModel().getColumn(2).setCellRenderer(new RightAlignRenderer());
        restockTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
        restockTable.getColumnModel().getColumn(4).setCellRenderer(new RightAlignRenderer());

        JLabel t = new JLabel("⚠ Strategic Restock Priority Engine", SwingConstants.LEFT); t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(t, BorderLayout.NORTH); panel.add(new JScrollPane(restockTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHealthView() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        String[] cols = {"Asset", "Purchased", "Last Maint.", "Next Due", "Maint. Interval", "Maint. Status", "Condition", "Score", "System Status"};
        healthModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        healthTable = styleTable(new JTable(healthModel));

        healthTable.getColumnModel().getColumn(5).setCellRenderer(new HealthStatusRenderer());
        healthTable.getColumnModel().getColumn(8).setCellRenderer(new HealthStatusRenderer());

        JLabel t = new JLabel("❤ Advanced Asset Health & Depreciation Report", SwingConstants.LEFT); t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(t, BorderLayout.NORTH); panel.add(new JScrollPane(healthTable), BorderLayout.CENTER);
        return panel;
    }

    private JTable styleTable(JTable table) {
        table.setRowHeight(30); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setShowVerticalLines(false); table.setIntercellSpacing(new Dimension(0, 0));
        return table;
    }

    // --- STRICT DIALOG & VALIDATION ---
    // (Inventory Dialog remains unchanged from previous version)
    private void showAddItemDialog() { handleItemDialog(null); }
    private void showUpdateItemDialog() {
        int r = inventoryTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row to update.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        String id = inventoryModel.getValueAt(inventoryTable.convertRowIndexToModel(r), 0).toString();
        handleItemDialog(inventoryService.getAllItems().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null));
    }
    private void showDeleteItemDialog() {
        int r = inventoryTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Permanently delete item?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            inventoryService.deleteItem(inventoryModel.getValueAt(inventoryTable.convertRowIndexToModel(r), 0).toString());
            refreshAllData();
        }
    }

    private void handleItemDialog(Item item) {
        JTextField txtId = new JTextField(item != null ? item.getId() : ""); txtId.setEnabled(item == null);
        JTextField txtName = new JTextField(item != null ? item.getName() : "");
        JTextField txtQty = new JTextField(item != null ? String.valueOf(item.getQuantity()) : "");
        JTextField txtPrice = new JTextField(item != null ? String.valueOf(item.getPrice()) : "");
        JTextField txtCat = new JTextField(item != null ? item.getCategory() : "");
        JTextField txtMin = new JTextField(item != null ? String.valueOf(item.getMinimumStock()) : "");

        Object[] msg = {"Item ID:", txtId, "Name:", txtName, "Quantity:", txtQty, "Unit Price (₹):", txtPrice, "Category:", txtCat, "Minimum Stock:", txtMin};
        if (JOptionPane.showConfirmDialog(this, msg, item == null ? "Add Item" : "Update Item", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) throw new Exception("ID and Name fields cannot be empty.");
                int q = Integer.parseInt(txtQty.getText().trim()); double p = Double.parseDouble(txtPrice.getText().trim()); int m = Integer.parseInt(txtMin.getText().trim());
                if (q < 0 || p < 0 || m < 0) throw new Exception("Values cannot be negative.");
                Item ni = new Item(txtId.getText().trim(), txtName.getText().trim(), q, p, txtCat.getText().trim(), m);
                if (item == null) inventoryService.addItem(ni); else inventoryService.updateItem(ni);
                refreshAllData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void showAddAssetDialog() { handleAssetDialog(null); }
    private void showUpdateAssetDialog() {
        int r = assetTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row to update.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        String id = assetModel.getValueAt(assetTable.convertRowIndexToModel(r), 0).toString();
        handleAssetDialog(assetService.getAllAssets().stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null));
    }
    private void showDeleteAssetDialog() {
        int r = assetTable.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Permanently delete asset?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            assetService.deleteAsset(assetModel.getValueAt(assetTable.convertRowIndexToModel(r), 0).toString());
            refreshAllData();
        }
    }

    private void handleAssetDialog(Asset asset) {
        JTextField txtId = new JTextField(asset != null ? asset.getId() : ""); txtId.setEnabled(asset == null);
        JTextField txtName = new JTextField(asset != null ? asset.getName() : "");
        JTextField txtDate = new JTextField(asset != null ? asset.getPurchaseDate() : LocalDate.now().toString());
        JTextField txtLastMaint = new JTextField(asset != null ? asset.getLastMaintenanceDate() : LocalDate.now().toString());
        JTextField txtCost = new JTextField(asset != null ? String.valueOf(asset.getPurchaseCost()) : "");
        JComboBox<String> cbCond = new JComboBox<>(new String[]{"Excellent", "Good", "Fair", "Poor"});
        if(asset != null) cbCond.setSelectedItem(asset.getCurrentCondition());
        JTextField txtMaint = new JTextField(asset != null ? String.valueOf(asset.getMaintenanceInterval()) : "12");

        Object[] msg = {"Asset ID:", txtId, "Asset Name:", txtName, "Purchase Date (YYYY-MM-DD):", txtDate,
                "Last Maintenance Date (YYYY-MM-DD):", txtLastMaint, "Purchase Cost (₹):", txtCost,
                "Current Physical Condition:", cbCond, "Maintenance Interval (Months):", txtMaint};

        if (JOptionPane.showConfirmDialog(this, msg, asset == null ? "Register Asset" : "Update Asset", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                if (txtId.getText().trim().isEmpty() || txtName.getText().trim().isEmpty()) throw new Exception("ID and Name cannot be empty.");

                LocalDate pDate = LocalDate.parse(txtDate.getText().trim());
                LocalDate lmDate = LocalDate.parse(txtLastMaint.getText().trim());
                LocalDate today = LocalDate.now();

                if (pDate.isAfter(today)) throw new Exception("Purchase Date cannot be in the future.");
                if (lmDate.isAfter(today)) throw new Exception("Last Maintenance Date cannot be in the future.");
                if (lmDate.isBefore(pDate)) throw new Exception("Last Maintenance cannot occur before Purchase Date.");

                double cost = Double.parseDouble(txtCost.getText().trim());
                int maint = Integer.parseInt(txtMaint.getText().trim());

                if (cost < 0) throw new Exception("Cost cannot be negative.");
                if (maint <= 0) throw new Exception("Maintenance Interval must be greater than zero.");

                Asset newAsset = new Asset(txtId.getText().trim(), txtName.getText().trim(), pDate.toString(), cost, cbCond.getSelectedItem().toString(), lmDate.toString(), maint);
                if (asset == null) assetService.addAsset(newAsset); else assetService.updateAsset(newAsset);

                refreshAllData();
            } catch (java.time.format.DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Dates must be YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Numeric fields must be valid numbers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // --- REFRESH LOGIC ---
    private void refreshAllData() {
        inventoryModel.setRowCount(0);
        for (Item i : inventoryService.getAllItems()) inventoryModel.addRow(new Object[]{i.getId(), i.getName(), i.getCategory(), i.getQuantity(), i.getPrice(), i.getMinimumStock()});

        assetModel.setRowCount(0);
        for (Asset a : assetService.getAllAssets()) {
            int score = assetService.calculateHealthScore(a);
            assetModel.addRow(new Object[]{a.getId(), a.getName(), a.getPurchaseDate(), a.getLastMaintenanceDate(), a.getPurchaseCost(), a.getCurrentCondition(), score + "/100", assetService.calculateHealthStatus(score)});
        }

        restockModel.setRowCount(0);
        List<Item> lowStock = inventoryService.getLowStockItems();
        lowStock.sort((i1, i2) -> Double.compare((double)(i2.getMinimumStock() - i2.getQuantity()) / i2.getMinimumStock(), (double)(i1.getMinimumStock() - i1.getQuantity()) / i1.getMinimumStock()));
        for (Item i : lowStock) restockModel.addRow(new Object[]{inventoryService.calculatePriorityLevel(i), i.getName(), i.getQuantity(), i.getMinimumStock(), i.getMinimumStock() - i.getQuantity()});

        healthModel.setRowCount(0);
        for (Asset a : assetService.getAllAssets()) {
            int score = assetService.calculateHealthScore(a);
            healthModel.addRow(new Object[]{a.getName(), a.getPurchaseDate(), a.getLastMaintenanceDate(), assetService.calculateNextMaintenanceDue(a).toString(), a.getMaintenanceInterval() + " mo", assetService.calculateMaintenanceStatus(a), a.getCurrentCondition(), score + "/100", assetService.calculateHealthStatus(score)});
        }

        lblItemsCount.setText("<html><center>📦 Inventory Items<br><b>" + inventoryService.getAllItems().size() + "</b></center></html>");
        lblAssetsCount.setText("<html><center>💻 Asset Count<br><b>" + assetService.getAllAssets().size() + "</b></center></html>");
        lblLowStock.setText("<html><center>⚠ Low Stock<br><b>" + lowStock.size() + "</b></center></html>");
        lblTotalValue.setText(String.format("<html><center>📊 Total Value<br><b>%s</b></center></html>", currencyFormat.format(inventoryService.getTotalValue())));

        lblTotalInvValue.setText("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>Total Portfolio Value</span><br/><span style='font-size:20px; font-weight:bold; color:#2c3e50;'>" + currencyFormat.format(inventoryService.getTotalValue()) + "</span></div></html>");
        Item hp = inventoryService.getHighestPricedItem(); lblHighestPriced.setText("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>Premium Item (Highest Price)</span><br/><span style='font-size:18px; font-weight:bold; color:#2980b9;'>" + (hp != null ? hp.getName() + " (" + currencyFormat.format(hp.getPrice()) + ")" : "N/A") + "</span></div></html>");
        Item mv = inventoryService.getMostValuableItem(); lblMostValuable.setText("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>Most Valuable Asset Class</span><br/><span style='font-size:18px; font-weight:bold; color:#27ae60;'>" + (mv != null ? mv.getName() + " (" + currencyFormat.format(mv.getPrice() * mv.getQuantity()) + ")" : "N/A") + "</span></div></html>");
        Item ls = inventoryService.getLowestStockItem(); lblLowestStock.setText("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>Critical Stock Warning</span><br/><span style='font-size:18px; font-weight:bold; color:#c0392b;'>" + (ls != null ? ls.getName() + " (" + ls.getQuantity() + " units)" : "N/A") + "</span></div></html>");
        lblAvgPrice.setText("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>Average Item Price</span><br/><span style='font-size:18px; font-weight:bold; color:#8e44ad;'>" + currencyFormat.format(inventoryService.getAverageItemPrice()) + "</span></div></html>");
        String catBrk = inventoryService.getCategoryBreakdown(); lblCategories.setText("<html><div style='text-align: center;'><span style='font-size:12px; color:gray;'>Category Breakdown</span><br/><span style='font-size:12px; font-weight:bold; color:#34495e;'>" + (catBrk.isEmpty() ? "N/A" : catBrk) + "</span></div></html>");
    }
}