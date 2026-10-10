package msgdao;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import db.dbcon;
public class InboxDAO {

    public static class Contact {
        public int userId;
        public String username;
        public String lastMessage;
        public LocalDateTime openingTime;

        public Contact(int userId, String username,
                       String lastMessage, LocalDateTime openingTime) {
            this.userId = userId;
            this.username = username;
            this.lastMessage = lastMessage;
            this.openingTime = openingTime;
        }
    }

    public static class Message {
        public int senderId;
        public String message;
        public LocalDateTime openingTime;
        public LocalDateTime sentAt;

        public Message(int senderId, String message,
                       LocalDateTime openingTime, LocalDateTime sentAt) {
            this.senderId = senderId;
            this.message = message;
            this.openingTime = openingTime;
            this.sentAt = sentAt;
        }

        public boolean isUnlocked() {
            return !LocalDateTime.now().isBefore(openingTime);
        }
    }

    // Retrieve all people who have exchanged messages with this user
    public static List<Contact> getContacts(int currentUserId)
            throws Exception {

        List<Contact> contacts = new ArrayList<>();

        String sql =
            "SELECT u.user_id, u.username, m.message, m.opening_time " +
            "FROM messages m " +
            "JOIN users u ON u.user_id = " +
            "CASE WHEN m.sender_id = ? " +
            "THEN m.receiver_id ELSE m.sender_id END " +
            "WHERE m.sender_id = ? OR m.receiver_id = ? " +
            "ORDER BY m.sent_at DESC";

        Set<Integer> added = new HashSet<>();

        try (Connection con = dbcon.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId);
            ps.setInt(3, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int otherUserId = rs.getInt("user_id");

                    if (added.add(otherUserId)) {
                        String message = rs.getString("message");

                        Timestamp openingTimestamp =
                            rs.getTimestamp("opening_time");

                        LocalDateTime openingTime =
                            openingTimestamp.toLocalDateTime();

                        // Never expose locked message text in the preview
                        if (LocalDateTime.now().isBefore(openingTime)) {
                            message = "🔒 Locked message";
                        } else if (message.length() > 35) {
                            message = message.substring(0, 35) + "...";
                        }

                        contacts.add(new Contact(
                            otherUserId,
                            rs.getString("username"),
                            message,
                            openingTime
                        ));
                    }
                }
            }
        }

        return contacts;
    }

    // Retrieve the full conversation in chronological order
    public static List<Message> getConversation(
            int currentUserId, int otherUserId) throws Exception {

        List<Message> messages = new ArrayList<>();

        String sql =
            "SELECT sender_id, message, opening_time, sent_at " +
            "FROM messages " +
            "WHERE (sender_id = ? AND receiver_id = ?) " +
            "OR (sender_id = ? AND receiver_id = ?) " +
            "ORDER BY sent_at ASC";

        try (Connection con = dbcon.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.setInt(2, otherUserId);
            ps.setInt(3, otherUserId);
            ps.setInt(4, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(new Message(
                        rs.getInt("sender_id"),
                        rs.getString("message"),
                        rs.getTimestamp("opening_time").toLocalDateTime(),
                        rs.getTimestamp("sent_at").toLocalDateTime()
                    ));
                }
            }
        }

        return messages;
    }
}
