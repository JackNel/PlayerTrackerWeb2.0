package com.theironyard;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Jack on 3/1/16.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        Main.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE users");
        stmt.execute("DROP TABLE players");
        conn.close();
    }

    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Jack", "Jack");
        User user = Main.selectUser(conn, "Jack");
        endConnection(conn);

        assertTrue(user != null);
    }

    @Test
    public void testPlayer() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Jack", "Jack");
        Main.insertPlayer(conn, 1, "Cam Newton", "Panthers", "QB", "Jack", true);
        Player player = Main.selectPlayer(conn, 1);
        endConnection(conn);

        assertTrue(player != null);
    }

    @Test
    public void testPlayers() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Jack", "Jack");
        Main.insertPlayer(conn, 1, "Cam Newton", "Panthers", "QB", "Jack", true);
        Main.insertPlayer(conn, 1, "Micah Hyde", "Packers", "SS", "Jack", true);
        Main.insertPlayer(conn, 1, "Carlos Hyde", "49ers", "HB", "Jack", true);
        ArrayList<Player> playersTestAL = Main.selectPlayers(conn);
        endConnection(conn);

        assertTrue(playersTestAL.size() == 3);
    }

}