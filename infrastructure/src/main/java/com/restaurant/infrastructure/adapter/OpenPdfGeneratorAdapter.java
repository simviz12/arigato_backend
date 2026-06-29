package com.restaurant.infrastructure.adapter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurant.application.port.out.PdfReportGenerator;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class OpenPdfGeneratorAdapter implements PdfReportGenerator {

    @Override
    public byte[] generateBestDistributorsReport(List<Map<String, Object>> rankingData, Map<String, Object> savingsData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Cover / Header
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Arigato Restaurant", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph subtitle = new Paragraph("Guía Óptima de Compras por Categoría", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            Paragraph datePar = new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), subtitleFont);
            datePar.setAlignment(Element.ALIGN_CENTER);
            datePar.setSpacingAfter(20);
            document.add(datePar);

            // Group data by Category
            Map<String, List<Map<String, Object>>> groupedByCategory = rankingData.stream()
                .collect(java.util.stream.Collectors.groupingBy(r -> (String) r.get("category")));

            Font categoryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font errorFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new java.awt.Color(200, 0, 0));

            for (Map.Entry<String, List<Map<String, Object>>> entry : groupedByCategory.entrySet()) {
                Paragraph catTitle = new Paragraph("Categoría: " + entry.getKey(), categoryFont);
                catTitle.setSpacingBefore(15);
                catTitle.setSpacingAfter(10);
                document.add(catTitle);

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{2.5f, 2.5f, 1.5f, 2f});

                String[] headers = {"Ingrediente", "Proveedor", "Costo", "Último Dato"};
                for (String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                    cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
                    cell.setPadding(5);
                    table.addCell(cell);
                }

                for (Map<String, Object> row : entry.getValue()) {
                    boolean isOrphan = "NONE".equals(row.get("distributorId"));
                    
                    table.addCell(new Phrase((String) row.get("productName"), bodyFont));
                    
                    if (isOrphan) {
                        PdfPCell orphanCell = new PdfPCell(new Phrase("Sin proveedor registrado", errorFont));
                        orphanCell.setBackgroundColor(new java.awt.Color(255, 230, 230));
                        table.addCell(orphanCell);
                        table.addCell(new Phrase("N/A", bodyFont));
                        table.addCell(new Phrase("Revisar Catálogos", errorFont));
                    } else {
                        table.addCell(new Phrase((String) row.get("distributorName"), bodyFont));
                        table.addCell(new Phrase("$" + row.get("costPerGram") + "/g", bodyFont));
                        
                        String updatedStr = (String) row.get("lastUpdated");
                        if (updatedStr != null && updatedStr.length() > 10) {
                            updatedStr = updatedStr.substring(0, 10); // just YYYY-MM-DD
                        }
                        table.addCell(new Phrase(updatedStr, bodyFont));
                    }
                }
                document.add(table);
            }

            // Summary Page
            document.newPage();
            Paragraph summaryTitle = new Paragraph("Resumen de Ahorro Potencial (Últimos 30 días)", titleFont);
            summaryTitle.setAlignment(Element.ALIGN_CENTER);
            summaryTitle.setSpacingAfter(20);
            document.add(summaryTitle);

            Font summaryBody = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Si hubieras realizado tus compras de los últimos 30 días usando estrictamente los proveedores recomendados en esta lista, tu balance sería el siguiente:", summaryBody));
            
            document.add(new Paragraph("\n• Gasto Real Ejecutado: $" + String.format("%,.2f", savingsData.get("actualSpent")), summaryBody));
            document.add(new Paragraph("• Costo Óptimo Calculado: $" + String.format("%,.2f", savingsData.get("optimizedCost")), summaryBody));
            
            Font savingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new java.awt.Color(0, 150, 0));
            Paragraph savingLine = new Paragraph("\nDinero dejado en la mesa (Ahorro Potencial): $" + String.format("%,.2f", savingsData.get("potentialSavings")), savingFont);
            savingLine.setAlignment(Element.ALIGN_CENTER);
            document.add(savingLine);

            document.close();
            
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF via OpenPDF", e);
        }
    }
}
