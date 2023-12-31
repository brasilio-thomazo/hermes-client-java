package br.dev.optimus.hermes.client;

import java.util.Set;
import java.util.logging.Logger;

import br.dev.optimus.hermes.client.model.Department;
import br.dev.optimus.hermes.grpc.HermesGrpc;
import br.dev.optimus.hermes.grpc.ListRequest;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientGrpc {
    private final String host;
    private final int port;
    private HermesGrpc.HermesBlockingStub client;
    private final Logger logger = Logger.getLogger(ClientGrpc.class.getName());

    public ClientGrpc createClient() {
        var channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        client = HermesGrpc.newBlockingStub(channel);
        return this;
    }

    public Set<Department> departmentList() {
        var reply = client.departmentList(ListRequest.newBuilder().build());
        logger.info(reply.toString());
        return reply.getListList()
                .stream()
                .map(department -> Department.builder()
                        .id(department.getId())
                        .name(department.getName())
                        .createdAt(department.getCreatedAt())
                        .updatedAt(department.getUpdatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toSet());
    }
}