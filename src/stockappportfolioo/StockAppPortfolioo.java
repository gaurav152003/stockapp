package stockappportfolioo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StockAppPortfolioo {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/stockpapp"; // Update to your DB name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root"; // Replace with your MySQL root password
    public static void showLogin() {
        Connection conn = null;
        try {
            // Establish the database connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Initialize LoginApp with the connection
            LoginApp.initialize(conn);

            // Open the login dialog and get the userID after login
            int userID = LoginApp.showLoginDialog();

            // If userID is valid, create and show the PortfolioPage
            if (userID != -1) {
                PortfolioPage portfolioPage = new PortfolioPage(userID, conn); // Create the portfolio page
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database.");
        } finally {
            // Close the connection here if you want to close it after usage
            try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
            LoginApp.initialize(conn);
            LoginApp.showLoginDialog();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database.");
        }
        }
    }


    public static void main(String[] args) {
        Connection conn = null;
        try {
            // Establish the database connection
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Initialize LoginApp with the connection
            LoginApp.initialize(conn);

            // Open the login dialog and get the userID after login
            int userID = LoginApp.showLoginDialog();

            // If userID is valid, create and show the PortfolioPage
            if (userID != -1) {
                PortfolioPage portfolioPage = new PortfolioPage(userID, conn); // Create the portfolio page
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database.");
        } finally {
    // Close the connection here after all application usage
    try {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
        showLogin();

        
    }
}
