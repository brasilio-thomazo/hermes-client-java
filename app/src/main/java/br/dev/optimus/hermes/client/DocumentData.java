package br.dev.optimus.hermes.client;

import br.dev.optimus.hermes.client.model.Document;
import lombok.*;

import java.io.File;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentData {
    private Set<Document> documents;
    private File file;
}
