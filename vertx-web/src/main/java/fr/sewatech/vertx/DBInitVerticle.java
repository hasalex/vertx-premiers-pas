package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

import static java.util.Arrays.asList;

public class DBInitVerticle extends AbstractVerticle {

    private static final String DB_URL = "jdbc:h2:mem:vertx_db2";
    private JDBCClient jdbcClient;

    @Override
    public void start() throws Exception {
        JsonObject jdbcConfig = new JsonObject().put("url", DB_URL);
        jdbcClient = JDBCClient.createShared(vertx, jdbcConfig);

        jdbcClient.getConnection(
                res -> res.result().setAutoCommit(
                        false,
                        result -> doTheJob(res)
                ));
    }

    private void doTheJob(AsyncResult<SQLConnection> result) {
        if (result.failed()) {
            result.cause().printStackTrace();
            return;
        }

        SQLConnection connection = result.result();

        CompletableFuture<AsyncResult<?>> cf1 = new CompletableFuture<>();
        cf1.handle(this::handle);
        CompletableFuture<AsyncResult<?>> cf2 = new CompletableFuture<>();
        cf2.handle(this::handle);
        CompletableFuture<AsyncResult<?>> cf3 = new CompletableFuture<>();
        cf3.handle(this::handle);
        CompletableFuture<AsyncResult<?>> cf4 = new CompletableFuture<>();
        cf4.handle(this::handle);
        CompletableFuture<Void> cf = CompletableFuture.allOf(cf1, cf2, cf3, cf4);
        cf.handle((aVoid, throwable) -> connection.commit(event -> connection.close()));
//        cf.complete(null);

        connection.batch(
                asList(
                        "create table user (username varchar(255), password varchar(255), password_salt varchar(255))",
                        "create table user_roles (username varchar(255), role varchar(255))",
                        "create table roles_perms (role varchar(255), perm varchar(255))"
                ),
                cf1::complete);

        connection.batchWithParams(
                "insert into user (username, password, password_salt) values (?, ?, ?)",
                asList(
                        new JsonArray().add("alexis").add(digest("aa")).add(""),
                        new JsonArray().add("bob").add(digest("bb")).add("")
                ),
                cf2::complete);

        connection.updateWithParams(
                "insert into user_roles (username, role) values (?, ?)",
                new JsonArray().add("bob").add("admin"),
                cf3::complete);

        connection.updateWithParams(
                "insert into roles_perms (role, perm) values (?, ?)",
                new JsonArray().add("admin").add("admin"),
                cf4::complete);

    }

    private void complete(AsyncResult<UpdateResult> asyncResult, SQLConnection connection) {
        if (asyncResult.succeeded()) {
            connection.commit(event -> connection.close());
        } else {
            connection.rollback(event -> connection.close());
        }
    }

    private Void handle(AsyncResult<?> result, Throwable throwable) {
        if (result.succeeded()) {
            System.out.println("SQL query OK");
        } else {
            System.out.println("SQL query Fail");
            result.cause().printStackTrace();
        }
        return null;
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
        jdbcClient.close();
    }
}
