# Backend del Sistema Arigato

Este documento describe la arquitectura y las funcionalidades del backend.

## Arquitectura (Clean Architecture)
El backend está dividido en 4 capas para garantizar escalabilidad:
1. **Domain (`domain`)**: Entidades centrales (`Product`, `Sale`, `Inventory`), Value Objects (`Weight`, `Money`) y los puertos (interfaces). No depende de nada.
2. **Application (`application`)**: Casos de uso de negocio (ej. `PrepareBatchSubproductUseCase`, `ProcessSaleUseCase`). Orquesta el dominio.
3. **Infrastructure (`infrastructure`)**: Adaptadores para Spring Data JPA, repositorios en PostgreSQL, configuración JWT y Spring Security.
4. **API (`api`)**: Controladores REST para la comunicación con el frontend.

## Lógica Matemática y Pruebas
Todos los cálculos de merma, rendimientos de lote y mermas por preparación (g a kg) están altamente probados mediante Pruebas Unitarias.
Se incluyeron *Unit Tests* para validaciones críticas como los *Márgenes de Rentabilidad*, asegurando que el sistema no permita pérdidas financieras sin alertas.

## Seguridad (Refresh Tokens)
Se implementó un esquema robusto de seguridad:
- Access Token (JWT): Vida corta (15 min).
- Refresh Token: Guardado en una cookie `HttpOnly` y `Secure`. El interceptor del frontend llama a `/api/auth/refresh` silenciosamente para renovar la sesión de los usuarios (como los cajeros POS) sin interrumpir sus ventas.
