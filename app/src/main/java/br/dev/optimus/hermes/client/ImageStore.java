package br.dev.optimus.hermes.client;

import br.dev.optimus.hermes.client.model.Document;
import br.dev.optimus.hermes.client.model.ErrorReply;
import br.dev.optimus.hermes.grpc.DocumentImageInfo;
import br.dev.optimus.hermes.grpc.DocumentImageReply;
import br.dev.optimus.hermes.grpc.DocumentImageRequest;
import br.dev.optimus.hermes.grpc.HermesGrpc;
import br.dev.optimus.hermes.image.GeneratePDF;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

@RequiredArgsConstructor
public class ImageStore implements Runnable {
    private final Random random = new Random();
    private final Document document;
    private final Channel channel;
    private final HermesGrpc.HermesStub stub;
    private final RunnerStatus status;
    private final UpdateLogger updateLogger;
    private final UpdateStatus updateStatus;
    private final GeneratePDF generatePDF = new GeneratePDF();


    @Override
    public void run() {
        int len = 1024 * 1024;
        byte[] buffer = new byte[len];
        int read;
        try {
            var generate = "generate::pdf [%s]";
            var started = "started document::image::store::%s";
            var finished = "finished document::image::store::%s";
            var client = HermesGrpc.newStub(channel);
            updateLogger.apply(started, document.getCode());
            generatePDF.generate(document);
            updateLogger.apply(generate, document.getDocumentImage().getFilename());

            var info = DocumentImageInfo.newBuilder()
                    .setDisk("local")
                    .setDocumentId(document.getId().toString())
                    .setExtension(".pdf")
                    .setPages(document.getDocumentImage().getPages())
                    .build();

            var request = DocumentImageRequest.newBuilder()
                    .setInfo(info)
                    .build();

            var fileStream = new FileInputStream(document.getDocumentImage().getFilename());
            var stream = client.documentImageStore(new DataObserver());
            stream.onNext(request);
            while ((read = fileStream.read(buffer, 0, buffer.length)) != -1) {
                var data = DocumentImageRequest.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, read))
                        .build();
                stream.onNext(data);
            }
            fileStream.close();
            stream.onCompleted();
            updateLogger.apply(finished, document.getCode());

        } catch (IOException ex) {
            updateLogger.apply("image::store::error [%s]", ex.getMessage());
        }
        status.plusRunner();
        updateStatus.apply(status);
    }

    class DataObserver implements StreamObserver<DocumentImageReply> {

        @Override
        public void onNext(DocumentImageReply value) {
            if (value == null) return;
            document.getDocumentImage().setCreatedAt(value.getCreatedAt());
            document.getDocumentImage().setUpdatedAt(value.getUpdatedAt());
            document.getDocumentImage().setId(value.getId());
        }

        @Override
        public void onError(Throwable t) {
            var error = ErrorReply.builder();
            if (t.getClass().equals(StatusRuntimeException.class))
                error.code(((StatusRuntimeException) t).getStatus().getCode());
            else error.code(Status.Code.UNKNOWN);
            error.message(t.getMessage());
            document.getDocumentImage().setError(error.build());
        }

        @Override
        public void onCompleted() {
        }
    }
}
