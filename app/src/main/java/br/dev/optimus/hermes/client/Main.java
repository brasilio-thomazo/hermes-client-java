package br.dev.optimus.hermes.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javafx.application.Application;

public class Main {
    public static final Properties properties = new Properties();
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("starting app");
        try {
            properties.load(new FileInputStream("config.properties"));
            Application.launch(App.class, args);
        } catch (IOException ex) {
            logger.severe(String.format("load file %s error\n%s", new File("config.properties").getAbsolutePath(), ex.getMessage()));
        }
    }
}
