
package view;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import db.dbcon;

public class login {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Login Page");
        frame.setSize(350, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        panel.add(new JLabel("Email:"));
        JTextField email = new JTextField();
        panel.add(email);

        panel.add(new JLabel("Password:"));
        JPasswordField password = new JPasswordField();
        panel.add(password);

        JButton loginButton = new JButton("Login");
        JButton createAccount = new JButton("Create Account");

        panel.add(loginButton);
        panel.add(createAccount);

        loginButton.addActionListener(e -> {

            String em = email.getText().trim();
            String p = new String(password.getPassword());

            if (em.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(
                    frame, "Please enter email and password."
                );
                return;
            }

            String sql =
                "SELECT user_id, username FROM users " +
                "WHERE email = ? AND password = ?";

            try (
                Connection con = dbcon.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
            ) {

                ps.setString(1, em);
                ps.setString(2, p);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        int userId = rs.getInt("user_id");
                        String username = rs.getString("username");

                        JOptionPane.showMessageDialog(
                            frame, "Login successful!"
                        );

                        new InboxPage(userId, username);
                        frame.dispose();

                    } else {
                        JOptionPane.showMessageDialog(
                            frame,
                            "Invalid email or password."
                        );
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                    frame,
                    "Database error: " + ex.getMessage()
                );
            }
        });

        createAccount.addActionListener(e -> {
            frame.dispose();
            CreateAccnt.main(null);
        });

        frame.add(panel);
        frame.setVisible(true);
    }
}
