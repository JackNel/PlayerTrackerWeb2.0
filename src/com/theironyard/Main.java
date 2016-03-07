package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jack on 3/1/16.
 */
public class Main {
    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS players (id IDENTITY, user_id INT, name VARCHAR, team VARCHAR, position VARCHAR, creator VARCHAR, isCreator BOOLEAN)");
    }

    public static void insertUser(Connection conn, String username, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("Insert INTO users VALUES (NULL, ?, ?)");
        stmt.setString(1, username);
        stmt.setString(2, password);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        ResultSet results = stmt.executeQuery();
        User user = null;
        if (results.next()) {
            user = new User();
            user.id = results.getInt("id");
            user.username = results.getString("username");
            user.password = results.getString("password");
        }
        return user;
    }

    public static void insertPlayer(Connection conn, int user_id, String name, String team, String position, String creator, boolean isCreator) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO players VALUES (NULL, ?, ?, ?, ?, ?, ?)");
        stmt.setInt(1, user_id);
        stmt.setString(2, name);
        stmt.setString(3, team);
        stmt.setString(4, position);
        stmt.setString(5, creator);
        stmt.setBoolean(6, isCreator);
        stmt.execute();
    }

    public static Player selectPlayer(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players INNER JOIN users ON players.user_id = users.id WHERE players.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        Player player = new Player();
        if(results.next()) {
            player.id = results.getInt("players.id");
            player.name = results.getString("players.name");
            player.team = results.getString("players.team");
            player.position = results.getString("players.position");
            player.creator = results.getString("players.creator");
        }
        return player;
    }

    public static ArrayList<Player> selectPlayers(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM players INNER JOIN users ON players.user_id = users.id");
        ResultSet results = stmt.executeQuery();
        ArrayList<Player> playersAL = new ArrayList<>();
        while (results.next()) {
            Player player = new Player();
            player.id = results.getInt("players.id");
            player.name = results.getString("players.name");
            player.team = results.getString("players.team");
            player.position = results.getString("players.position");
            player.creator = results.getString("players.creator");

            playersAL.add(player);
        }
        return playersAL;
    }

    public static void updateBoolean (Connection conn, boolean isCreator, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE players SET isCreator = ? WHERE id = ?");
        stmt.setBoolean(1, isCreator);
        stmt.setInt(2, id);
        stmt.execute();
    }







    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.get (
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    ArrayList<Player> playersAL = selectPlayers(conn);


                    for (Player player : playersAL) {
                        if (player.creator.equals(username)) {
                            player.isCreator = true;
                        }
                    }

                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("players", playersAL);
                    m.put("isCreator", playersAL);

                    return new ModelAndView(m, "/home.html");
                }),
                new MustacheTemplateEngine()
        );

        Spark.post (
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    String password = request.queryParams("password");

                    if (username.isEmpty() || password.isEmpty()) {
                        System.out.println("Need a name and a password");
                        Spark.halt(403);
                    }

                    User user = selectUser(conn, username);
                    if (user == null) {
                        insertUser(conn, username, password);
                    }
                    else if (!password.equals(user.password)) {
                        System.out.println("Wrong password");
                        Spark.halt(403);
                    }

                    Session session = request.session();
                    session.attribute("username", username);

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post (
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post (
                "/create-entry",
                ((request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    if (username == null) {
                        System.out.println("Username = null");
                        Spark.halt(403);
                    }

                    String name = request.queryParams("playerName");
                    String team = request.queryParams("playerTeam");
                    String position = request.queryParams("position");

                    try {
                        User user = selectUser(conn, username);
                        insertPlayer(conn, user.id, name, team, position, user.username, true);
                    } catch (Exception e) {
                    }

                    response.redirect(request.headers("Referer"));
                    return "";
                })
        );
    }
}
