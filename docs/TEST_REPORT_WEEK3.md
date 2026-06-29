# TEST REPORT: WEEK 3 (Subproducts & Cost Engine)

## 1. Recipe Scaling & Mathematical Precision
**Test Executed:** `RecipeScalingTest.scalesComplexFractionsWithAbsolutePrecision`
- **Methodology**: Forced the system to scale a base recipe of 1200g into a batch of 1750g. This resulted in a non-terminating fractional multiplier (`1.458333...`).
- **Results**: **PASSED**. 
- **Analysis**: The `BigDecimal` engine with `Scale 6` and `RoundingMode.HALF_UP` perfectly constrained the fraction. The deduction from the database was precisely `21.875000g` of salt. No "phantom grams" were lost to floating-point truncation.

## 2. Atomic Rollback Verification
**Test Executed:** `BatchRollbackIntegrationTest` (and simulated via `BatchPreparationTest.rollsBackAtomicallyIfStockIsInsufficient`)
- **Methodology**: Attempted to prepare a batch where the first 5 ingredients had sufficient stock, but the 6th ingredient lacked 2 grams.
- **Results**: **PASSED**.
- **Analysis**: The `@Transactional(isolation = Isolation.SERIALIZABLE)` wrapper caught the exception thrown by the `PrimaryProductStockModifier`. Direct database assertions confirmed that the first 5 ingredients remained completely untouched in the `primary_products` table. Zero side effects.

## 3. Financial Cost Verification
**Test Executed:** `CostVerificationTest.verifyScenariosFromSpreadsheet`
- **Methodology**: Pitted the system's `SubproductCostCalculator` against 5 hand-calculated scenarios documented in `/docs/cost-verification.csv`.
- **Scenario Highlight**: Fluctuating inflation prices.
- **Results**: **PASSED**.
- **Analysis**: The `WeightedAverageCostingStrategy` successfully derived a `2.333333` per/gram average from historical purchases, applied it to the recipe proportion, and outputted a total batch cost of `1616.6665`, identical to the CSV.

## 4. End-to-End Visual (Playwright)
**Script Executed:** `frontend/e2e/recipe-builder.spec.ts`
- **Methodology**: Automated Chromium browser to create a new `BATCH` recipe, verified the 400ms debounced Live Cost panel displayed the calculated cost, navigated to the Batch Dashboard, and verified the Red Warning Modal correctly multiplied the required ingredients for a non-standard 1500g batch preparation.
- **Results**: **PASSED**.
- **Analysis**: The UI strictly prevents saving zero-ingredient recipes (via Zod) and forces mandatory physical deduction confirmations before calling the API.

---
**CONCLUSION**: Week 3 is complete. The system can now securely define, scale, and financially cost complex recursive recipes without rounding anomalies or transaction hazards.
