package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

public class SimpleVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(request -> request.response().end("Hello\n"))
                .listen(8001, "127.0.0.1");
        System.out.println(this.getClass() + " : Listening on port 8001");
    }

    private void request(HttpServerRequest request) {
        if (isUpload(request)) {
            upload(request);
        } else {
            defaultRequest(request);
        }
    }

    private void upload(HttpServerRequest request) {
        request.setExpectMultipart(true); //Will expect a form
        request .uploadHandler(event -> System.out.println("form " + request.formAttributes()))
                .response()
                .end();
    }

    private boolean isUpload(HttpServerRequest request) {
        return request.uri().startsWith("/upload") && (request.method() == HttpMethod.POST);
    }

    private void defaultRequest(HttpServerRequest request) {
        switch (request.method()) {
            case GET:
                request.response()
                        .putHeader("content-type", "text/plain")
                        .end("OK from Vert.X");
                break;
            case POST:
                request.bodyHandler(buffer -> System.out.println(buffer.toString()))
                        .endHandler(event -> System.out.println(request.formAttributes().size()))
                        .response()
                        .putHeader("content-type", "text/plain")
                        .end("OK from Vert.X");
                break;
        }
    }

}
