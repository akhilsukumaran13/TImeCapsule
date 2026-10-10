package view;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import msgdao.MessageDAO;
public class ComposePage extends JFrame {

    private JTextField recipientField;
    private JTextField openingTimeField;
    private JTextArea messageField;

    private final int senderId;
    private final String username;

    public ComposePage(int senderId, String username) {
    this.senderId = senderId;
    this.username = username;

        setTitle("Time Capsule - Compose");
        setSize(750, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Heading
        JLabel heading = new JLabel("Compose a Time Capsule");
        heading.setFont(new Font("Arial", Font.BOLD, 25));
        heading.setForeground(new Color(35, 80, 150));

        JLabel subtitle = new JLabel(
            "Write a message that can only be opened at the chosen time."
        );
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel headerPanel = new JPanel();
        JLabel userLabel = new JLabel("👤 " + username);
userLabel.setFont(new Font("Arial", Font.BOLD, 14));
userLabel.setHorizontalAlignment(SwingConstants.RIGHT);

JPanel topBar = new JPanel(new BorderLayout());
topBar.setBackground(Color.WHITE);
topBar.add(headerPanel, BorderLayout.CENTER);
topBar.add(userLabel, BorderLayout.EAST);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.add(heading);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitle);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Recipient email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("To (email):"), gbc);

        recipientField = new JTextField();
        recipientField.setFont(new Font("Arial", Font.PLAIN, 15));

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(recipientField, gbc);

        // Opening date and time
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Opens at:"), gbc);

        openingTimeField = new JTextField();
        openingTimeField.setFont(new Font("Arial", Font.PLAIN, 15));
        openingTimeField.setText(
            LocalDateTime.now()
                .plusDays(1)
                .withSecond(0)
                .withNano(0)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        gbc.gridx = 1;
        gbc.weightx = 1;
        formPanel.add(openingTimeField, gbc);

        JLabel formatHint = new JLabel(
            "Format: yyyy-MM-ddTHH:mm  (e.g. 2026-10-15T10:30)"
        );
        formatHint.setFont(new Font("Arial", Font.ITALIC, 11));
        formatHint.setForeground(Color.GRAY);

        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(formatHint, gbc);

        // Message
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        formPanel.add(new JLabel("Message:"), gbc);

        messageField = new JTextArea(10, 30);
        messageField.setFont(new Font("Arial", Font.PLAIN, 15));
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        messageField.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JScrollPane messageScroll = new JScrollPane(messageField);
        messageScroll.setBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210))
        );

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(messageScroll, gbc);

        // Buttons
        JButton backButton = new JButton("Cancel");
        JButton sendButton = new JButton("Send Capsule");

        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(35, 110, 220));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        backButton.addActionListener(e -> dispose());
        sendButton.addActionListener(e -> sendMessage());

        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(backButton);
        buttonPanel.add(sendButton);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void sendMessage() {
        String recipientEmail = recipientField.getText().trim();
        String message = messageField.getText().trim();
        String openingText = openingTimeField.getText().trim();

        if (recipientEmail.isEmpty() || message.isEmpty()
                || openingText.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please fill in the recipient, message, and opening time."
            );
            return;
        }

        try {
            LocalDateTime openingTime =
                LocalDateTime.parse(openingText);

            if (!openingTime.isAfter(LocalDateTime.now())) {
                JOptionPane.showMessageDialog(
                    this,
                    "Opening time must be in the future."
                );
                return;
            }

            MessageDAO.saveMessage(
                senderId,
                recipientEmail,
                message,
                openingTime
            );

            JOptionPane.showMessageDialog(
                this,
                "Your Time Capsule has been sent!"
            );

            dispose();

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Invalid date/time format. Use yyyy-MM-ddTHH:mm"
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not send message: " + ex.getMessage()
            );
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ComposePage(1, "TestUser"));
    }
}
