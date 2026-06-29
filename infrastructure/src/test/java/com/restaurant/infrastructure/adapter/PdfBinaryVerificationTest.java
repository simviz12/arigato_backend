package com.restaurant.infrastructure.adapter;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class PdfBinaryVerificationTest {

    @Test
    public void testGeneratedPdfHasValidMagicBytes() {
        // Given
        OpenPdfGeneratorAdapter adapter = new OpenPdfGeneratorAdapter();
        List<Map<String, Object>> dummyData = List.of(
            Map.of("productName", "Carne", "distributorName", "Carnes Juan", "costPerGram", 25.5)
        );

        // When
        byte[] pdfBytes = adapter.generateBestDistributorsReport(dummyData);

        // Then
        assertNotNull(pdfBytes, "PDF byte array should not be null");
        assertTrue(pdfBytes.length > 500, "PDF byte array should be reasonably large");

        // Verify PDF Magic Bytes (%PDF-)
        // % is 37, P is 80, D is 68, F is 70, - is 45
        assertEquals(37, pdfBytes[0], "Byte 0 should be %");
        assertEquals(80, pdfBytes[1], "Byte 1 should be P");
        assertEquals(68, pdfBytes[2], "Byte 2 should be D");
        assertEquals(70, pdfBytes[3], "Byte 3 should be F");
        assertEquals(45, pdfBytes[4], "Byte 4 should be -");
    }
}
