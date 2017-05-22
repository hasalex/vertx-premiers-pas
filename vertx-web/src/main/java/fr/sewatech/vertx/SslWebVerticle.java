package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class SslWebVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.post().handler(BodyHandler.create());
        router.route().handler(context -> context.response().end("OK"));

        JksOptions jksOptions = new JksOptions()
                .setPath(".config/ssl.jks")
                .setPassword("secret");
        HttpServerOptions options = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(jksOptions);

        vertx.createHttpServer(options)
                .requestHandler(router::accept)
                .listen(8003);
        System.out.println(this.getClass() + " : Listening on port 8003");
    }

}
