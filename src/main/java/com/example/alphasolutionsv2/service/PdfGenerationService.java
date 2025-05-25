package com.example.alphasolutionsv2.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PdfGenerationService {


    private final TemplateEngine templateEngine;

    public PdfGenerationService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdfFromTemplate(String templateName, Context context) throws Exception {
        // Behandl Thymeleaf skabelonen til HTML
        String htmlContent = templateEngine.process(templateName, context);

        // Læs ccs fil fra classpath
        String cssContent = readCssFromClasspath();

        // Rens CSS for at fjerne egenskaber, der foråsager advarlser
        String cleanedCss = cleanCssForPdf(cssContent);

        // Parse HTML med JSoup og inline den rensede CSS
        Document document = Jsoup.parse(htmlContent);

        // Fjern eksisterende CSS link og tilføj inline-stilarter
        document.select("link[rel=stylesheet]").remove();
        document.head().appendElement("style").text(cleanedCss);

        // Konventer til XHTML for OpenHTMLToPDF
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document xhtmlDocument = w3cDom.fromJsoup(document);

        // Generer PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withW3cDocument(xhtmlDocument, "/");
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }

    /**
     * Clean CSS by removing or replacing properties that cause warnings in OpenHTMLToPDF
     */
    private String cleanCssForPdf(String cssContent) {
        if (cssContent == null || cssContent.isEmpty()) {
            return "";
        }

        // Fjern box-shadow properties
        cssContent = cssContent.replaceAll("box-shadow\\s*:[^;]+;", "/* box-shadow removed for PDF */");

        // Fjern webkit og color-adjust properties
        cssContent = cssContent.replaceAll("-webkit-print-color-adjust\\s*:[^;]+;", "");
        cssContent = cssContent.replaceAll("color-adjust\\s*:[^;]+;", "");
        cssContent = cssContent.replaceAll("print-color-adjust\\s*:[^;]+;", "");

        // Fjern opacity (erstat med kommentar for at bevare den visuelle effekt
        cssContent = cssContent.replaceAll("opacity\\s*:[^;]+;", "/* opacity removed for PDF */");

        // Erstat linear-gradient med solid farve
        cssContent = cssContent.replaceAll(
                "background:\\s*linear-gradient\\([^)]+\\);",
                "background: #007bff;"
        );

        // Erstat CSS Grid med PDF-friendly alternativer
        cssContent = replaceCssGrid(cssContent);

        // Fjern flexbox og erstat with block
        cssContent = cssContent.replaceAll("display\\s*:\\s*flex;", "display: block;");

        // Fjern break-inside properties
        cssContent = cssContent.replaceAll("break-inside\\s*:[^;]+;", "");

        // Fjern page-break properties som potentialt kan forårsage problemer
        cssContent = cssContent.replaceAll("page-break-after\\s*:[^;]+;", "");

        // Fjern tr:hover da det ikke er nødvændigt i pdf
        cssContent = cssContent.replaceAll("\\.pdf-table\\s+tr:hover\\s*\\{[^}]+}", "");

        return cssContent;
    }

    /**
     * Replace CSS Grid with PDF-friendly layout
     */
    private String replaceCssGrid(String cssContent) {
        // Erstat grid display med block
        cssContent = cssContent.replaceAll("display\\s*:\\s*grid;", "display: block;");

        // Fjern grid-template-columns
        cssContent = cssContent.replaceAll("grid-template-columns\\s*:[^;]+;", "");

        // Fjern gap property
        cssContent = cssContent.replaceAll("gap\\s*:[^;]+;", "");

        // Tilføj PDF-venlig alternativer til summary grid
        String pdfGridStyles = """
            .pdf-summary-item {
                display: inline-block;
                width: 24%;
                vertical-align: top;
                margin-right: 1%;
                text-align: center;
                padding: 15px;
                background: white;
                border-radius: 8px;
                border: 1px solid #e0e0e0;
                box-sizing: border-box;
            }
            
            .pdf-info-item {
                display: block;
                margin-bottom: 10px;
                width: 100%;
            }
            """;

        // Tilføj den PDF-venlige styles i slutningen
        cssContent += "\n/* PDF-friendly grid alternatives */\n" + pdfGridStyles;

        return cssContent;
    }

    /**
     * Read CSS file from classpath
     */
    private String readCssFromClasspath() {
        String cssPath = "/static/css/login.css";
        try {
            ClassPathResource resource = new ClassPathResource(cssPath);
            InputStream inputStream = resource.getInputStream();
            byte[] cssBytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(cssBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Warning: CSS file not found at " + "/static/css/login.css");
            return "";
        }
    }
}