package br.dev.optimus.hermes.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Department {
    private long id;
    private String name;
    private long createdAt;
    private long updatedAt;
}
