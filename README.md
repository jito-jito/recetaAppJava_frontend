# API Backend de Recetas 🍽️

Proyecto transformado de aplicación web tradicional a API REST para gestión de recetas con autenticación JWT.

## 🏗️ Arquitectura

Este proyecto ha sido convertido de una aplicación web monolítica con Thymeleaf a un **backend API REST puro** siguiendo la metodología del [repositorio de referencia](https://github.com/chri-alvarez/isy2202-backend-2026-201).

### Tecnologías Utilizadas

- **Java 17** con **Spring Boot 3.1.12**
- **Spring Security** con autenticación JWT
- **Spring Data JPA** con base de datos H2 (en memoria)
- **Maven** como gestor de dependencias
- **JJWT** para manejo de tokens JWT

## 🚀 Configuración y Ejecución

### Requisitos Previos

- Java 17
- Maven 3.6+

### Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

La aplicación se ejecutará en `http://localhost:8080`

### Base de Datos de Desarrollo

- **Base de datos:** H2 (en memoria)
- **Console H2:** `http://localhost:8080/h2-console`
  - **URL JDBC:** `jdbc:h2:mem:recetadb`
  - **Usuario:** `sa`
  - **Contraseña:** (vacía)

## 📡 API Endpoints

### 🏠 Información General

```http
GET / 
```
Información general del API

```http
GET /health
```
Check de salud del servicio

### 🔐 Autenticación

#### Login
```http
POST /login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Respuesta:**
```
Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwic3ViIjoidXNlciIsImlhdCI6MTcwOTg3NjU0MywiZXhwIjoxNzEwNzQwNTQzfQ.4X1lAX8gfxJoGQpVrEd5aePcRHgE_3yT7F6nRfB8lY0
```

#### Registro
```http
POST /api/usuarios/register
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "email": "usuario@email.com",
  "password": "mi_password"
}
```

### 🍽️ Gestión de Recetas

> **Nota:** Los endpoints de modificación requieren autenticación (token JWT en header `Authorization: Bearer <token>`)

#### Obtener todas las recetas
```http
GET /recipes
```

#### Obtener receta por ID
```http
GET /recipes/{id}
```

#### Buscar recetas
```http
GET /recipes/search?titulo=pasta&tipoCocina=italiana&paisOrigen=italia
```

Parámetros de búsqueda disponibles:
- `titulo`: Búsqueda por título (contiene, case-insensitive)
- `tipoCocina`: Tipo de cocina (contiene, case-insensitive)
- `paisOrigen`: País de origen (contiene, case-insensitive)
- `tiempoMaximo`: Tiempo máximo de cocción en minutos
- `popularidadMinima`: Popularidad mínima (0.0-5.0)

#### Recetas por popularidad
```http
GET /recipes/populares
```

#### Recetas recientes
```http
GET /recipes/recientes
```

#### Crear nueva receta
```http
POST /recipes
Authorization: Bearer <token>
Content-Type: application/json

{
  "titulo": "Nueva Receta",
  "tipoCocina": "Italiana",
  "paisOrigen": "Italia",
  "dificultad": "MEDIA",
  "tiempoCoccion": 30,
  "instrucciones": "Paso a paso...",
  "popularidad": 4.5,
  "fechaPublicacion": "2024-03-27",
  "imagenes": ["url1.jpg", "url2.jpg"],
  "ingredientes": [
    {
      "nombre": "Pasta",
      "cantidad": 400,
      "unidadMedida": "g"
    }
  ]
}
```

#### Actualizar receta
```http
PUT /recipes/{id}
Authorization: Bearer <token>
Content-Type: application/json
```

#### Eliminar receta
```http
DELETE /recipes/{id}
Authorization: Bearer <token>
```

### 👤 Gestión de Usuarios

#### Obtener perfil
```http
GET /api/usuarios/profile?username=admin
```

## 📊 Modelo de Datos

### Receta
```json
{
  "idReceta": 1,
  "titulo": "Tacos al Pastor",
  "tipoCocina": "Mexicana",
  "paisOrigen": "México",
  "dificultad": "MEDIA",
  "tiempoCoccion": 45,
  "instrucciones": "Instrucciones paso a paso...",
  "popularidad": 4.5,
  "fechaPublicacion": "2024-03-15",
  "imagenes": ["url1.jpg", "url2.jpg"],
  "ingredientes": [
    {
      "nombre": "Carne de cerdo",
      "cantidad": 1.0,
      "unidadMedida": "kg"
    }
  ]
}
```

### Niveles de Dificultad
- `BAJA`
- `MEDIA` 
- `ALTA`

## 🔒 Seguridad

- **JWT Token:** Expira en 10 días
- **Algoritmo:** HS256
- **Endpoints públicos:** `GET /recipes/**`, `POST /login`, `POST /api/usuarios/register`
- **Endpoints protegidos:** `POST`, `PUT`, `DELETE` en `/recipes/**`

## 🌐 CORS

CORS está habilitado en los controladores REST para permitir peticiones desde frontends externos.

## 💾 Datos de Prueba

La aplicación se inicializa con datos de prueba:

**Usuarios:**
- `admin / password`
- `user / password`  
- `chef / password`

**Recetas:**
- Tacos al Pastor (Mexicana)
- Paella Valenciana (Española)
- Pasta Carbonara (Italiana)
- Sushi California Roll (Japonesa)
- Ceviche Peruano (Peruana)

## 🔧 Configuración de Desarrollo

### Variables de Entorno (application.properties)

```properties
# Base de datos H2
spring.datasource.url=jdbc:h2:mem:recetadb
spring.h2.console.enabled=true

# Puerto del servidor
server.port=8080

# Logging para desarrollo
logging.level.com.duoc.seguridadcalidad=DEBUG
```

## 📝 Notas de Migración

### Cambios Principales Realizados:

1. ✅ **Eliminación de Thymeleaf** y dependencias de frontend
2. ✅ **Implementación de JWT** para autenticación stateless  
3. ✅ **Conversión de Controllers a RestControllers**
4. ✅ **Configuración de CORS** para permitir frontends externos
5. ✅ **Implementación completa de repositorios JPA**
6. ✅ **Mejora de modelos** con anotaciones JPA apropiadas
7. ✅ **Endpoint de salud** y información del API
8. ✅ **Datos de prueba** mediante data.sql

### Para el Frontend Separado:

El frontend deberá:
1. Implementar autenticación JWT (almacenar token tras login)
2. Enviar token en header `Authorization: Bearer <token>` para operaciones protegidas
3. Consumir los endpoints REST documentados arriba
4. Manejar estados de error HTTP apropiados

## 🚀 Próximos Pasos

1. Crear proyecto frontend separado que consuma esta API
2. Implementar hashing de passwords con BCrypt (comentado en el código)
3. Configurar base de datos persistente para producción
4. Añadir paginación a los endpoints de listado
5. Implementar cache con Redis para mejorar rendimiento

---

**Desarrollado para:** Curso de Seguridad y Calidad en el Desarrollo - DuocUC  
**Metodología:** Basada en https://github.com/chri-alvarez/isy2202-backend-2026-201