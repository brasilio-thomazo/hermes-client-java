package br.dev.optimus.hermes.client;

import br.dev.optimus.hermes.client.form.MainForm;
import br.dev.optimus.hermes.grpc.HermesGrpc;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.*;

public class App extends Application {
    private final int pools = Integer.parseInt(Main.properties.getProperty("thread.pools", "5"));
    private final String directoryName = Main.properties.getProperty("watcher.directory", ".");
    private final ExecutorService service = Executors.newFixedThreadPool(pools);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Queue<Runnable> queue = new LinkedList<>();
    private final Stack<DocumentData> stack = new Stack<>();
    private final RunnerStatus status = RunnerStatus.builder().runner(0).total(0).build();
    private StringProperty logProperty;
    private StringProperty statusProperty;
    private Channel channel;
    private HermesGrpc.HermesBlockingStub blockingStub;
    private HermesGrpc.HermesStub stub;

    @Override
    public void start(Stage stage) throws Exception {
        var form = new MainForm(stage);
        logProperty = form.getLogs().textProperty();
        statusProperty = form.getStatus().textProperty();
        var watcher = new DataWatcher(queue, stack, directoryName, channel, blockingStub, stub, status, this::updateLog, this::updateStatus);
        var scheduled =  new QueueScheduled(queue, stack, service, status, this::updateLog, this::updateStatus);
        Scene scene = new Scene(form, 1024.0, 500.0);
        stage.setTitle("Filler");
        stage.setScene(scene);
        stage.show();
        service.submit(watcher);
        scheduler.scheduleAtFixedRate(scheduled, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        service.shutdownNow();
        scheduler.shutdown();
    }

    @Override
    public void init() {
        var host = Main.properties.getProperty("grpc.host", "localhost");
        var port = Integer.parseInt(Main.properties.getProperty("grpc.port", "50051"));
        var credentials = InsecureChannelCredentials.create();
        channel = Grpc.newChannelBuilderForAddress(host, port, credentials).build();
        blockingStub = HermesGrpc.newBlockingStub(channel);
        stub = HermesGrpc.newStub(channel);
    }

    private void updateStatus(RunnerStatus status) {
        updateStatus("runners %d/%d", status.getRunner(), status.getTotal());
    }

    private void updateLog(String format, Object... args) {
        if (Platform.isFxApplicationThread()) {
            var current = logProperty.get();
            var add = String.format(format, args);
            logProperty.set(String.format("%s\n%s", add, current));
            return;
        }
        Platform.runLater(() -> {
            var current = logProperty.get();
            logProperty.set(String.format("%s\n%s", format.formatted(args), current));
        });
    }

    private void updateStatus(String format, Object... args) {
        if (Platform.isFxApplicationThread()) {
            statusProperty.set(format.formatted(args));
            return;
        }
        Platform.runLater(() -> statusProperty.set(format.formatted(args)));
    }
}
