package stockappportfolioo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import org.json.JSONException;

public class MarketData {

    private static final String API_KEY = "4M725G3K5B2KY8OF";
    private static final String API_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&apikey=" + API_KEY;

    private static JTextField symbolField;
    private static JPanel chartPanel;

    public static JPanel createMarketDataPanel(String symbol) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout());
        symbolField = new JTextField(10);
        JButton searchButton = new JButton("Search");
        
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputSymbol = symbolField.getText().trim();
                if (!inputSymbol.isEmpty()) {
                    updateChart(inputSymbol);
                } else {
                    JOptionPane.showMessageDialog(null, "Please enter a stock symbol.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        topPanel.add(new JLabel("Enter Stock Symbol:"));
        topPanel.add(symbolField);
        topPanel.add(searchButton);
       
        chartPanel = new JPanel(new BorderLayout());
        updateChart(symbol); 
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private static void updateChart(String symbol) {
        String response = fetchMarketData(symbol);
        try {
            JSONObject jsonObject = new JSONObject(response);
            
            if (jsonObject.has("Error Message") || !jsonObject.has("Time Series (Daily)")) {
                JOptionPane.showMessageDialog(null, "Invalid stock symbol or no data available. Please try again.", "Invalid Symbol", JOptionPane.ERROR_MESSAGE);
                chartPanel.removeAll();
                chartPanel.revalidate();
                chartPanel.repaint();
                return;
            }
            
            XYSeriesCollection dataset = parseMarketData(response);
            JFreeChart chart = ChartFactory.createXYLineChart(
                "STOCK PRICE OVER TIME", 
                "Date", 
                "Price", 
                dataset, 
                PlotOrientation.VERTICAL,
                true, 
                true, 
                false 
            );

            
            XYPlot plot = chart.getXYPlot();
            DateAxis axis = new DateAxis("Date");
            axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
            plot.setDomainAxis(axis);

            ChartPanel chartPanelContent = new ChartPanel(chart);
            chartPanelContent.setPreferredSize(new Dimension(800, 400));
            
            chartPanel.removeAll();
            chartPanel.add(chartPanelContent, BorderLayout.CENTER);
            chartPanel.revalidate();
            chartPanel.repaint();
            
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "Error parsing data. Please try again later.", "Data Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "An unexpected error occurred. Please try again later.", "Unexpected Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static String fetchMarketData(String symbol) {
        StringBuilder result = new StringBuilder();
        String urlString = String.format(API_URL, symbol);
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error fetching data from API. Please check your internet connection or try again later.", "API Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return result.toString();
    }

    private static XYSeriesCollection parseMarketData(String response) {
        XYSeries series = new XYSeries("Stock Price");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");

            for (String date : timeSeries.keySet()) {
                JSONObject dailyData = timeSeries.getJSONObject(date);
                double closePrice = dailyData.getDouble("4. close");

                Date parsedDate = dateFormat.parse(date);
                series.add(parsedDate.getTime(), closePrice);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }

    // Method to get the current stock price for a given symbol
    public static double getCurrentStockPrice(String symbol) throws Exception {
        String response = fetchMarketData(symbol);
        JSONObject jsonObject = new JSONObject(response);
        
        // Check for API errors or missing data
        if (jsonObject.has("Error Message") || !jsonObject.has("Time Series (Daily)")) {
            throw new Exception("Invalid company name or data not found.");
        }
        
        JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");
        String latestDate = timeSeries.keys().next();
        JSONObject latestData = timeSeries.getJSONObject(latestDate);
        return latestData.getDouble("4. close");
    }
}



































