package br.dev.optimus.hermes.client;

import lombok.RequiredArgsConstructor;

import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class QueueScheduled implements Runnable {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final Queue<Runnable> queue;
    private final Stack<DocumentData> stack;
    private final ExecutorService service;
    private final RunnerStatus status;
    private final UpdateLogger updateLogger;
    private final UpdateStatus updateStatus;

    @Override
    public void run() {
        logger.info("queue size: %s".formatted(queue.size()));
        // updateLogger.apply("add %d task in queue", queue.size());
        if (queue.isEmpty() && stack.isEmpty()) return;
        if (queue.isEmpty() && status.getRunner() == status.getTotal()) {
            while (!stack.isEmpty()) {
                var task = service.submit(new SaveDocument(stack.pop(), updateLogger));
                while (true) if (task.isDone()) break;
            }
        }
        while (!queue.isEmpty()) {
            service.submit(queue.poll());
            status.plusTotal();
            updateStatus.apply(status);
            try {
                Thread.sleep(100);
            }
            catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }
    }
}
