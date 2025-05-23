package com.example.alphasolutionsv2.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;

class PdfGenerationServiceTest {

    private TemplateEngine templateEngine;
    private PdfGenerationService pdfService;

    @BeforeEach
    void setUp() {
        templateEngine = Mockito.mock(TemplateEngine.class);
        pdfService = new PdfGenerationService(templateEngine);
    }

    @Test
    void testGeneratePdfFromTemplate_shouldReturnNonEmptyPdf() throws Exception {
        // Arrange
        String fakeHtml = "<html><head></head><body><h1>Test PDF</h1></body></html>";
        Mockito.when(templateEngine.process(Mockito.anyString(), Mockito.any(Context.class)))
                .thenReturn(fakeHtml);

        Context context = new Context();

        // Act
        byte[] pdfBytes = pdfService.generatePdfFromTemplate("dummy-template", context);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100, "PDF should not be empty");
    }
}