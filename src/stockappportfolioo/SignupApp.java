package stockappportfolioo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;

public class SignupApp {
    private static Connection conn;

    public static void initialize(Connection connection) {
        conn = connection;
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Sign Up");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 500);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Colors
        Color bgColor = new Color(230, 230, 250); 
        Color panelColor = new Color(230, 230, 250); 
        Color btnColor = new Color(76, 175, 80);
        Color backBtnColor = new Color(33, 150, 243);
        Color textColor = Color.BLACK; 

        // Title
        JLabel titleLabel = new JLabel(" CREATE YOUR ACCOUNT", JLabel.CENTER);
        titleLabel.setForeground(textColor); 
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        frame.add(titleLabel, BorderLayout.NORTH);

        // Panel
        JPanel panel = new JPanel(new GridLayout(9, 2, 10, 10));
        panel.setBackground(panelColor);  
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Labels and fields
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameText = new JTextField("Enter full name");
        JLabel emailLabel = new JLabel("Email ID:");
        JTextField emailText = new JTextField("Enter email ID");
        JLabel dobLabel = new JLabel("Date of Birth (YYYY-MM-DD):");
        JTextField dobText = new JTextField("Enter date of birth");
        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField("Enter username");
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passText = new JPasswordField("Enter password");
        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPassText = new JPasswordField("Confirm password");
        JCheckBox showPasswordCheck = new JCheckBox("Show Password");

        // Style labels and text fields to have black text
        JLabel[] labels = {nameLabel, emailLabel, dobLabel, userLabel, passLabel, confirmPassLabel};
        for (JLabel lbl : labels) lbl.setForeground(textColor);
        nameText.setForeground(Color.GRAY);
        emailText.setForeground(Color.GRAY);
        dobText.setForeground(Color.GRAY);
        userText.setForeground(Color.GRAY);
        passText.setForeground(Color.GRAY);
        confirmPassText.setForeground(Color.GRAY);
        showPasswordCheck.setForeground(textColor);
        showPasswordCheck.setBackground(panelColor);

        // Add focus listener for placeholder functionality
        addPlaceholderFunctionality(nameText, "Enter full name");
        addPlaceholderFunctionality(emailText, "Enter email ID");
        addPlaceholderFunctionality(dobText, "Enter date of birth");
        addPlaceholderFunctionality(userText, "Enter username");
        addPlaceholderFunctionality(passText, "Enter password");
        addPlaceholderFunctionality(confirmPassText, "Confirm password");

        // Buttons
        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(btnColor);
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);

        JButton backButton = new JButton("Back to Login");
        backButton.setBackground(backBtnColor);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);

        // Add components
        panel.add(nameLabel); panel.add(nameText);
        panel.add(emailLabel); panel.add(emailText);
        panel.add(dobLabel); panel.add(dobText);
        panel.add(userLabel); panel.add(userText);
        panel.add(passLabel); panel.add(passText);
        panel.add(confirmPassLabel); panel.add(confirmPassText);
        panel.add(showPasswordCheck); panel.add(new JLabel());
        panel.add(signupButton); panel.add(backButton);

        frame.add(panel, BorderLayout.CENTER);

        // Show/Hide password checkbox functionality
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passText.setEchoChar((char) 0);
                confirmPassText.setEchoChar((char) 0);
            } else {
                passText.setEchoChar('•');
                confirmPassText.setEchoChar('•');
            }
        });

        // Sign up button action
        signupButton.addActionListener(e -> {
            String name = nameText.getText();
            String email = emailText.getText();
            String dob = dobText.getText();
            String username = userText.getText();
            String password = new String(passText.getPassword());
            String confirmPassword = new String(confirmPassText.getPassword());

            if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid email address.");
            } else if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(frame, "Passwords do not match.");
            } else if (!isPasswordValid(password)) {
                JOptionPane.showMessageDialog(frame, "Password must be at least 8 characters, "
                        + "include uppercase, lowercase, number, and special character.");
            } else {
                saveUser(name, email, dob, username, password);
            }
        });

        // Back button action
        backButton.addActionListener(e -> {
            frame.dispose();
            StockAppPortfolioo.showLogin();
        });

        frame.getContentPane().setBackground(bgColor); // Lavender background for the entire frame
        frame.setVisible(true);
    }

    private static void saveUser(String name, String email, String dob, String username, String password) {
        try {
            String sql = "INSERT INTO users (name, email, dob, username, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, dob);
                pstmt.setString(4, username);
                pstmt.setString(5, password);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "✅ Sign up successful!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    // Password validation method
    private static boolean isPasswordValid(String password) {
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    // Email validation method
    private static boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Helper method to add placeholder functionality
    private static void addPlaceholderFunctionality(JTextField field, String placeholder) {
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK); // Change text color to black when focused
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder); // Reset placeholder text
                }
            }
        });
    }

    // Helper method to add placeholder functionality for JPasswordField
    private static void addPlaceholderFunctionality(JPasswordField field, String placeholder) {
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK); // Change text color to black when focused
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (String.valueOf(field.getPassword()).isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder); // Reset placeholder text
                }
            }
        });
    }
}

































































