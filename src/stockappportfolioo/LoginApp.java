package stockappportfolioo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginApp {
    private static Connection conn;
    private static JFrame loginFrame;

    public static void initialize(Connection connection) {
        conn = connection;
    }

    public static void closeLoginDialog() {
        if (loginFrame != null) {
            loginFrame.dispose();
            loginFrame = null;
        }
    }

    public static int showLoginDialog() {
        if (loginFrame != null && loginFrame.isVisible()) {
            loginFrame.requestFocus();
            return -1;
        }

        loginFrame = new JFrame("Stock Portfolio Login");
        loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loginFrame.setSize(450, 350);
        loginFrame.setLayout(new BorderLayout());
        loginFrame.setLocationRelativeTo(null);

        Color bgColor = new Color(230, 230, 250);       
        Color panelColor = new Color(230, 230, 250);    
        Color btnColor = new Color(76, 175, 80);        
        Color textColor = Color.BLACK;                   
        
        JLabel titleLabel = new JLabel(" WELCOME TO STOCK HUB", JLabel.CENTER);
        titleLabel.setForeground(textColor);            
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); 
        loginFrame.add(titleLabel, BorderLayout.NORTH);
        
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBackground(panelColor);  
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(textColor);  
        JTextField userText = new JTextField("Enter username");
        userText.setForeground(Color.GRAY); 
        userText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (userText.getText().equals("Enter username")) {
                    userText.setText("");
                    userText.setForeground(Color.BLACK); 
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (userText.getText().isEmpty()) {
                    userText.setForeground(Color.GRAY);
                    userText.setText("Enter username"); 
                }
            }
        });

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(textColor);  
        JPasswordField passText = new JPasswordField("Enter password");
        passText.setForeground(Color.GRAY); 
        passText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(passText.getPassword()).equals("Enter password")) {
                    passText.setText("");
                    passText.setForeground(Color.BLACK); 
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(passText.getPassword()).isEmpty()) {
                    passText.setForeground(Color.GRAY);
                    passText.setText("Enter password"); 
                }
            }
        });
      
        JCheckBox showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setBackground(panelColor);
        showPasswordCheckBox.setForeground(textColor); 
        
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(btnColor);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
       
        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(new Color(33, 150, 243));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);
        
        panel.add(userLabel);
        panel.add(userText);
        panel.add(passLabel);
        panel.add(passText);
        panel.add(showPasswordCheckBox);
        panel.add(new JLabel()); 
        panel.add(loginButton);
        panel.add(signupButton);
       
        loginFrame.add(panel, BorderLayout.CENTER);
       
        final int[] userIDHolder = {-1};
        
        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passText.getPassword());
            userIDHolder[0] = validateLogin(username, password);
            if (userIDHolder[0] != -1) {
                JOptionPane.showMessageDialog(loginFrame, "✅ Login successful!");
                HomeDashboardApp.initialize(userIDHolder[0], conn);
                HomeDashboardApp.createAndShowGUI();
                loginFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "❌ Invalid username or password.");
            }
        });
       
        signupButton.addActionListener(e -> {
            loginFrame.dispose();
            SignupApp.initialize(conn);
            SignupApp.createAndShowGUI();
        });
      
        showPasswordCheckBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                passText.setEchoChar((char) 0);
            } else {
                passText.setEchoChar('•');
            }
        });

        loginFrame.getContentPane().setBackground(bgColor); 
        loginFrame.setVisible(true);

        return -1;
    }

    private static int validateLogin(String username, String password) {
        try {
            String sql = "SELECT ID FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("ID");
                    } else {
                        return -1;
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            return -1;
        }
    }
}
