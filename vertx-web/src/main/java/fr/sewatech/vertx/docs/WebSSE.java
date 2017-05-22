package fr.sewatech.vertx.docs;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

import java.util.stream.IntStream;

public class WebSSE extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        chunk(router);
        sse(router);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8005);
        System.out.println(this.getClass() + " : Listening on port 8005");
    }

    private void chunk(Router router) {
        router.get("/chunk").handler(
                contextHandler -> {
                    HttpServerResponse response = contextHandler.request().response();
                    response.setChunked(true)
                            .headers()
                                .add("Content-Type", "text/event-stream")
                                .add("Cache-Control", "no-cache")
                                .add("Connection", "keep-alive");
                    IntStream.iterate(0, i -> ++i)
                            .limit(10)
                            .forEach(i -> response.write("Single chunk number" + i + "\n"));
                }
        );
    }

    private void sse(Router router) {
        String sseTemplate = "id: %s\nretry: %s\nevent: %s\ndata: %s\n\n";
        router.get("/sse").handler(ctx -> {
            HttpServerResponse response = ctx.request().response();
            response.setChunked(true)
                    .headers()
                        .add("Content-Type", "text/event-stream")
                        .add("Cache-Control", "no-cache")
                        .add("Connection", "keep-alive");
            IntStream.iterate(0, i -> ++i)
                    .limit(10)
                    .forEach(i -> {
                        response.write(
                                String.format(sseTemplate, i, 3_000, "message", "Message " + i)
                        );
                    });
            response.end();
        });
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new WebSSE());
    }

}
