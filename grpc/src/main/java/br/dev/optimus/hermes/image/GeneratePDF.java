package br.dev.optimus.hermes.image;

import br.dev.optimus.hermes.client.model.Document;
import br.dev.optimus.hermes.client.model.DocumentFile;
import br.dev.optimus.hermes.client.model.DocumentImage;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

@RequiredArgsConstructor
public class GeneratePDF {

    public void generate(Document document) throws IOException {
        var pdf = new PDDocument();
        var pages = 0;
        var list = new ArrayList<>(document.getFiles());
        list.sort(Comparator.comparingInt(DocumentFile::getPage));
        for (var img : list) {
            var filename = String.format("%s/%s", img.getPath(), img.getFile());
            var file = new File(filename);
            if (!file.exists() || !file.isFile()) continue;
            addPage(pdf, filename);
            pages++;
        }
        var temp = "%s/%s.pdf".formatted(System.getProperty("java.io.tmpdir", "/tmp"), document.getId().toString());
        pdf.save(temp);
        pdf.close();
        var image = DocumentImage.builder()
                .filename(temp)
                .pages(pages)
                .documentId(document.getId())
                .build();
        document.setDocumentImage(image);
    }

    private void addPage(PDDocument pdf, String filename) throws IOException {
        var page = new PDPage();
        var image = PDImageXObject.createFromFile(filename, pdf);
        var box = page.getMediaBox();
        var img = ImageIO.read(new File(filename));

        var pageWidth = box.getWidth();
        var pageHeight = box.getHeight();

        var imgWidth = img.getWidth();
        var imgHeight = img.getHeight();

        float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
        float width, height;
        if (imgWidth > pageWidth || imgHeight > pageHeight) {
            width = imgWidth * scale;
            height = imgHeight * scale;
        }
        else {
            width = imgWidth;
            height = imgHeight;
        }

        var x = (pageWidth - width) / 2;
        var y = (pageHeight - height) / 2;

        pdf.addPage(page);
        var stream = new PDPageContentStream(pdf, page);
        stream.drawImage(image, x, y, width, height);
        stream.close();
    }
}
