package br.dev.optimus.hermes.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentImage {
    private Long id;
    @JsonProperty("document_id")
    private UUID documentId;
    private String filename;
    private Integer pages;
    @JsonProperty("created_at")
    private Long createdAt;
    @JsonProperty("updated_at")
    private Long updatedAt;
    private ErrorReply error;
}
