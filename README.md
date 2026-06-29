# Arigato Inventory System - Backend

Este es el backend del **Sistema de Inventario Arigato**, diseñado para gestionar compras, inventario, recetas (subproductos) y ventas (POS) de un restaurante.

## 🚀 Tecnologías Utilizadas
- **Java 21**
- **Spring Boot 3.2.5**
- **PostgreSQL 15** (Base de datos relacional)
- **Maven** (Gestión de dependencias)
- **MapStruct** (Mapeo de DTOs)
- **Lombok** (Reducción de boilerplate)
- **Docker & Docker Compose**

## 🏗️ Arquitectura: Clean Architecture

El backend está construido bajo el paradigma de **Clean Architecture** (Arquitectura Limpia), dividiendo el sistema en módulos estrictos. ¿Por qué usamos esta arquitectura y por qué es tan buena?

1. **Independencia de Frameworks:** La lógica de negocio no depende de Spring Boot ni de la base de datos.
2. **Alta Testabilidad:** Al estar la lógica de negocio aislada, probar el comportamiento del sistema es sumamente fácil.
3. **Mantenibilidad:** Si en el futuro cambiamos la base de datos de PostgreSQL a otra, solo debemos cambiar el módulo de `infrastructure`, sin tocar ni una línea de la lógica central.

### Estructura de Módulos (Multi-Module Maven)
El proyecto se divide en las siguientes capas (de más interna a más externa):

- `domain/`: **El Corazón del Sistema.** Contiene las entidades puras de Java (Ej: `PrimaryProduct`, `Sale`, `Purchase`), interfaces de repositorios y excepciones de negocio. Cero dependencias de Spring.
- `application/`: **Casos de Uso.** Implementa la lógica de orquestación (Ej: `ProcessSaleUseCase`, `RegisterPurchaseUseCase`). Utiliza las interfaces del dominio para comunicarse con el exterior.
- `infrastructure/`: **Adaptadores de Salida.** Contiene la implementación real de JPA/Hibernate, repositorios de Spring Data y migraciones de Flyway. Es la encargada de hablar con PostgreSQL.
- `api/`: **Adaptadores de Entrada.** Contiene los Controladores REST (`@RestController`), la configuración de Spring Boot (`application.yml`), manejo de errores globales y seguridad.

## 🐳 Cómo Compilar y Correr en Docker

El sistema entero (Backend, Frontend y Base de Datos) está orquestado mediante `docker-compose`. 

1. Asegúrate de tener **Docker Desktop** iniciado.
2. Ve a la carpeta raíz del proyecto (donde está el archivo `docker-compose.yml`).
3. Ejecuta el siguiente comando para construir las imágenes y levantar el sistema en segundo plano:

```bash
docker-compose up --build -d
```

### ¿Qué hace esto por detrás?
- Descarga una imagen de Maven y **compila el código fuente** de Java generando el archivo `app.jar`.
- Levanta un contenedor de **PostgreSQL** (`arigato-db-1`) en el puerto 5432.
- Levanta el contenedor de **Spring Boot** (`arigato-backend-1`) en el puerto 8080.
- Ejecuta automáticamente las migraciones SQL (Flyway) para crear las tablas.

*Si quieres ver los logs del backend para depurar:*
```bash
docker logs -f arigato-backend-1
```

## 🛠️ Ejecución Local (Sin Docker)
Si deseas ejecutar solo el backend de forma local:
1. Levanta una base de datos PostgreSQL en el puerto `5432` con usuario/contraseña `postgres`.
2. En la raíz de la carpeta `backend`, ejecuta:
```bash
mvn clean install -DskipTests
```
3. Luego, inicia el módulo API:
```bash
mvn spring-boot:run -pl api
```
