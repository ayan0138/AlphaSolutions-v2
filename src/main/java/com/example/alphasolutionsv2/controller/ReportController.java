package com.example.alphasolutionsv2.controller;

import com.example.alphasolutionsv2.model.ProjectReport;
import com.example.alphasolutionsv2.model.User;
import com.example.alphasolutionsv2.service.PdfGenerationService;
import com.example.alphasolutionsv2.service.ReportService;
import com.example.alphasolutionsv2.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final PdfGenerationService pdfGenerationService;

    public ReportController(ReportService reportService,
                            UserService userService,
                            PdfGenerationService pdfGenerationService) {
        this.reportService = reportService;
        this.userService = userService;
        this.pdfGenerationService = pdfGenerationService;
    }

    /**
     * Generate and display project report
     */
    @GetMapping("/project/{projectId}")
    public String generateProjectReport(@PathVariable Long projectId,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Hent den indloggede bruger
            User loggedInUser = userService.getUserByUsername(userDetails.getUsername())
                    .orElse(null);

            if (loggedInUser == null) {
                return "redirect:/login";
            }

            // Generer rapporten
            ProjectReport report = reportService.generateProjectReport(projectId, loggedInUser);

            // Tilføj data til modellen for visning
            model.addAttribute("report", report);
            model.addAttribute("loggedInUser", loggedInUser);

            return "projects/project-report";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/projects/" + projectId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Der opstod en fejl ved generering af rapporten: " + e.getMessage());
            return "redirect:/projects/" + projectId;
        }
    }

    /**
     * Generate and download project report as PDF
     */
    @GetMapping("/project/{projectId}/pdf")
    public void generateProjectReportPdf(@PathVariable Long projectId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         HttpServletResponse response,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Hent den ind loggede bruger
            User loggedInUser = userService.getUserByUsername(userDetails.getUsername())
                    .orElse(null);

            if (loggedInUser == null) {
                response.sendRedirect("/login");
                return;
            }

            // Generer rapporten
            ProjectReport report = reportService.generateProjectReport(projectId, loggedInUser);

            // Opret Thymeleaf kontekst
            Context context = new Context();
            context.setVariable("report", report);
            context.setVariable("loggedInUser", loggedInUser);

            // Generer PDF ved hjælp af PDF skabelonen
            byte[] pdfBytes = pdfGenerationService.generatePdfFromTemplate("project-report-pdf", context);

            // Angiv respons-headere for PDF download
            String filename = "projekt-rapport-" +
                    report.getProject().getName().replaceAll("[^a-zA-Z0-9\\-_]", "-") +
                    ".pdf";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Content-Length", String.valueOf(pdfBytes.length));

            // Skirv pdf til respons
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (IllegalArgumentException e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Der opstod en fejl ved generering af PDF: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}