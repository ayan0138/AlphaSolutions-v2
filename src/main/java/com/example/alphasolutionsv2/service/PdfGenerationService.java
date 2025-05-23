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

/**
 * PDF Generation Service baseret på OpenHTMLToPDF's officielle integration guide
 * Se: <a href="https://github.com/danfickle/openhtmltopdf/wiki/Integration-Guide">...</a>

 * Implementeringen følger det anbefalede mønster fra OpenHTMLToPDF dokumentationen
 * med tilføjede optimiseringer til CSS-håndtering for bedre PDF-rendering.
 */
@Service
public class PdfGenerationService {

    private final TemplateEngine templateEngine;

    public PdfGenerationService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Hovedmetode til PDF-generering baseret på OpenHTMLToPDF's standard workflow:
     * 1. Process Thymeleaf template til HTML
     * 2. Parse med JSoup
     * 3. Konverter til W3C DOM
     * 4. Generer PDF med PdfRendererBuilder
     * Tilføjet: CSS-optimisering for bedre PDF-kompatibilitet
     */
    public byte[] generatePdfFromTemplate(String templateName, Context context) throws Exception {
        // Step 1: Standard Thymeleaf processing (fra OpenHTMLToPDF guide)
        String htmlContent = templateEngine.process(templateName, context);

        // Step 2: CSS handling (inspireret af community best practices)
        String cssContent = loadCssContent();
        String optimizedCss = optimizeCssForPdf(cssContent);

        // Step 3: HTML manipulation med JSoup (OpenHTMLToPDF standard approach)
        Document document = Jsoup.parse(htmlContent);
        document.select("link[rel=stylesheet]").remove();
        document.head().appendElement("style").text(optimizedCss);

        // Step 4: W3C DOM conversion (direkte fra OpenHTMLToPDF dokumentation)
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document xhtmlDocument = w3cDom.fromJsoup(document);

        // Step 5: PDF generation (standard PdfRendererBuilder usage)
        return buildPdfFromDocument(xhtmlDocument);
    }

    /**
     * Standard CSS loading metode baseret på Spring Boot best practices
     * Inspiration: Spring.io guides om ressource-håndtering
     */
    private String loadCssContent() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource("/static/css/style.css");
            InputStream inputStream = resource.getInputStream();
            byte[] cssBytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(cssBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("CSS file ikke fundet, bruger standard styling");
            return getDefaultPdfStyles();
        }
    }

    /**
     * PDF generation metode der følger OpenHTMLToPDF's anbefalede mønster
     * Se: <a href="https://github.com/danfickle/openhtmltopdf/wiki/Integration-Guide">...</a>
     */
    private byte[] buildPdfFromDocument(org.w3c.dom.Document xhtmlDocument) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Standard PdfRendererBuilder setup fra OpenHTMLToPDF dokumentation
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withW3cDocument(xhtmlDocument, "/");
        builder.toStream(outputStream);
        builder.run();

        return outputStream.toByteArray();
    }

    /**
     * CSS optimisering metode - kombinerer OpenHTMLToPDF anbefalinger
     * med community best practices for PDF-rendering
     * Baseret på issues og solutions fra OpenHTMLToPDF GitHub repository
     */
    private String optimizeCssForPdf(String cssContent) {
        if (cssContent == null || cssContent.isEmpty()) {
            return getDefaultPdfStyles();
        }

        // Fix #1: box-shadow ikke understøttet (kendt OpenHTMLToPDF limitation)
        cssContent = removeUnsupportedBoxShadow(cssContent);

        // Fix #2: Gradient support workaround (fra community solutions)
        cssContent = replaceGradientsWithSolidColors(cssContent);

        // Fix #3: Modern layout fixes (baseret på PDF rendering best practices)
        cssContent = convertModernLayoutForPdf(cssContent);

        // Fix #4: Print-specific optimizations
        cssContent = addPrintOptimizations(cssContent);

        return cssContent;
    }

    /**
     * Fjerner box-shadow properties der ikke understøttes af OpenHTMLToPDF
     * Problem dokumenteret i: <a href="https://github.com/danfickle/openhtmltopdf/issues">...</a>
     */
    private String removeUnsupportedBoxShadow(String css) {
        return css.replaceAll("box-shadow\\s*:[^;]+;", "/* box-shadow ikke understøttet i PDF */");
    }

    /**
     * Erstatter linear-gradient med solid colors
     * Workaround for OpenHTMLToPDF's begrænsede gradient support
     */
    private String replaceGradientsWithSolidColors(String css) {
        // Erstat login gradient med solid farve
        css = css.replaceAll(
                "background:\\s*linear-gradient\\([^)]+\\);",
                "background: #2c5aa0;"
        );
        return css;
    }

    /**
     * Konverterer moderne CSS layout (Grid, Flexbox) til PDF-kompatible alternativer
     * Baseret på anbefalinger fra CSS-to-PDF conversion best practices
     */
    private String convertModernLayoutForPdf(String css) {
        // Flexbox → Block layout conversion
        css = css.replaceAll("display\\s*:\\s*flex;", "display: block;");

        // CSS Grid → Table-like layout conversion
        css = css.replaceAll("display\\s*:\\s*grid;", "display: block;");
        css = css.replaceAll("grid-template-columns\\s*:[^;]+;", "");
        css = css.replaceAll("gap\\s*:[^;]+;", "");

        // Tilføj PDF-venlige layout alternativer
        css += getPdfLayoutStyles();

        return css;
    }

    /**
     * Tilføjer print-specifikke optimiseringer
     * Baseret på W3C print stylesheet anbefalinger
     */
    private String addPrintOptimizations(String css) {
        // Fjern interaktive elementer
        css = css.replaceAll("tr:hover\\s*\\{[^}]+}", "");
        css = css.replaceAll("opacity\\s*:[^;]+;", "");

        // Fjerner webkit-specifikke properties
        css = css.replaceAll("-webkit-[^:]+:[^;]+;", "");

        return css;
    }

    /**
     * PDF-venlige layout styles som erstatning for moderne CSS features
     */
    private String getPdfLayoutStyles() {
        return """
            
            /* PDF Layout Alternatives - erstatter Grid/Flexbox */
            .pdf-summary-item {
                display: inline-block;
                width: 24%;
                vertical-align: top;
                margin-right: 1%;
                text-align: center;
                padding: 15px;
                background: white;
                border: 1px solid #e0e0e0;
                box-sizing: border-box;
            }
            
            .pdf-info-item {
                display: block;
                margin-bottom: 10px;
                width: 100%;
            }
            """;
    }

    /**
     * Fallback styles hvis hovedstyling ikke kan loades
     */
    private String getDefaultPdfStyles() {
        return """
            body { font-family: Arial, sans-serif; margin: 20px; }
            .pdf-header { background: #007bff; color: white; padding: 20px; text-align: center; }
            .pdf-table { width: 100%; border-collapse: collapse; }
            .pdf-table th, .pdf-table td { border: 1px solid #ddd; padding: 8px; }
            .pdf-table th { background-color: #f2f2f2; }
            """;
    }
}
