package br.dev.optimus.hermes.client;

@FunctionalInterface
public interface UpdateLogger {
    void apply(String format, Object...args);
}
