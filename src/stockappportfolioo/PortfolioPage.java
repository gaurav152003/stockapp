package stockappportfolioo;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

public class PortfolioPage {

    private JTable portfolioTable;
    private int userID;
    private static Connection conn;

    public PortfolioPage(int userID, Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.userID = userID;
        this.conn = conn;
    }

    private void createPortfolioTable() {
        String[] columnNames = {
            "Date", "Company Name", "Quantity", "Buy Price", "Current Stock Price", "Balance Display", "Gain", "Loss"
        };

        Object[][] data = fetchUserPortfolioData();

        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        portfolioTable = new JTable(model);
        portfolioTable.getTableHeader().setReorderingAllowed(false);
        portfolioTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Table Header Styling
        JTableHeader header = portfolioTable.getTableHeader();
        header.setBackground(new Color(0, 123, 255));  // Bright blue
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));

        // Table Row Styling
        portfolioTable.setBackground(Color.WHITE); // white background
        portfolioTable.setForeground(Color.BLACK); // black text
        portfolioTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        portfolioTable.setRowHeight(26);
        portfolioTable.setGridColor(new Color(220, 220, 220));
        portfolioTable.setSelectionBackground(new Color(189, 195, 199));
        portfolioTable.setSelectionForeground(Color.BLACK);

        // Alternating Row Background
        portfolioTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(248, 248, 248) : Color.WHITE);
                }
                return c;
            }
        });

        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < portfolioTable.getColumnCount(); i++) {
            portfolioTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Gain & Loss Cell Color Renderers
        portfolioTable.getColumnModel().getColumn(6).setCellRenderer(new GainLossCellRenderer(true));  // Gain
        portfolioTable.getColumnModel().getColumn(7).setCellRenderer(new GainLossCellRenderer(false)); // Loss
    }

    private Object[][] fetchUserPortfolioData() {
        ArrayList<Object[]> rows = new ArrayList<>();
        String query = "SELECT date, company_name, quantity, buy_price, current_stock_price FROM portfolio WHERE user_id = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setInt(1, userID);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int quantity = rs.getInt("quantity");
                double buyPrice = rs.getDouble("buy_price");
                double currentStockPrice = rs.getDouble("current_stock_price");

                double balanceDisplay;
                if (quantity != 0) {
                    balanceDisplay = (currentStockPrice - (buyPrice / quantity)) * quantity;
                } else {
                    balanceDisplay = Double.NaN;
                }

                double gain = balanceDisplay > 0 ? balanceDisplay : 0;
                double loss = balanceDisplay < 0 ? Math.abs(balanceDisplay) : 0;

                Object[] row = new Object[8];
                row[0] = rs.getDate("date");
                row[1] = rs.getString("company_name");
                row[2] = quantity;
                row[3] = buyPrice;
                row[4] = currentStockPrice;
                row[5] = balanceDisplay;
                row[6] = gain;
                row[7] = loss;
                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rows.toArray(new Object[0][]);
    }

    public void deleteZeroQuantityRows() {
        String sql = "DELETE FROM portfolio WHERE quantity = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println(rowsDeleted + " rows deleted where quantity was zero.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting zero quantity rows: " + e.getMessage());
        }
    }

    public JPanel getContentPanel() {
        createPortfolioTable();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE); // match screenshot background

        JLabel titleLabel = new JLabel("PORTFOLIO", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(new Color(52, 73, 94)); // Dark gray-blue
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(portfolioTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static class GainLossCellRenderer extends DefaultTableCellRenderer {
        private final boolean isGain;

        public GainLossCellRenderer(boolean isGain) {
            this.isGain = isGain;
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isGain) {
                c.setForeground(new Color(39, 174, 96)); // Green
            } else {
                c.setForeground(new Color(231, 76, 60)); // Red
            }

            c.setFont(new Font("Segoe UI", Font.BOLD, 13));
            return c;
        }
    }
}















































