package view;


import javax.swing.*;

import db.dbcon;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateAccnt {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Create Account");
        frame.setSize(350, 280);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        JTextField username = new JTextField();
        panel.add(username);

        panel.add(new JLabel("Email:"));
        JTextField email = new JTextField();
        panel.add(email);

        panel.add(new JLabel("Password:"));
        JPasswordField password = new JPasswordField();
        panel.add(password);

        JButton create = new JButton("Create Account");
        JButton backToLogin = new JButton("Login");

        panel.add(create);
        panel.add(backToLogin);

        backToLogin.addActionListener(e -> {
            frame.dispose();
            login.main(null);
        });

        frame.add(panel);
        frame.setVisible(true);

        create.addActionListener(e -> {
    String u = username.getText().trim();
    String em = email.getText().trim();
    String p = new String(password.getPassword());

    if (u.isEmpty() || em.isEmpty() || p.isEmpty()) {
        JOptionPane.showMessageDialog(
            frame, "Please fill all fields"
        );
        return;
    }

    try (Connection con = dbcon.getConnection();
         PreparedStatement ps = con.prepareStatement(
             "INSERT INTO users (username, email, password) VALUES (?, ?, ?)"
         )) {

        ps.setString(1, u);
        ps.setString(2, em);
        ps.setString(3, p);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            JOptionPane.showMessageDialog(
                frame, "Account created successfully!"
            );
        }

    } catch (Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
            frame,
            "Account creation failed:\n" + ex.getMessage()
        );
    }
});
    }
}
