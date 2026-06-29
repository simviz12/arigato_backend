package com.restaurant.application.port.out;

import java.util.List;
import java.util.Map;

public interface PdfReportGenerator {
    /**
     * Generates a PDF byte array for the Best Distributor Ranking report.
     * @param rankingData The list of ranked distributors per product (must include category)
     * @param savingsData Total savings estimates
     * @return PDF binary data
     */
    byte[] generateBestDistributorsReport(List<Map<String, Object>> rankingData, Map<String, Object> savingsData);
}
