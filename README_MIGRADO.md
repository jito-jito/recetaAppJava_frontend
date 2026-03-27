# RecetaApp - Aplicación Web Frontend

Esta es la aplicación frontend de RecetaApp, ahora migrada completamente a una aplicación web Java con Thymeleaf, eliminando toda la lógica de backend REST API.

## ✅ Migración Completada

La aplicación ha sido migrada exitosamente de un backend REST API a una aplicación web frontend pura con las siguientes mejoras:

### 🎯 Funcionalidades Implementadas

- ✅ **Frontend Web Completo**: Aplicación web funcional usando Spring Boot + Thymeleaf
- ✅ **Sistema de Autenticación**: Login/logout usando Spring Security
- ✅ **Páginas Responsivas**: Diseño moderno y responsive en todas las páginas
- ✅ **Gestión de Sesiones**: Autenticación basada en sesiones web
- ✅ **Usuarios de Prueba**: Sistema de usuarios preconfigurados

### 🏗️ Arquitectura

```
src/
├── main/
│   ├── java/com/duoc/seguridadcalidad/
│   │   ├── controladores/
│   │   │   └── WebController.java           # Controlador web para páginas
│   │   ├── modelos/
│   │   │   └── Usuario.java                 # Modelo de usuario
│   │   ├── repositorios/
│   │   │   └── UserRepository.java          # Repositorio de usuarios
│   │   ├── MyUserDetailsService.java        # Servicio de autenticación
│   │   ├── WebSecurityConfig.java           # Configuración de seguridad
│   │   └── SeguridadcalidadApplication.java # Aplicación principal
│   └── resources/
│       ├── static/                          # Archivos CSS
│       │   ├── style.css
│       │   ├── inicio.css
│       │   ├── login.css
│       │   └── detalle.css
│       ├── templates/                       # Plantillas Thymeleaf
│       │   ├── inicio.html
│       │   ├── login.html
│       │   └── detalle.html
│       ├── application.properties
│       └── data.sql                         # Datos de usuarios de prueba
```

### 🚀 Cómo Ejecutar

1. **Compilar el proyecto**:
   ```bash
   ./mvnw clean compile
   ```

2. **Ejecutar la aplicación**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Acceder a la aplicación**:
   - URL: http://localhost:8080
   - Base de datos H2: http://localhost:8080/h2-console

### 👥 Usuarios de Prueba

La aplicación incluye usuarios preconfigurados:

- **Usuario**: `user` / Contraseña: `password`
- **Administrador**: `admin` / Contraseña: `password`

### 🌐 Páginas Disponibles

#### 📄 Página Principal (`/`)
- **Acceso**: Público (sin autenticación)
- **Funcionalidades**:
  - Presentación de la aplicación
  - Búsqueda y filtrado de recetas
  - Vista previa de recetas disponibles
  - Diseño responsive con animaciones

#### 🔐 Página de Login (`/login`)
- **Acceso**: Público 
- **Funcionalidades**:
  - Formulario de inicio de sesión
  - Manejo de errores de autenticación
  - Redirección automática después del login
  - Mensajes de estado (logout exitoso)

#### 📚 Página de Detalles (`/detalle`)
- **Acceso**: Requiere autenticación
- **Funcionalidades**:
  - Vista completa de recetas detalladas
  - Información del usuario logueado
  - Ingredientes e instrucciones completas
  - Dashboard privado personalizado

### 🛡️ Seguridad

- **Autenticación**: Spring Security con formularios web
- **Sesiones**: Gestión automática de sesiones
- **Protección**: Páginas protegidas requieren autenticación
- **Logout**: Cierre de sesión seguro

### 🎨 Diseño

- **Framework CSS**: CSS custom con variables CSS
- **Tipografía**: Poppins, Inter para diferentes secciones
- **Responsive**: Diseño adaptable a dispositivos móviles
- **Animaciones**: Transiciones suaves y efectos hover

### 📊 Base de Datos

- **Motor**: H2 en memoria (desarrollo)
- **Tablas**: Solo tabla `Usuario` (eliminadas tablas de recetas REST)
- **Datos**: Usuarios de prueba precargados
- **Persistencia**: Datos se reinician al relanzar la aplicación

### 🔧 Tecnologías

- **Backend**: Spring Boot 3.1.12, Spring Security 6
- **Frontend**: Thymeleaf + HTML5 + CSS3 + JavaScript
- **Base de Datos**: H2 Database (memoria)
- **Build Tool**: Maven 3
- **Java**: Versión 17

### 📝 Cambios Realizados en la Migración

#### ✅ Elementos Migrados
- ✅ Archivos CSS movidos a `src/main/resources/static/`
- ✅ Templates HTML migrados a `src/main/resources/templates/`
- ✅ Spring Security configurado para formularios web
- ✅ Controlador web para servir páginas HTML
- ✅ Sistema de autenticación con sesiones

#### 🗑️ Elementos Eliminados (Backend REST)
- ❌ Controladores REST API
- ❌ Modelos de recetas (Receta, Ingrediente, etc.)
- ❌ Repositorios de recetas
- ❌ Configuración JWT
- ❌ Dependencias JWT
- ❌ Archivos de datos de recetas

#### 🔄 Elementos Modificados
- 🔄 WebSecurityConfig: De JWT a formularios
- 🔄 POM.xml: Agregado Thymeleaf, eliminado JWT
- 🔄 MyUserDetailsService: Configurado para web
- 🔄 data.sql: Solo usuarios, sin recetas

---

## 📖 Notas de Desarrollo

### Estructura del Frontend
- Las recetas mostradas ahora son datos estáticos en HTML
- La funcionalidad de búsqueda es puramente client-side (JavaScript)
- No se conecta a ningún backend REST API

### Configuración de Seguridad
- Autenticación basada en formularios web
- PasswordEncoder sin cifrado para datos de prueba
- Redirección automática después del login

### Base de Datos
- Database URL: `jdbc:h2:mem:recetadb`
- Usuario H2: `sa` (sin contraseña)
- La base de datos se reinicia en cada ejecución

---

## 🎯 Resultado Final

**✅ Migración Exitosa**: La aplicación ahora funciona como un frontend web puro en Java, sin lógica de backend REST API, manteniendo todas las funcionalidades visuales y de autenticación necesarias.