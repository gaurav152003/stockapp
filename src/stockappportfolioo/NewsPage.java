        package stockappportfolioo;

        import org.json.JSONArray;
        import org.json.JSONObject;

        import javax.swing.*;
        import javax.swing.event.HyperlinkEvent;
        import javax.swing.event.HyperlinkListener;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.io.BufferedReader;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;

        public class NewsPage extends JPanel {

            private static final String API_KEY = "1b5186d0c69f4400967295a23c0bed4f"; // Replace with your actual API key
            private static final String API_URL = "https://newsapi.org/v2/top-headlines"; // Example API URL

            private JPanel searchPanel;
            private JTextField searchTermField;
            private JComboBox<String> categoryComboBox;
            private JComboBox<String> regionComboBox;
            private JTextPane newsArea;
            private JScrollPane scrollPane;

            public NewsPage() {
                setLayout(new BorderLayout());

                // Initialize components
                searchPanel = new JPanel();
                searchTermField = new JTextField(15);
                String[] categories = { "Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology" };
                categoryComboBox = new JComboBox<>(categories);
                String[] regions = { "us", "gb", "ca", "au", "in" };
                regionComboBox = new JComboBox<>(regions);
                JButton searchButton = new JButton("Search");
                newsArea = new JTextPane();
                newsArea.setContentType("text/html"); // Set content type to HTML
                newsArea.setEditable(false); // Make the area non-editable
                scrollPane = new JScrollPane(newsArea);

                // Set the preferred size of the scrollPane to make it larger
                scrollPane.setPreferredSize(new Dimension(600, 500)); // Adjust size as needed

                // Add components to search panel with vertical BoxLayout
                searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
                searchPanel.add(new JLabel("Search Term:"));
                searchPanel.add(searchTermField);
                searchPanel.add(new JLabel("Category:"));
                searchPanel.add(categoryComboBox);
                searchPanel.add(new JLabel("Region:"));
                searchPanel.add(regionComboBox);
                searchPanel.add(Box.createVerticalStrut(10)); // Add spacing
                searchPanel.add(searchButton);

                // Add panels to main panel
                add(searchPanel, BorderLayout.WEST);
                add(scrollPane, BorderLayout.CENTER);

                // Add action listener to search button
                searchButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fetchNews();
                    }
                });

                // Add hyperlink listener to JTextPane
                newsArea.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
                            try {
                                Desktop.getDesktop().browse(e.getURL().toURI());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
            }

            public JPanel createNewsPagePanel() {
                return this; // Return the current instance of NewsPage as JPanel
            }

            private void fetchNews() {
                String searchTerm = searchTermField.getText().trim();
                String category = (String) categoryComboBox.getSelectedItem();
                String region = (String) regionComboBox.getSelectedItem();

                String urlString = String.format("%s?apiKey=%s&q=%s&category=%s&country=%s", 
                        API_URL, API_KEY, searchTerm, category, region);

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        parseAndDisplayNews(response.toString());
                    } else {
                        newsArea.setText("Failed to fetch news. HTTP response code: " + responseCode);
                    }
                } catch (Exception e) {
                    newsArea.setText("Error: " + e.getMessage());
                }
            }

            private void parseAndDisplayNews(String jsonResponse) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray articles = jsonObject.getJSONArray("articles");

                    if (articles.length() == 0) {
                        newsArea.setText("No news articles found.");
                        return;
                    }

                    StringBuilder newsContent = new StringBuilder();
                    for (int i = 0; i < articles.length(); i++) {
                        JSONObject article = articles.getJSONObject(i);
                        String title = article.getString("title");
                        String description = article.optString("description", "No description available.");
                        String url = article.getString("url");

                        // Add news title as a clickable link
                        newsContent.append(String.format("<a href=\"%s\">%s</a><br>", url, title));
                        newsContent.append(String.format("%s<br><br>", description));
                    }
                    newsArea.setText(newsContent.toString());
                } catch (Exception e) {
                    newsArea.setText("Error parsing news: " + e.getMessage());
                }
            }
        }
