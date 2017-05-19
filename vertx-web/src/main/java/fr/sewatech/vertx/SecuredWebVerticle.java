package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.htdigest.HtdigestAuth;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class SecuredWebVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.post().handler(
                BodyHandler.create()
                        .setMergeFormAttributes(false)
                        .setDeleteUploadedFilesOnEnd(true)
                        .setUploadsDirectory(".uploads")); // avoid null body

        router.route().handler(CookieHandler.create());

        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));

        HtdigestAuth htdigestAuth = HtdigestAuth.create(vertx, ".config/htdigest");
        router.route("/digest")
                .handler(DigestAuthHandler.create(htdigestAuth));
        router.get("/digest")
                .handler(routingContext -> routingContext.response().end("OK"));

        JsonObject jdbcConfig = new JsonObject().put("url", "jdbc:h2:mem:vertx_db");
        JDBCClient jdbcClient = JDBCClient.createShared(vertx, jdbcConfig);
        JDBCAuth jdbcAuth = JDBCAuth.create(vertx, jdbcClient);
        router.route("/basic")
                .handler(BasicAuthHandler.create(jdbcAuth).addAuthority("role:admin"));
        router.get("/basic")
                .handler(ctx -> ctx.response()
                        .end("OK:"
                                + ctx.user().principal().getValue("username")));

        JsonObject jwtConfig = new JsonObject().put(
                "keyStore",
                new JsonObject()
                        .put("path", ".config/jwt.jks")
                        .put("type", "jceks")
                        .put("password", "secret"));
        JWTAuth jwtAuth = JWTAuth.create(vertx, jwtConfig);
        router.route("/jwt")
                .handler(JWTAuthHandler.create(jwtAuth, "/jwt0").addAuthority("jwt"));
        router.get("/jwt")
                .handler(ctx -> ctx.response()
                        .end("OK:"
                                + ctx.user().principal().getValue("username")));
        router.route("/jwt0")
                .handler(BasicAuthHandler.create(jdbcAuth));
        router.get("/jwt0")
                .handler(ctx -> {
                    String token = jwtAuth.generateToken(
                            ctx.user()
                                    .principal()
                                    .put("permissions", new JsonArray().add("jwt")),
                            new JWTOptions());
                    ctx.response().end(token);
                });

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8002);
        System.out.println("Listening on port 8002");
    }

}
