package br.dev.optimus.hermes.client;

import br.dev.optimus.hermes.client.model.Document;
import br.dev.optimus.hermes.grpc.HermesGrpc;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Channel;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DataWatcher implements Runnable {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Queue<Runnable> queue;
    private final Stack<DocumentData> stack;
    private final String directoryName;
    private final Channel channel;
    private final HermesGrpc.HermesBlockingStub blockingStub;
    private final HermesGrpc.HermesStub stub;
    private final RunnerStatus status;
    private final UpdateLogger updateLogger;
    private final UpdateStatus updateStatus;
    @Setter
    private boolean stop = false;

    @Override
    public void run() {
        try {
            var path = Paths.get(new File(directoryName).getAbsolutePath());
            WatchService service = FileSystems.getDefault().newWatchService();
            path.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            updateLogger.apply("watching path [%s/data*.json]", path.toFile().getAbsolutePath());
            var pattern = Pattern.compile("^data(.+?)\\.json$");
            while (!this.stop) {
                System.out.println(".");
                var key = service.take();
                for (var evt : key.pollEvents()) {
                    var kind = evt.kind();
                    var context = evt.context();
                    var file = path.resolve((Path) context);
                    if (kind != StandardWatchEventKinds.ENTRY_MODIFY && kind != StandardWatchEventKinds.ENTRY_CREATE) {
                        continue;
                    }
                    if (!Files.isRegularFile(file) || !pattern.matcher(file.toFile().getName()).find()) {
                        continue;
                    }

                    if (exists(file.toFile())) {
                        continue;
                    }
                    updateLogger.apply("file [%s] updated/created", file.getFileName());
                    makeQueue(file.toFile());
                }
                if (!key.reset()) {
                    break;
                }
            }
        } catch (InterruptedException | IOException ex) {
            updateLogger.apply("data::watcher::error [%s]", ex.getMessage());
        }
    }

    private void makeQueue(File file) {
        try {
            Set<Document> documents = Set.of(mapper.readValue(file, Document[].class));
            var added = false;
            for (var document : documents) {
                if (!document.isDone()) continue;
                if (document.getError() != null && document.getError().getCode() != Status.Code.UNAVAILABLE) continue;
                if (document.getId() != null) {
                    if (document.getDocumentImage() != null && document.getDocumentImage().getCreatedAt() != null)
                        continue;
                    queue.add(new ImageStore(document, channel, stub, status, updateLogger, updateStatus));
                    System.out.printf("ADD 01 %s\n", document.getCode());
                    added = true;
                    continue;
                }
                queue.add(new DocumentStore(document, channel, blockingStub, stub, queue, status, updateLogger, updateStatus));
                System.out.printf("ADD 02 %s\n", document.getCode());
                added = true;
            }
            if (added) {
                stack.push(DocumentData.builder().file(file).documents(documents).build());
            }
        }
        catch (IOException ex) {
            updateLogger.apply("make::queue::error [%s]", ex.getMessage());
        }
    }

    private boolean exists(File file) {
        for (var data : stack) {
            if (data.getFile().getAbsolutePath().equals(file.getAbsolutePath())) return true;
        }
        return false;
    }
}
