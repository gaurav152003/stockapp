package stockappportfolioo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import org.jfree.chart.axis.NumberAxis;

public class HomeDashboardApp {
    private static int userID;
    private static Connection conn;

    public static void initialize(int currentUserID, Connection connection) {
        userID = currentUserID;
        conn = connection;
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Home");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        navPanel.setBackground(new Color(33, 37, 41));

        JButton homeButton = new JButton("Home");
        JButton infoButton = new JButton("Info");
        JButton researchButton = new JButton("Research");
        JButton buySellButton = new JButton("Buy & Sell");
        JButton portfolioButton = new JButton("Portfolio");

        Color buttonColor = new Color(0, 123, 255);

        homeButton.setBackground(buttonColor);
        infoButton.setBackground(buttonColor);
        researchButton.setBackground(buttonColor);
        buySellButton.setBackground(buttonColor);
        portfolioButton.setBackground(buttonColor);

        Color textColor = Color.BLACK;

        homeButton.setForeground(textColor);
        infoButton.setForeground(textColor);
        researchButton.setForeground(textColor);
        buySellButton.setForeground(textColor);
        portfolioButton.setForeground(textColor);

        // Buttons added in new order (Research before Buy & Sell)
        navPanel.add(homeButton);
        navPanel.add(infoButton);
        navPanel.add(researchButton);
        navPanel.add(buySellButton);
        navPanel.add(portfolioButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(248, 249, 250));

        JPanel homeContentPanel = createHomeContent();
        contentPanel.add(homeContentPanel, BorderLayout.CENTER);

        frame.add(navPanel, BorderLayout.NORTH);
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeDB();
            }
        });

        homeButton.addActionListener(new NavButtonListener(frame));
        infoButton.addActionListener(new NavButtonListener(frame));
        researchButton.addActionListener(new NavButtonListener(frame));
        buySellButton.addActionListener(new NavButtonListener(frame));
        portfolioButton.addActionListener(new NavButtonListener(frame));

        frame.setVisible(true);
    }

    private static void closeDB() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static JPanel createHomeContent() {
        JPanel homeContentPanel = new JPanel();
        homeContentPanel.setLayout(new BorderLayout());
        homeContentPanel.setBackground(new Color(248, 249, 250));

        double currentNetWorth = Double.parseDouble(fetchNetWorth());

        JLabel netWorthLabel = new JLabel("<html><h1>Current Net Worth: $" + String.format("%.2f", currentNetWorth) + "</h1></html>");
        netWorthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        netWorthLabel.setForeground(new Color(0, 123, 255));
        homeContentPanel.add(netWorthLabel, BorderLayout.NORTH);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        double janNetWorth = calculateNetWorthForMonth("Jan");
        double febNetWorth = calculateNetWorthForMonth("Feb");
        double marNetWorth = calculateNetWorthForMonth("Mar");

        dataset.addValue(janNetWorth, "Portfolio Value", "Jan");
        dataset.addValue(febNetWorth, "Portfolio Value", "Feb");
        dataset.addValue(marNetWorth, "Portfolio Value", "Mar");
        dataset.addValue(currentNetWorth, "Portfolio Value", "Current");

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Portfolio Value",
                "Month",
                "Value ($)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        org.jfree.chart.plot.CategoryPlot plot = lineChart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(230, 230, 250));
        plot.setRangeGridlinePaint(Color.GRAY);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(-1000, 10000);
        DecimalFormat format = new DecimalFormat("##,###");
        yAxis.setNumberFormatOverride(format);

        lineChart.getTitle().setPaint(new Color(33, 37, 41));
        plot.getDomainAxis().setLabelPaint(new Color(33, 37, 41));
        plot.getRangeAxis().setLabelPaint(new Color(33, 37, 41));
        plot.getDomainAxis().setTickLabelPaint(new Color(33, 37, 41));
        plot.getRangeAxis().setTickLabelPaint(new Color(33, 37, 41));

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setBackground(new Color(248, 249, 250));

        homeContentPanel.add(chartPanel, BorderLayout.CENTER);

        return homeContentPanel;
    }

    private static double calculateNetWorthForMonth(String month) {
        double totalNetWorth = 0.0;

        try {
            String query = "SELECT company_name, quantity FROM portfolio WHERE user_id = ? AND purchase_month = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userID);
                pstmt.setString(2, month);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String companyName = rs.getString("company_name");
                        int quantity = rs.getInt("quantity");
                        double currentPrice = fetchCurrentStockPrice(companyName);
                        totalNetWorth += currentPrice * quantity;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalNetWorth > 0 ? totalNetWorth : 0.0;
    }

    private static JPanel createBuySellContent() {
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "Database connection is not established.", "Error", JOptionPane.ERROR_MESSAGE);
            return new JPanel();
        }

        BuySellPage buySellPage = new BuySellPage(userID, conn);
        JPanel panel = buySellPage.getBuySellPanel();
        panel.setBackground(new Color(248, 249, 250));
        return panel;
    }

    private static JPanel createPortfolioContent() {
        PortfolioPage portfolioPage = new PortfolioPage(userID, conn);
        JPanel panel = portfolioPage.getContentPanel();
        panel.setBackground(new Color(248, 249, 250));
        return panel;
    }

    private static String fetchNetWorth() {
        double totalNetWorth = 0.0;
        try {
            String query = "SELECT company_name, quantity FROM portfolio WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String companyName = rs.getString("company_name");
                        int quantity = rs.getInt("quantity");
                        double currentPrice = fetchCurrentStockPrice(companyName);
                        totalNetWorth += currentPrice * quantity;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("%.2f", totalNetWorth);
    }

    private static double fetchCurrentStockPrice(String companyName) {
        double stockPrice = 0.0;
        try {
            String apiKey = "XRGFSM8TVWHS92QD";
            String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + companyName + "&apikey=" + apiKey;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            if (jsonObject.has("Error Message")) {
                JOptionPane.showMessageDialog(null, "Error fetching stock data: " + jsonObject.getString("Error Message"), "API Error", JOptionPane.ERROR_MESSAGE);
                return 0.0;
            }

            if (!jsonObject.has("Time Series (Daily)")) {
                JOptionPane.showMessageDialog(null, "No time series data available for symbol: " + companyName, "Data Error", JOptionPane.ERROR_MESSAGE);
                return 0.0;
            }

            JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");
            String latestDate = timeSeries.keys().next();
            JSONObject latestData = timeSeries.getJSONObject(latestDate);
            stockPrice = latestData.getDouble("4. close");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An error occurred while fetching the stock price.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return stockPrice;
    }

    private static class NavButtonListener implements ActionListener {
        private final JFrame frame;

        public NavButtonListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JButton sourceButton = (JButton) e.getSource();
            String buttonText = sourceButton.getText();

            switch (buttonText) {
                case "Home" -> updateContentPanel(frame, createHomeContent());
                case "Info" -> updateContentPanel(frame, createNewsPageContent());
                case "Research" -> updateContentPanel(frame, createMarketDataContent("AAPL"));
                case "Buy & Sell" -> updateContentPanel(frame, createBuySellContent());
                case "Portfolio" -> updateContentPanel(frame, createPortfolioContent());
            }
        }
    }

    private static void updateContentPanel(JFrame frame, JPanel newContentPanel) {
        Container contentPane = frame.getContentPane();
        Component[] components = contentPane.getComponents();

        if (components.length > 1 && components[1] instanceof JPanel) {
            JPanel contentPanel = (JPanel) components[1];
            contentPanel.removeAll();
            contentPanel.add(newContentPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private static JPanel createNewsPageContent() {
        NewsPage newsPage = new NewsPage();
        JPanel panel = newsPage.createNewsPagePanel();
        panel.setBackground(new Color(248, 249, 250));
        return panel;
    }

    private static JPanel createMarketDataContent(String symbol) {
        JPanel panel = MarketData.createMarketDataPanel(symbol);
        panel.setBackground(new Color(248, 249, 250));
        return panel;
    }
}















































