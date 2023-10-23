package br.dev.optimus.hermes.client;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RunnerStatus {
    private int total;
    private int runner;

    public void plusTotal() {
        total++;
    }

    public void plusRunner() {
        runner++;
    }
}
