package fr.sewatech.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;

public class BlockingVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        long sleepDuration = 5000;
//        vertx.setPeriodic(sleepDuration * 2, event -> System.out.println(sleep(sleepDuration)));
        printThread();
        System.out.println();
        vertx.setPeriodic(sleepDuration * 2, event -> sleepBlocking(sleepDuration));
    }

    private String sleep(long duration) {
        try {
            System.out.println(String.format("Start sleeping for %s ms", duration));
            printThread();
//            sleepBlocking(100);
            Thread.sleep(duration);
            return "End sleeping";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Problem while sleeping";
        }
    }

    private void sleepBlocking(long duration) {
//        vertx.executeBlocking(
//                event -> event.complete(sleep(duration)),
//                event -> System.out.println(event.result().toString()));

        WorkerExecutor executor = vertx.createSharedWorkerExecutor("toto");
        executor.executeBlocking(
                event -> event.complete(sleep(duration)),
                event -> System.out.println(event.result().toString()));
    }

    private void printThread() {
        System.out.println(Thread.currentThread().getName());
    }
}
