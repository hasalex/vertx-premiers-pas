package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class WebVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route("/hello")
                .handler(routingContext -> routingContext.response().end("Hello World\n"));
        router.get("/hello/:name").handler(this::hello);

        router.post().handler(
                BodyHandler.create()
                        .setMergeFormAttributes(false)
                        .setDeleteUploadedFilesOnEnd(true)
                        .setUploadsDirectory(".uploads")); // avoid null body
        router.post("/update").handler(this::update);
        router.post("/form").handler(this::form);
        router.post("/upload").handler(this::upload);

        router.route().handler(CookieHandler.create());
        router.get("/cookie").handler(this::cookie);

        SessionStore sessionStore = LocalSessionStore.create(vertx, "sessions", 10);
//        SessionStore sessionStore = ClusteredSessionStore.create(vertx);
        router.route().handler(SessionHandler.create(sessionStore));
        router.get("/session").handler(this::session);

        // otherwise serve static pages
        router.route().handler(StaticHandler.create());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8002);
        System.out.println("Listening on port 8002");
    }

    private void hello(RoutingContext routingContext) {
        String name = routingContext.request().getParam("name");
        routingContext.response().end("Hello " + name + "\n");
    }

    private void update(RoutingContext routingContext) {
        routingContext.getBodyAsJson()
                .stream()
                .filter(entry -> entry.getKey().equals("name"))
                .findFirst()
                .map(entry -> routingContext.response()
                        .putHeader("Content-Length", "" + (entry.getValue().toString().length() + 1))
                        .write(entry.getValue().toString() + "\n")
                )
                .orElse(routingContext.response().setStatusCode(404))
                .end();
    }

    private void form(RoutingContext routingContext) {
        MultiMap attributes = routingContext.request().formAttributes();
        routingContext.response()
                .end(attributes.toString());
    }

    private void upload(RoutingContext routingContext) {
        routingContext.fileUploads()
                .stream()
                .map(FileUpload::uploadedFileName)
                .forEach(System.out::println);
        routingContext.response().end();
    }

    private void cookie(RoutingContext routingContext) {
        Cookie cookie = routingContext.getCookie("call-no");
        int value;
        if (cookie == null) {
            value = 1;
        } else {
            value = Integer.parseInt(cookie.getValue()) + 1;
        }
        String textValue = String.valueOf(value);
        Cookie responseCookie = Cookie.cookie("call-no", textValue);
        routingContext.addCookie(responseCookie);

        System.out.println(textValue);
        routingContext.response().end(textValue);
    }

    private void session(RoutingContext routingContext) {
        Session session = routingContext.session();
        Integer value = session.get("call-no");
        value = (value == null ? 1 : ++value);

        session.put("call-no", value);
        routingContext.response().end(value.toString());
    }
}
