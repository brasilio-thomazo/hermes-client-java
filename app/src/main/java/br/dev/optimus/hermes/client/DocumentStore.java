package br.dev.optimus.hermes.client;

import br.dev.optimus.hermes.client.model.Document;
import br.dev.optimus.hermes.client.model.ErrorReply;
import br.dev.optimus.hermes.grpc.DocumentRequest;
import br.dev.optimus.hermes.grpc.HermesGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;

import java.util.Queue;
import java.util.UUID;

@RequiredArgsConstructor
public class DocumentStore implements Runnable {
    private final Document document;
    private final Channel channel;
    private final HermesGrpc.HermesBlockingStub blockingStub;
    private final HermesGrpc.HermesStub stub;
    private final Queue<Runnable> queue;
    private final RunnerStatus status;
    private final UpdateLogger updateLogger;
    private final UpdateStatus updateStatus;

    @Override
    public void run() {
        var started = "started document::store::%s";
        var finished = "finished document::store::%s [%s]";
        updateLogger.apply(started, document.getCode());
        var builder = DocumentRequest.newBuilder()
                .setDocumentTypeId(document.getDocumentTypeId())
                .setDepartmentId(document.getDepartmentId())
                .setName(document.getName())
                .setIdentity(document.getIdentity())
                .setCode(document.getCode())
                .setDateDocument(document.getDateDocument());

        if (document.getComment() != null)
            builder.setComment(document.getComment());

        if (document.getStorage() != null)
            builder.setStorage(document.getStorage());

        try {
            var client = HermesGrpc.newBlockingStub(channel);
            var reply = client.documentStore(builder.build());
            updateLogger.apply(finished, reply.getCode(), reply.getId());
            var id = UUID.fromString(reply.getId());
            document.setId(id);
            document.setCreatedAt(reply.getCreatedAt());
            document.setUpdatedAt(reply.getUpdatedAt());
            queue.add(new ImageStore(document, channel, stub, status, updateLogger, updateStatus));
        } catch (StatusRuntimeException ex) {
            var error = ErrorReply.builder()
                    .code(ex.getStatus().getCode())
                    .message(ex.getStatus().getDescription())
                    .build();
            document.setError(error);
        }
        status.plusRunner();
        updateStatus.apply(status);
    }
}
