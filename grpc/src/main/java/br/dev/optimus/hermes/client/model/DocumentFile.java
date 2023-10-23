package br.dev.optimus.hermes.client.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentFile {
    private String path;
    private String file;
    private Integer page;
    private String error;
}
