package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbcon {

    public static Connection getConnection() throws Exception {

        String url = "jdbc:mysql://localhost:3306/timemesg";
        String username = "root";
        String password = "password";

        return DriverManager.getConnection(
            url, username, password
        );
    }
}
