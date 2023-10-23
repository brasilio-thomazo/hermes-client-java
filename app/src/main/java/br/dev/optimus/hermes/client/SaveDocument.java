package br.dev.optimus.hermes.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RequiredArgsConstructor
public class SaveDocument implements Runnable {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DocumentData data;
    private final UpdateLogger logger;

    @Override
    public void run() {
        try {
            var newFile = new File("%s.new".formatted(data.getFile().getAbsolutePath()));
            logger.apply("saving file %s", newFile.getName());
            FileWriter writer = new FileWriter(newFile);
            mapper.writeValue(writer, data.getDocuments());
            writer.close();
            if (!data.getFile().delete()) {
                logger.apply("delete::%s error", data.getFile().getName());
                return;
            }
            if (!newFile.renameTo(data.getFile())) {
                logger.apply("rename::file %s to %s error", newFile.getName(), data.getFile().getName());
            }
            logger.apply("file %s saved", data.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.apply("save::file error", ex.getMessage());
        }
    }
}
