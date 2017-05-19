package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;

public class MvnVerticle extends AbstractVerticle {

    @Override
    public void start() {
        int port = 8009;
        vertx.createHttpServer()
                .requestHandler(request -> request.response().end("Hello from 8009\n"))
                .listen(port, "127.0.0.1");
        System.out.println("Listening on port " + port + "!!");
    }
}
