package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import static java.util.Arrays.asList;

public class MessageService extends AbstractVerticle {

    private static final String DB_URL = "jdbc:h2:mem:msg";

    private JDBCClient jdbcClient;

    @Override
    public void start() throws Exception {
        vertx.eventBus().<String>consumer("swt.msg")
                .handler(message -> answer(1, message));

        vertx.eventBus().<String>consumer("swt.msg")
                .handler(message -> answer(2, message));

        vertx.eventBus().<String>consumer("swt.msg")
                .handler(message -> answer(3, message));

        vertx.eventBus().<Hello>consumer("swt.hello")
                .handler(this::answer);

        initDB();
        vertx.eventBus().<String>consumer("swt.messages")
                .handler(message -> getConnection(res -> messages(res.result(), message, vertx.eventBus())));
    }

    private void initDB() {
        jdbcClient = JDBCClient.createShared(vertx, new JsonObject().put("url", DB_URL));

        getConnection(
                res -> res.result().update(
                        "create table hello (id number(32), who varchar(255), text varchar(255))",
                        event -> res.result().batchWithParams(
                                "insert into hello (id, who, text) values (?, ?, ?)",
                                asList(
                                        new JsonArray().add(1).add("Alexis").add("Bonjour"),
                                        new JsonArray().add(2).add("Bob").add("Hello"),
                                        new JsonArray().add(2).add("Alice").add("Hi")
                                ),
                                x1 -> res.result().commit(
                                        x2 -> res.result().close()
                                )
                        )
                )
        );
    }

    private void getConnection(Handler<AsyncResult<SQLConnection>> connectionHandler) {
        jdbcClient.getConnection(
                res -> res.result().setAutoCommit(
                        false,
                        event -> connectionHandler.handle(res)
                ));
    }

    private void messages(SQLConnection connection, Message<String> message, EventBus eventBus) {
        Handler<JsonArray> rowHandler = array ->
                eventBus.send(message.headers().get("reply-address"),
                        new JsonObject()
                                .put("id", array.getLong(0))
                                .put("who", array.getString(1))
                                .put("text", array.getString(2))
                );
        Handler<Void> endHandler = end -> {
            connection.close();
            eventBus.send(message.headers().get("end-address"), new JsonObject());
        };

        connection.queryStream(
                "select id, who, text from hello",
                rowResult ->
                        rowResult.result()
                                .handler(rowHandler)
                                .endHandler(endHandler)
        );
    }

    private void answer(int number, Message<String> message) {
        System.out.println(number + ":" + message.body());
        message.reply("OK " + number + message.body());
    }

    private void answer(Message<Hello> message) {
        System.out.println(message.body());
        message.reply("OK : " + message.body());
    }
}
