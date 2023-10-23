package br.dev.optimus.hermes.client.model;

import io.grpc.Status;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorReply {
    private String message;
    private Status.Code code;
}
