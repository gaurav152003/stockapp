package stockappportfolioo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BuySellPage {
    private int userID;
    private Connection conn;

    public BuySellPage(int userID, Connection conn) {
        if (conn != null) {
            this.userID = userID;
            this.conn = conn;
        } else {
            throw new IllegalArgumentException("Connection cannot be null");
        }
    }

    public JPanel getBuySellPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        // Buy Panel
        JPanel buyPanel = new JPanel(new GridLayout(4, 2));
        JLabel companyNameLabelBuy = new JLabel("Company Name:");
        JLabel quantityLabelBuy = new JLabel("Quantity:");
        JLabel priceLabelBuy = new JLabel("Buy Price:");
        companyNameLabelBuy.setForeground(Color.BLACK);
        quantityLabelBuy.setForeground(Color.BLACK);
        priceLabelBuy.setForeground(Color.BLACK);

        JTextField companyNameFieldBuy = new JTextField();
        JTextField quantityFieldBuy = new JTextField();
        JTextField priceFieldBuy = new JTextField();
        priceFieldBuy.setEditable(false);

        JButton buyButton = new JButton("Buy");
        buyButton.setForeground(Color.BLACK);

        ActionListener autofillBuyPriceListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String companyName = companyNameFieldBuy.getText().trim();
                String quantityStr = quantityFieldBuy.getText().trim();

                if (!companyName.isEmpty() && !quantityStr.isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        double currentPrice = MarketData.getCurrentStockPrice(companyName);
                        priceFieldBuy.setText(String.valueOf(currentPrice * quantity));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error fetching stock price. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        };

        companyNameFieldBuy.addActionListener(autofillBuyPriceListener);
        quantityFieldBuy.addActionListener(autofillBuyPriceListener);

        buyPanel.add(companyNameLabelBuy);
        buyPanel.add(companyNameFieldBuy);
        buyPanel.add(quantityLabelBuy);
        buyPanel.add(quantityFieldBuy);
        buyPanel.add(priceLabelBuy);
        buyPanel.add(priceFieldBuy);
        buyPanel.add(new JLabel());
        buyPanel.add(buyButton);

        // Sell Panel
        JPanel sellPanel = new JPanel(new GridLayout(5, 2));
        JLabel companyNameLabelSell = new JLabel("Company Name:");
        JLabel quantityLabelSell = new JLabel("Quantity:");
        JLabel buyPriceLabelSell = new JLabel("Buy Price:");
        JLabel sellPriceLabelSell = new JLabel("Sell Price:");
        companyNameLabelSell.setForeground(Color.BLACK);
        quantityLabelSell.setForeground(Color.BLACK);
        buyPriceLabelSell.setForeground(Color.BLACK);
        sellPriceLabelSell.setForeground(Color.BLACK);

        JTextField companyNameFieldSell = new JTextField();
        JTextField quantityFieldSell = new JTextField();
        JTextField buyPriceFieldSell = new JTextField();
        JTextField sellPriceFieldSell = new JTextField();
        sellPriceFieldSell.setEditable(false);

        JButton sellButton = new JButton("Sell");
        sellButton.setForeground(Color.BLACK);

        ActionListener autofillSellPriceListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String companyName = companyNameFieldSell.getText().trim();
                String quantityStr = quantityFieldSell.getText().trim();

                if (!companyName.isEmpty() && !quantityStr.isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        double currentPrice = MarketData.getCurrentStockPrice(companyName);
                        sellPriceFieldSell.setText(String.valueOf(currentPrice * quantity));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error fetching stock price. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        };

        companyNameFieldSell.addActionListener(autofillSellPriceListener);
        quantityFieldSell.addActionListener(autofillSellPriceListener);

        sellPanel.add(companyNameLabelSell);
        sellPanel.add(companyNameFieldSell);
        sellPanel.add(quantityLabelSell);
        sellPanel.add(quantityFieldSell);
        sellPanel.add(buyPriceLabelSell);
        sellPanel.add(buyPriceFieldSell);
        sellPanel.add(sellPriceLabelSell);
        sellPanel.add(sellPriceFieldSell);
        sellPanel.add(new JLabel());
        sellPanel.add(sellButton);

        panel.add(buyPanel);
        panel.add(sellPanel);

        buyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String companyName = companyNameFieldBuy.getText();
                int quantity = Integer.parseInt(quantityFieldBuy.getText());
                double price = Double.parseDouble(priceFieldBuy.getText());
                handleBuy(companyName, quantity, price);
            }
        });

        sellButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String companyName = companyNameFieldSell.getText();
                int quantity = Integer.parseInt(quantityFieldSell.getText());
                double buyPrice = Double.parseDouble(buyPriceFieldSell.getText());
                double sellPrice = Double.parseDouble(sellPriceFieldSell.getText());
                handleSell(companyName, quantity, buyPrice, sellPrice);
            }
        });

        return panel;
    }

    private void handleBuy(String companyName, int quantity, double price) {
        if (companyName.isEmpty() || quantity <= 0 || price <= 0) {
            JOptionPane.showMessageDialog(null, "Please enter valid company name, quantity, and price.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(now);

        try {
            double currentStockPrice = MarketData.getCurrentStockPrice(companyName);
            double balanceDisplay = currentStockPrice - price;
            String gainLoss = balanceDisplay >= 0 ? "Gain" : "Loss";

            String sql = "INSERT INTO portfolio (user_id, company_name, quantity, buy_price, current_stock_price, date, balance_display, gain_loss) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, userID);
                pstmt.setString(2, companyName);
                pstmt.setInt(3, quantity);
                pstmt.setDouble(4, price);
                pstmt.setDouble(5, currentStockPrice);
                pstmt.setString(6, currentDate);
                pstmt.setDouble(7, balanceDisplay);
                pstmt.setString(8, gainLoss);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Stock bought successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSell(String companyName, int quantity, double buyPrice, double sellPrice) {
        try {
            String query = "SELECT quantity FROM portfolio WHERE user_id = ? AND company_name = ?";
            int availableQuantity = 0;
            boolean stockExists = false;

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userID);
                pstmt.setString(2, companyName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        availableQuantity = rs.getInt("quantity");
                        stockExists = true;
                    }
                }
            }

            if (stockExists) {
                if (availableQuantity >= quantity) {
                    double balanceDisplay = (sellPrice - buyPrice) * quantity;
                    String gainLoss = balanceDisplay >= 0 ? "Gain" : "Loss";

                    String updateSql = "UPDATE portfolio SET quantity = quantity - ?, balance_display = ?, gain_loss = ? WHERE user_id = ? AND company_name = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setInt(1, quantity);
                        updatePstmt.setDouble(2, balanceDisplay);
                        updatePstmt.setString(3, gainLoss);
                        updatePstmt.setInt(4, userID);
                        updatePstmt.setString(5, companyName);
                        int rowsAffected = updatePstmt.executeUpdate();

                        if (availableQuantity - quantity == 0) {
                            String deleteSql = "DELETE FROM portfolio WHERE user_id = ? AND company_name = ?";
                            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                                deletePstmt.setInt(1, userID);
                                deletePstmt.setString(2, companyName);
                                deletePstmt.executeUpdate();
                            }
                        }

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(null, "Stock sold successfully!");
                        } else {
                            JOptionPane.showMessageDialog(null, "Stock sale failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Insufficient quantity to sell.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Stock not found in portfolio.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
