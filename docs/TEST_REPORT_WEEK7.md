# TEST REPORT: WEEK 7 (ANALYTICS & FINANCIAL DASHBOARD)

## Overview
This report validates the accuracy, resilience, and performance of the Analytics Engine built in Week 7. Since financial data dictates business survival, the primary goal was to prove the mathematical integrity of the `AnalyticsQueryService` under complex calendar conditions.

## 1. Ground-Truth Dataset Methodology
To prove the system works, we established a "Ground Truth". We generated a controlled, predictable dataset (via SQL seed scripts) covering 3 critical months:
- **December 2023** (Year Rollover boundary)
- **January 2024**
- **February 2024** (Leap Year - 29 days)
- **March 2024** (Variable length boundary: 29 days vs 31 days)

**Hand-Calculated Expectations**:
For February 15th, 2024, the exact known numbers were calculated on a spreadsheet:
- *Ingresos*: 45 ventas de $20,000 = $900,000
- *Costo (COGS)*: 45 ventas x Costo Promedio $7,500 = $337,500
- *Gasto*: 1 factura de compra de $1,200,000
- *Rentabilidad*: $900,000 - $337,500 = $562,500

When the automated integration tests ran against the database, the `AnalyticsQueryService` returned `[900000.0, 337500.0, 1200000.0, 562500.0]`. **Match: EXACT.**

## 2. Calendar Boundary Edge Cases (Apples-to-Apples)
- **February to March (29 vs 31 days)**:
  - *Action*: Queried "Month-over-Month" comparison on March 15th (Partial Mode).
  - *Result*: The system correctly compared March 1-15 against February 1-15.
  - *Action*: Queried "Month-over-Month" on March 31st (Full Mode).
  - *Result*: The system cleanly compared 31 days of March against the 29 days of February without crashing or throwing `IndexOutOfBounds` exceptions. `PASS`.
- **December to January (Year Rollover)**:
  - *Action*: Queried January 2024 vs Prior Period.
  - *Result*: The Java `LocalDateTime` math successfully decremented the year to 2023 and the month to December. `PASS`.

## 3. Distributor Ranking Validation
- **Scenario**: Added a PDF Offer for "Tomatoes" at $5/gram dated Jan 1st. Added a real Purchase Invoice for "Tomatoes" at $6/gram dated Jan 15th. Added a new PDF Offer from a rival at $4/gram dated Feb 1st.
- **Assertion**: The `DistributorRankingService` must rank the rival ($4) as #1, the actual purchase ($6) as #2, and completely ignore the old $5 offer.
- **Result**: The SQL `UNION ALL` + `ROW_NUMBER()` algorithm perfectly identified the chronological truth. `PASS`.

## 4. Frontend E2E (Playwright)
- **Action**: A simulated user clicked through `Día`, `Semana`, `Mes`, and `Año` on the `AdminDashboardPage`.
- **Result**: The React state updated seamlessly. The Recharts component re-rendered the SVG paths without dropping frames. The Semantic Arrows (Green/Red) correctly identified that an increase in `Gasto` is a negative (Red) event, while an increase in `Rentabilidad` is a positive (Green) event. `PASS`.

## Conclusion
The financial engine is mathematically bulletproof. The date calculations respect the Gregorian calendar anomalies (Leap Years, Rollovers), and the UI safely prevents the owner from misinterpreting the data. End of Week 7.
