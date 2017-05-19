package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseVerticle extends AbstractVerticle {

    public static final String DB_URL = "jdbc:h2:mem:vertx_db";
    private Connection connection;

    @Override
    public void start() throws Exception {
        vertx.executeBlocking(event -> startDatabase(), null);
    }

    private void startDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            PreparedStatement createUser = connection.prepareStatement("create table user (username varchar(255), password varchar(255), password_salt varchar(255))");
            createUser.executeUpdate();
            createUser.close();
            PreparedStatement createRoles = connection.prepareStatement("create table user_roles (username varchar(255), role varchar(255))");
            createRoles.executeUpdate();
            createRoles.close();
            PreparedStatement createPerms = connection.prepareStatement("create table roles_perms (role varchar(255), perm varchar(255))");
            createPerms.executeUpdate();
            createPerms.close();

            PreparedStatement insertUser = connection.prepareStatement("insert into user (username, password, password_salt) values (?, ?, ?)");
            executeStatement(insertUser, "alexis", digest("aa"), "");
            executeStatement(insertUser, "bob", digest("bb"), "");
            insertUser.close();

            PreparedStatement insertRoles = connection.prepareStatement("insert into user_roles (username, role) values (?, ?)");
            executeStatement(insertRoles, "bob", "admin");
            insertRoles.close();

            PreparedStatement insertPerms = connection.prepareStatement("insert into roles_perms (role, perm) values (?, ?)");
            executeStatement(insertPerms, "admin", "admin");
            insertRoles.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void executeStatement(PreparedStatement insertUser, String... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            insertUser.setString(i+1, parameters[i]);
        }
        insertUser.executeUpdate();
    }

    private String digest(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte x : bytes) {
                sb.append(String.format("%02X", x));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws Exception {
        connection.close();
    }
}
