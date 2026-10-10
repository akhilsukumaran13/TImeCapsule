package view;
import msgdao.InboxDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InboxPage extends JFrame {

    private final int currentUserId;
    private JPanel contactsPanel;
    private JPanel chatPanel;
    private JLabel chatTitle;
    private int selectedContactId = -1;

    private final Color background = new Color(245, 247, 250);
    private final Color blue = new Color(35, 110, 220);

    private final String username;

public InboxPage(int currentUserId, String username) {
    this.currentUserId = currentUserId;
    this.username = username;

        setTitle("Time Capsule - Inbox");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Left panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(290, 650));
        leftPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("  Time Capsule");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setBorder(new EmptyBorder(20, 10, 20, 10));

        contactsPanel = new JPanel();
        contactsPanel.setLayout(
            new BoxLayout(contactsPanel, BoxLayout.Y_AXIS)
        );
        contactsPanel.setBackground(Color.WHITE);

        JScrollPane contactsScroll = new JScrollPane(contactsPanel);
        contactsScroll.setBorder(null);

        JButton composeButton = new JButton("+ Compose");
        composeButton.setFont(new Font("Arial", Font.BOLD, 15));
        composeButton.setBackground(blue);
        composeButton.setForeground(Color.WHITE);
        composeButton.setFocusPainted(false);

        composeButton.addActionListener(e ->
            new ComposePage(currentUserId,username)
        );

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadContacts());

        JPanel bottom = new JPanel(new GridLayout(1, 2, 8, 0));
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottom.setBackground(Color.WHITE);
        bottom.add(composeButton);
        bottom.add(refreshButton);

        leftPanel.add(title, BorderLayout.NORTH);
        leftPanel.add(contactsScroll, BorderLayout.CENTER);
        leftPanel.add(bottom, BorderLayout.SOUTH);

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(background);

        chatTitle = new JLabel("  Select a conversation");
        chatTitle.setFont(new Font("Arial", Font.BOLD, 20));
        chatTitle.setOpaque(true);
        chatTitle.setBackground(Color.WHITE);
        chatTitle.setBorder(new EmptyBorder(20, 10, 20, 10));
        JPanel topBar = new JPanel(new BorderLayout());
topBar.setBackground(Color.WHITE);
topBar.setBorder(
    new javax.swing.border.EmptyBorder(10, 15, 10, 15)
);

chatTitle.setBorder(
    new javax.swing.border.EmptyBorder(10, 0, 10, 0)
);

JLabel userLabel = new JLabel("👤 " + username);
userLabel.setFont(new Font("Arial", Font.BOLD, 14));

topBar.add(chatTitle, BorderLayout.CENTER);
topBar.add(userLabel, BorderLayout.EAST);

rightPanel.add(topBar, BorderLayout.NORTH);
        chatPanel = new JPanel();
        chatPanel.setLayout(
            new BoxLayout(chatPanel, BoxLayout.Y_AXIS)
        );
        chatPanel.setBackground(background);
        chatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

      
        rightPanel.add(new JScrollPane(chatPanel), BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        loadContacts();
        setVisible(true);
    }

    private void loadContacts() {
        contactsPanel.removeAll();

        try {
            List<InboxDAO.Contact> contacts =
                InboxDAO.getContacts(currentUserId);

            if (contacts.isEmpty()) {
                contactsPanel.add(new JLabel("  No conversations yet."));
            }

            for (InboxDAO.Contact contact : contacts) {
                String preview = contact.lastMessage;

                JButton button = new JButton(
                    "<html><b>" + escapeHtml(contact.username) +
                    "</b><br><small>" + escapeHtml(preview) +
                    "</small></html>"
                );

                button.setHorizontalAlignment(SwingConstants.LEFT);
                button.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 65)
                );
                button.setPreferredSize(new Dimension(270, 65));
                button.setBackground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorder(new EmptyBorder(10, 12, 10, 8));

                button.addActionListener(e -> {
                    selectedContactId = contact.userId;
                    showConversation(contact.userId, contact.username);
                });

                contactsPanel.add(button);
            }

        } catch (Exception ex) {
            showError("Could not load conversations", ex);
        }

        contactsPanel.revalidate();
        contactsPanel.repaint();
    }

    private void showConversation(int otherUserId, String username) {
        chatTitle.setText("  " + username);
        chatPanel.removeAll();

        try {
            List<InboxDAO.Message> messages =
                InboxDAO.getConversation(currentUserId, otherUserId);

            for (InboxDAO.Message message : messages) {
                boolean sentByMe = message.senderId == currentUserId;

                if (message.isUnlocked()) {
                    addBubble(
                        message.message,
                        sentByMe,
                        message.sentAt.format(
                            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                        )
                    );
                } else {
                    String openingTime = message.openingTime.format(
                        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
                    );

                    addLockedBubble(openingTime, sentByMe);
                }
            }

        } catch (Exception ex) {
            showError("Could not load messages", ex);
        }

        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private void addBubble(
            String text, boolean sentByMe, String sentAt) {

        JPanel row = new JPanel(new FlowLayout(
            sentByMe ? FlowLayout.RIGHT : FlowLayout.LEFT
        ));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(
            sentByMe ? new Color(210, 240, 210) : Color.WHITE
        );
        bubble.setBorder(new EmptyBorder(10, 12, 8, 12));

        JTextArea content = new JTextArea(text);
        content.setEditable(false);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);
        content.setFont(new Font("Arial", Font.PLAIN, 14));
        content.setBackground(bubble.getBackground());
        content.setBorder(null);
        content.setColumns(28);

        JLabel time = new JLabel(sentAt);
        time.setFont(new Font("Arial", Font.PLAIN, 10));
        time.setForeground(Color.GRAY);
        time.setAlignmentX(Component.RIGHT_ALIGNMENT);

        bubble.add(content);
        bubble.add(Box.createVerticalStrut(5));
        bubble.add(time);

        row.add(bubble);
        chatPanel.add(row);
    }

    private void addLockedBubble(String openingTime, boolean sentByMe) {
        JPanel row = new JPanel(new FlowLayout(
            sentByMe ? FlowLayout.RIGHT : FlowLayout.LEFT
        ));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton locked = new JButton(
            "<html><b>Locked message</b><br>" +
            "Opens: " + openingTime + "</html>"
        );

        locked.setHorizontalAlignment(SwingConstants.LEFT);
        locked.setBackground(new Color(255, 248, 220));
        locked.setBorder(new EmptyBorder(12, 12, 12, 12));

        locked.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "This message cannot be opened yet.\nOpening time: " +
                    openingTime,
                "Time Capsule",
                JOptionPane.INFORMATION_MESSAGE
            )
        );

        row.add(locked);
        chatPanel.add(row);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    private void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
            this,
            message + ": " + ex.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        // Test only: replace 1 with an existing user_id.
        SwingUtilities.invokeLater(() -> new InboxPage(1, "TestUser  "));
    }
}
