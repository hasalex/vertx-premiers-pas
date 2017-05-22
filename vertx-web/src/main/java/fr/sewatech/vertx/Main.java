package fr.sewatech.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.lang.management.ManagementFactory;

public class Main {

    public static void main(String[] args) {
        VertxOptions options = new VertxOptions();

        // Cool for debug, bad in production
        if (isDebug()) {
            System.out.println("Debug detected");
            options.setBlockedThreadCheckInterval(1_000_000L);
//            options.setMaxEventLoopExecuteTime(500_000_000L);
//            options.setMaxWorkerExecuteTime(1_000_000L);
        }

        Vertx vertx = Vertx.vertx(options);
//        vertx.deployVerticle(new DBInitVerticle(), new DeploymentOptions().setWorker(true));
//        vertx.deployVerticle(new DBInitVerticle());
//        vertx.deployVerticle(SimpleVerticle.class.getName(), new DeploymentOptions().setInstances(4));
//        vertx.deployVerticle(new WebVerticle());
//        vertx.deployVerticle(new SecuredWebVerticle());
//        vertx.deployVerticle(new SslWebVerticle());
//        vertx.deployVerticle(new BlockingVerticle(), new DeploymentOptions().setWorker(false));
//        vertx.deployVerticle(new ServerSharingVerticle());

        vertx.deployVerticle(new MessageService());
        vertx.deployVerticle(new WebSSEVerticle());

    }

    private static boolean isDebug() {
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments()
                .stream()
                .anyMatch(arg -> arg.startsWith("-agentlib:jdwp"));
    }
}
