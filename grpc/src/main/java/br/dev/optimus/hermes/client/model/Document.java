package br.dev.optimus.hermes.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private UUID id;
    @JsonProperty("document_type_id")
    private Long documentTypeId;
    @JsonProperty("department_id")
    private Long departmentId;
    private String code;
    private String identity;
    private String name;
    private String comment;
    private String storage;
    @JsonProperty("date_document")
    private String dateDocument;
    @JsonProperty("created_at")
    private Long createdAt;
    @JsonProperty("updated_at")
    private Long updatedAt;
    @JsonProperty("is_done")
    private boolean isDone;
    @JsonProperty("is_update")
    private boolean isUpdate;
    private Set<DocumentFile> files;
    @JsonProperty("document_image")
    private DocumentImage documentImage;
    private ErrorReply error;
}
