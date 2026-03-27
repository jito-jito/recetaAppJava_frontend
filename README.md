# RecetaApp - Frontend 🍳

## Descripción
RecetaApp es una aplicación frontend desarrollada con Spring Boot y Thymeleaf que presenta una colección de recetas saludables. Esta aplicación sirve contenido estático y plantillas HTML sin conexión a base de datos.

## Características
- 🎨 **Interfaz moderna**: Diseño responsivo con CSS personalizado
- 🍽️ **Páginas de recetas**: Colección de recetas con ingredientes e instrucciones
- 🔐 **Página de login**: Frontend para conectar con backend de autenticación
- 📱 **Responsive**: Compatible con dispositivos móviles

## Estructura del Proyecto
```
src/
├── main/
│   ├── java/
│   │   └── com/duoc/seguridadcalidad/
│   │       ├── SeguridadcalidadApplication.java  # Aplicación principal
│   │       └── controladores/
│   │           └── WebController.java            # Controlador para páginas
│   └── resources/
│       ├── static/                              # Archivos CSS
│       │   ├── style.css                        # Estilos principales
│       │   ├── inicio.css                       # Estilos página inicio
│       │   ├── login.css                        # Estilos página login
│       │   └── detalle.css                      # Estilos página detalle
│       ├── templates/                           # Plantillas Thymeleaf
│       │   ├── inicio.html                      # Página principal
│       │   ├── login.html                       # Página de login
│       │   └── detalle.html                     # Página de recetas
│       └── application.properties               # Configuración
```

## Tecnologías Utilizadas
- **Spring Boot 3.1.12**: Framework principal
- **Thymeleaf**: Motor de plantillas
- **Maven**: Gestión de dependencias
- **Java 17**: Versión de Java

## Instalación y Ejecución

### Prerrequisitos
- Java 17 o superior
- Maven (incluido con wrapper)

### Pasos de instalación
1. **Clonar el repositorio**
   ```bash
   git clone [url-del-repositorio]
   cd recetaAppJava-frontend
   ```

2. **Compilar el proyecto**
   ```bash
   ./mvnw clean compile
   ```

3. **Ejecutar la aplicación**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Acceder a la aplicación**
   - URL: `http://localhost:8080`
   - Puerto: 8080 (configurable en application.properties)

## Páginas Disponibles
- **/** - Página principal con recetas destacadas
- **/login** - Página de autenticación (frontend)
- **/detalle** - Página con recetas detalladas

## Configuración
La aplicación se configura a través del archivo `application.properties`:
```properties
# Nombre de la aplicación
spring.application.name=receta-frontend

# Puerto del servidor
server.port=8080

# Configuración de Thymeleaf
spring.thymeleaf.cache=false
```

## Desarrollo

### Estructura de archivos CSS
- `style.css`: Variables CSS, reset y estilos base
- `inicio.css`: Estilos específicos para la página principal
- `login.css`: Estilos para formulario de login
- `detalle.css`: Estilos para páginas de recetas detalladas

### Plantillas Thymeleaf
Las plantillas HTML utilizan Thymeleaf para:
- Referencias a archivos CSS: `th:href="@{/archivo.css}"`
- Links entre páginas: `th:href="@{/ruta}"`

## Integración con Backend
Esta aplicación frontend está diseñada para conectarse con un backend separado que maneja:
- Autenticación de usuarios
- Gestión de datos de recetas
- API REST endpoints

**Nota**: La conexión a base de datos y autenticación se maneja en un repositorio backend separado.

## Migración Completada
✅ **Archivos estáticos migrados** desde `recetaAppFrontend-static/` a la estructura Spring Boot   
✅ **Templates Thymeleaf** configurados y funcionando  
✅ **Controlador Web** implementado para routing  
✅ **Dependencias simplificadas** sin lógica de backend  
✅ **Elimando Backend REST** para mantener solo frontend  
✅ **Configuración limpiada** sin referencias a base de datos  

## Soporte
Para problemas o preguntas sobre la aplicación, contactar al equipo de desarrollo.