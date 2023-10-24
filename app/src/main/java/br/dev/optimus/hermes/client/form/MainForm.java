package br.dev.optimus.hermes.client.form;

import br.dev.optimus.hermes.client.ClientGrpc;
import br.dev.optimus.hermes.client.form.component.IMenuBar;
import br.dev.optimus.hermes.client.model.Document;
import br.dev.optimus.hermes.client.model.DocumentFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

public class MainForm extends VBox {
    private final Logger logger = Logger.getLogger(MainForm.class.getName());
    private final Stage stage;
    @Getter
    private final TextArea logs = new TextArea();
    @Getter
    private final Label status = new Label("");

    private final Button button = new Button("Start");

    {
        button.setId("btn-start");
        button.setOnAction(this::buttonAction);
    }

    private final ButtonBar buttonBar = new ButtonBar();

    {
        buttonBar.getButtons().add(button);
    }

    private final ObjectMapper mapper = new ObjectMapper();

    public MainForm(Stage stage) {
        super(5);
        this.stage = stage;
        logs.setEditable(false);
        VBox.setVgrow(logs, Priority.ALWAYS);
        IMenuBar menuBar = new IMenuBar(this::menuAction);
        getChildren().addAll(menuBar, logs, status);
    }

    private void menuAction(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        if (item.getId().equals("menu-close")) {
            stage.close();
        }
        if (item.getId().equals("menu-generate")) {
            generate();
        }
        if (item.getId().equals("menu-test")) {
            listDepartment();
        }
    }

    private void buttonAction(ActionEvent event) {
        Button item = (Button) event.getSource();
        if (item.getId().equals("btn-start")) {
            stage.close();
        }
    }

    private void listDepartment() {
        logger.info("Listando departamentos");
        var client = new ClientGrpc("192.168.59.105", 31110);
        client.createClient().departmentList().forEach(department -> {
            logger.info(department.toString());
        });
    }

    private String generateName() {
        String[] names = {
                "Anakin Skywalker",
                "Kylo Ren",
                "Obi-Wan Kenobi",
                "Luke Skywalker",
                "Leia Organa",
                "Han Solo",
                "Padmé Amidala",
                "Qui-Gon Jinn",
                "Boba Fett",
                "Lando Calrissian",
        };
        var rng = new Random();
        return names[rng.nextInt(names.length - 1)];
    }

    private String generateIdentity() {
        var size = 11;
        var data = "0123456789";
        var build = new StringBuilder(size);
        var rand = new SecureRandom();
        for (int i = 0; i < size; i++)
            build.append(data.charAt(rand.nextInt(data.length())));
        return build.toString();
    }

    private String generateCode() {
        var size = 10;
        var data = "ABCDEFGHIJKLMNOPQRSWXYZ0123456789";
        var build = new StringBuilder(size);
        var rand = new SecureRandom();
        for (int i = 0; i < size; i++)
            build.append(data.charAt(rand.nextInt(data.length())));
        return build.toString();
    }

    private void generate() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione imagens para gerar os documentos de teste");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("images", "*.jpg", "*.png", "*.tiff"));
        var files = fileChooser.showOpenMultipleDialog(stage);
        if (files == null) {
            return;
        }

        var rng = new Random();
        var end = rng.nextInt(50, 100);
        Set<Document> documents = new HashSet<>();
        for (int i = 0, x = 1; i < end; i++, x++) {
            var builder = Document.builder()
                    .departmentId(2L)
                    .documentTypeId(1L)
                    .name(generateName())
                    .identity(generateIdentity())
                    .code(generateCode())
                    .dateDocument(LocalDate.now().minusDays(rng.nextLong(30)).toString())
                    .isDone(true);
            var addFiles = new HashSet<DocumentFile>();
            for (int j = 0; j < rng.nextInt(1, 10); j++) {
                var fileIndex = rng.nextInt(files.size() - 1);
                var addFile = DocumentFile.builder()
                        .file(files.get(fileIndex).getName())
                        .path(files.get(fileIndex).getParent())
                        .page(j)
                        .build();
                addFiles.add(addFile);
            }
            builder.files(addFiles);
            documents.add(builder.build());
        }
        try {
            var data = mapper.writeValueAsString(documents);
            var writer = new FileWriter("data-test.json");
            writer.write(data);
            writer.close();
        } catch (IOException ex) {
            logger.warning(ex.getMessage());
        }
    }
}
