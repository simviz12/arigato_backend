# Glosario Financiero y AnalÃ­tico

Este documento define de forma estricta los conceptos financieros utilizados en todo el sistema Arigato (especialmente en los servicios de analÃ­tica). **Bajo ninguna circunstancia** se deben alterar estas definiciones matemÃ¡ticas, ya que representan el modelo mental exacto solicitado por el cliente.

## Conceptos Core

### 1. Ingresos (Revenue)
* **DefiniciÃ³n**: La suma total de dinero que entrÃ³ al negocio por concepto de ventas completadas durante un periodo especÃ­fico. Incluye todos los mÃ©todos de pago (Efectivo, Nequi, etc.).
* **FÃ³rmula SQL**: `SUM(total_amount_cents)` en la tabla `sales` filtrado por `sale_date`.
* **Regla de Negocio**: Ya tiene descontados los "Descuentos" aplicados en el POS (porque el POS envÃ­a el Total final).

### 2. Costo (Cost of Goods Sold - COGS)
* **DefiniciÃ³n**: El costo de los ingredientes y productos que *fÃ­sicamente salieron del inventario y fueron vendidos al cliente* en ese mismo periodo.
* **FÃ³rmula SQL**: `SUM(unit_cost_cents_at_sale * quantity_sold)` en la tabla `sale_lines` cruzada con `sales`.
* **Regla de Negocio CrÃ­tica**: JamÃ¡s se recalcula usando el costo actual de los ingredientes. Se utiliza obligatoriamente el "snapshot" del costo histÃ³rico guardado en el momento exacto en que ocurriÃ³ la venta (`unit_cost_cents_at_sale`).

### 3. Gasto (Cash Spend / Purchasing Spend)
* **DefiniciÃ³n**: Todo el dinero fÃ­sico que el restaurante sacÃ³ del bolsillo para comprar insumos a distribuidores, *sin importar si esos insumos ya se vendieron o siguen guardados en la nevera*.
* **FÃ³rmula SQL**: `SUM(total_price_cents)` en la tabla `purchases` filtrado por `purchase_date`.
* **Regla de Negocio**: Esto mide el "golpe de flujo de caja" de abastecer el restaurante, no la eficiencia de las ventas.

### 4. Rentabilidad Bruta (Gross Profit)
* **DefiniciÃ³n**: La diferencia entre el dinero que entrÃ³ (Ingresos) y lo que costÃ³ preparar esa comida (Costo).
* **FÃ³rmula MatemÃ¡tica**: `Rentabilidad = Ingresos - Costo`.
* **Diferenciador**: No se resta el "Gasto". Si hoy compro 1 millÃ³n de pesos en carne para todo el mes (Gasto = 1M), pero solo vendo 10 hamburguesas (Ingreso = 250k, Costo = 80k), mi rentabilidad del dÃ­a es 170k, no -830k. El Gasto es flujo de caja libre, el Costo es eficiencia operativa.

---
> **Para Desarrolladores**: Cuando construyan reportes, validen que las cifras concuerden 100% con estas fÃ³rmulas. Mezclar "Costo" con "Gasto" destruirÃ¡ la contabilidad del sistema.

### 5. Ranking de Proveedores (Best Distributor Logic)
* **Definición**: La lógica matemática para decidir qué proveedor ofrece el mejor precio para un ingrediente en tiempo real.
* **Problema de Negocio**: Un proveedor puede registrar una "Oferta" oficial hace 6 meses, pero en una compra real de ayer puede haber cobrado un precio distinto.
* **Regla de Negocio (El Dato Más Reciente Gana)**: El sistema cruzará ambas tablas (distributor_offers y purchase_lines). Para un proveedor específico y un ingrediente específico, el sistema tomará exclusivamente el registro con la fecha más reciente (sea una oferta o una compra). Luego, ordenará a todos los proveedores basándose en ese precio más reciente.
* **Implicación**: Las compras reales sobreescriben ofertas viejas automáticamente, y nuevas ofertas sobreescriben compras viejas. No se requiere intervención manual del administrador.

### 6. Licencias de Terceros y PDF (OpenPDF)
* **Problema Comercial**: La generación de PDFs en Java suele hacerse con iText. Las versiones recientes de iText (5+) utilizan la licencia AGPL, la cual es **viral y restrictiva**. Si se compila iText en Arigato, legalmente obligaría a la empresa a publicar todo el código fuente del backend como Open Source.
* **Solución (OpenPDF)**: Arigato utiliza OpenPDF (un fork mantenido por la comunidad de iText 4). Su licencia es **LGPL y MPL**.
* **Impacto Legal**: La licencia LGPL/MPL permite enlazar la librería dinámicamente en aplicaciones comerciales de código cerrado (closed-source) sin obligar a abrir el código fuente del restaurante. Es 100% segura para el uso corporativo privado del cliente.
