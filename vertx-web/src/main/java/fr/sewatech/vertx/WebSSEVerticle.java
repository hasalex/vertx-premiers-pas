package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.stream.IntStream;

public class WebSSEVerticle extends AbstractVerticle {

    private static final String SSE_TEMPLATE = "id: %s\nretry: %s\nevent: %s\ndata: %s\n\n";

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.get("/chunk").handler(
                context -> partialResponse(context)
                        .write("Single chunk\n")
        );

        router.get("/sse").handler(ctx -> {
            HttpServerResponse response = partialResponse(ctx);
            IntStream.iterate(0, i -> ++i)
                    .limit(10)
                    .forEach(i ->
                            response.write(
                                    String.format(SSE_TEMPLATE, i, 3_000, "message", "Message " + i)
                            ));
            response.end();
        });

        router.get("/messages").handler(ctx -> {
            HttpServerResponse response = partialResponse(ctx);
            String address = "swt.messages";
            DeliveryOptions options = new DeliveryOptions();
            options.addHeader("reply-address", address + ".reply." + response.hashCode());
            options.addHeader("end-address", address + ".end" + response.hashCode());

            EventBus eventBus = vertx.eventBus();
            MessageConsumer<JsonObject> replyConsumer = eventBus.consumer(
                    options.getHeaders().get("reply-address"),
                    event0 -> response.write(event0.body() + "\n"));
            MessageConsumer<JsonObject> endConsumer = eventBus.consumer(
                    options.getHeaders().get("end-address"));
            endConsumer.handler(
                    event0 -> {
                        response.end();
                        replyConsumer.unregister();
                        endConsumer.unregister();
                    });
            eventBus.<Void>send(address, null, options);
        });

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8005);
        System.out.println(this.getClass() + " : Listening on port 8005");
    }

    private HttpServerResponse partialResponse(RoutingContext ctx) {
        HttpServerResponse response = ctx.request().response();
        response.setChunked(true);
        response.headers().add("Content-Type", "text/event-stream")
                .add("Cache-Control", "no-cache")
                .add("Connection", "keep-alive");
        return response;
    }

}
