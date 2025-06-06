/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package stockappportfolioo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InfoPageOption {

    public JPanel createInfoPageOptionPanel(JFrame mainFrame) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
       
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10)); 
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50)); 
        
        JButton marketDataButton = new JButton("Current Market Data");
        JButton newsButton = new JButton("News");

        marketDataButton.setFont(new Font("Arial", Font.BOLD, 18));
        newsButton.setFont(new Font("Arial", Font.BOLD, 18));

        marketDataButton.setBackground(new Color(0, 123, 255));
        newsButton.setBackground(new Color(0, 123, 255));

        marketDataButton.setForeground(Color.BLACK);
        newsButton.setForeground(Color.BLACK);

        buttonPanel.add(marketDataButton);
        buttonPanel.add(newsButton);

        infoPanel.add(buttonPanel, BorderLayout.CENTER);
      
        marketDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open MarketData JPanel
                String symbol = "MSFT"; // Default or prompt user to input
                JPanel marketDataPanel = new MarketData().createMarketDataPanel(symbol);
                updateContentPanel(mainFrame, marketDataPanel); // Update content panel
            }
        });

        newsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel newsPagePanel = new NewsPage().createNewsPagePanel(); // Ensure createNewsPagePanel returns JPanel
                updateContentPanel(mainFrame, newsPagePanel); // Update content panel
            }
        });

        return infoPanel;
    }

    private void updateContentPanel(JFrame frame, JPanel newContentPanel) {
        Container contentPane = frame.getContentPane();
        if (contentPane.getComponentCount() > 1 && contentPane.getComponent(1) instanceof JPanel) {
            JPanel contentPanel = (JPanel) contentPane.getComponent(1); // Assuming content panel is the second component
            contentPanel.removeAll();
            contentPanel.add(newContentPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        } else {
            JPanel newContentPanelWrapper = new JPanel(new BorderLayout());
            newContentPanelWrapper.add(newContentPanel, BorderLayout.CENTER);
            contentPane.add(newContentPanelWrapper, BorderLayout.CENTER);
            contentPane.revalidate();
            contentPane.repaint();
        }
    }
}
