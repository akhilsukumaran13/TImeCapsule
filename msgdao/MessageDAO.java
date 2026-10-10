package msgdao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import db.dbcon;

public class MessageDAO {

    public static void saveMessage(
            int senderId,
            String recipientEmail,
            String message,
            LocalDateTime openingTime) throws Exception {

        try (Connection con = dbcon.getConnection()) {

            // Find the recipient's user ID
            String findUser =
                "SELECT user_id FROM users WHERE email = ?";

            int receiverId;

            try (PreparedStatement ps =
                     con.prepareStatement(findUser)) {

                ps.setString(1, recipientEmail);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception(
                            "Recipient email not found!"
                        );
                    }

                    receiverId = rs.getInt("user_id");
                }
            }

            // Prevent sending a capsule to yourself
            if (senderId == receiverId) {
                throw new Exception(
                    "You cannot send a message to yourself."
                );
            }

            // Save the message in MySQL
            String insert =
                "INSERT INTO messages " +
                "(sender_id, receiver_id, message, opening_time) " +
                "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps =
                     con.prepareStatement(insert)) {

                ps.setInt(1, senderId);
                ps.setInt(2, receiverId);
                ps.setString(3, message);
                ps.setTimestamp(
                    4, Timestamp.valueOf(openingTime)
                );

                ps.executeUpdate();
            }
        }
    }
}
