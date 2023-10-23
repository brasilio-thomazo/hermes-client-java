package br.dev.optimus.hermes.client;

@FunctionalInterface
public interface UpdateStatus {
    void apply(RunnerStatus status);
}
