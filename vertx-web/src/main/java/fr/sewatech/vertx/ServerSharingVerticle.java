package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;

public class ServerSharingVerticle extends AbstractVerticle{

    @Override
    public void start() throws Exception {
        vertx.createHttpServer(new HttpServerOptions().setLogActivity(true))
                .requestHandler(request -> request.response().end("Hello 1\n"))
                .listen(8111, "127.0.0.1");

        vertx.createHttpServer(new HttpServerOptions().setLogActivity(true))
                .requestHandler(request -> request.response().end("Hello 2\n"))
                .listen(8111, "127.0.0.1");
    }
}
