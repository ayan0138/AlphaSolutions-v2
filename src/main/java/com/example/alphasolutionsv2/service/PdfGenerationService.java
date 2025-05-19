package com.example.alphasolutionsv2.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generatePdfFromTemplate(String templateName, Context context) throws Exception {
        // Process the Thymeleaf template to HTML
        String htmlContent = templateEngine.process(templateName, context);

        // Read CSS file from classpath
        String cssContent = readCssFromClasspath("/static/css/style.css");

        // Clean CSS to remove properties that cause warnings
        String cleanedCss = cleanCssForPdf(cssContent);

        // Parse HTML with JSoup and inline the cleaned CSS
        Document document = Jsoup.parse(htmlContent);

        // Remove existing CSS link and add inline styles
        document.select("link[rel=stylesheet]").remove();
        document.head().appendElement("style").text(cleanedCss);

        // Convert to XHTML for OpenHTMLToPDF
        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document xhtmlDocument = w3cDom.fromJsoup(document);

        // Generate PDF
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

        // Remove box-shadow properties
        cssContent = cssContent.replaceAll("box-shadow\\s*:[^;]+;", "/* box-shadow removed for PDF */");

        // Remove webkit and color-adjust properties
        cssContent = cssContent.replaceAll("-webkit-print-color-adjust\\s*:[^;]+;", "");
        cssContent = cssContent.replaceAll("color-adjust\\s*:[^;]+;", "");
        cssContent = cssContent.replaceAll("print-color-adjust\\s*:[^;]+;", "");

        // Remove opacity (replace with comment to maintain the effect visually)
        cssContent = cssContent.replaceAll("opacity\\s*:[^;]+;", "/* opacity removed for PDF */");

        // Replace linear-gradient with solid color
        cssContent = cssContent.replaceAll(
                "background:\\s*linear-gradient\\([^)]+\\);",
                "background: #007bff;"
        );

        // Replace CSS Grid with PDF-friendly alternatives
        cssContent = replaceCssGrid(cssContent);

        // Remove flexbox and replace with block
        cssContent = cssContent.replaceAll("display\\s*:\\s*flex;", "display: block;");

        // Remove break-inside properties
        cssContent = cssContent.replaceAll("break-inside\\s*:[^;]+;", "");

        // Remove page-break properties that might cause issues
        cssContent = cssContent.replaceAll("page-break-after\\s*:[^;]+;", "");

        // Remove tr:hover as it's not needed in PDF
        cssContent = cssContent.replaceAll("\\.pdf-table\\s+tr:hover\\s*\\{[^}]+\\}", "");

        return cssContent;
    }

    /**
     * Replace CSS Grid with PDF-friendly layout
     */
    private String replaceCssGrid(String cssContent) {
        // Replace grid display with block
        cssContent = cssContent.replaceAll("display\\s*:\\s*grid;", "display: block;");

        // Remove grid-template-columns
        cssContent = cssContent.replaceAll("grid-template-columns\\s*:[^;]+;", "");

        // Remove gap property
        cssContent = cssContent.replaceAll("gap\\s*:[^;]+;", "");

        // Add PDF-friendly alternative for summary grid
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

        // Add the PDF-friendly styles at the end
        cssContent += "\n/* PDF-friendly grid alternatives */\n" + pdfGridStyles;

        return cssContent;
    }

    /**
     * Read CSS file from classpath
     */
    private String readCssFromClasspath(String cssPath) throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(cssPath);
            InputStream inputStream = resource.getInputStream();
            byte[] cssBytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(cssBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Warning: CSS file not found at " + cssPath);
            return "";
        }
    }
}
