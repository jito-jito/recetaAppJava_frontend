# Análisis Post-Corrección — SonarQube RecetaApp Frontend

**Fecha del re-análisis:** 12 de abril de 2026  
**Proyecto:** `receta-app-frontend` (714 líneas de código)  
**Versión:** 0.0.1-SNAPSHOT

---

## 1. Comparación Antes vs Después

| Métrica | Antes | Después | Cambio |
|---------|-------|---------|--------|
| **Total de issues** | 50 | 4 | ✅ **-92%** |
| **Bugs** | 23 | 1 | ✅ **-96%** |
| **Code Smells** | 27 | 3 | ✅ **-89%** |
| **Vulnerabilidades** | 0 | 0 | ✅ Mantenido |
| **Issues BLOCKER** | 10 | 0 | ✅ **-100%** |
| **Issues CRITICAL** | 21 | 1 | ✅ **-95%** |
| **Issues MAJOR** | 16 | 1 | ✅ **-94%** |
| **Issues MINOR** | 3 | 2 | ✅ **-33%** |
| **Deuda técnica** | 613 min (~10.2h) | 55 min | ✅ **-91%** |
| **Duplicación (Overall)** | — | 7.7% | ⚠️ presente |

### Calificaciones de Calidad (Overall Code)

| Rating | Valor | Nota |
|--------|-------|------|
| **Fiabilidad (Reliability)** | C (3.0) | ⚠️ 1 bug pendiente |
| **Seguridad (Security)** | A (1.0) | ✅ Excelente |
| **Mantenibilidad** | A (1.0) | ✅ Excelente |
| **Deuda técnica ratio** | 0.2% | ✅ Excelente |

---

## 2. Quality Gate — Estado Actual

El Quality Gate falla en **3 condiciones** (sobre New Code):

| Condición | Valor actual | Requerido | Estado |
|-----------|-------------|-----------|--------|
| New Issues | 4 | 0 | ❌ Failed |
| Coverage | 0.0% | ≥ 80.0% | ❌ Failed |
| Duplicated Lines (%) | 16.15% | ≤ 3.0% | ❌ Failed |

> **Nota:** La métrica de "New Code" en SonarQube compara contra el período base establecido. Como el archivo fue reescrito completamente, SonarQube trata las 714 líneas como "new code", lo que explica por qué el % de duplicación aparece alto en el tab "New Code" (16.15%) vs. "Overall Code" (7.7%).

---

## 3. Issues Nuevas Pendientes (4)

### Issue 1 y 2 — URIs hardcodeadas como constantes (S1075)

| Campo | Detalle |
|-------|---------|
| **Regla** | `java:S1075` — *Refactor your code to get this URI from a customizable parameter* |
| **Severidad** | MINOR |
| **Tipo** | Code Smell |
| **Líneas** | 69 (`RECIPES_PATH = "/recipes/"`) y 70 (`FAVORITOS_API_PATH = "/api/usuarios/favoritos"`) |
| **Esfuerzo** | 20 min c/u |

**Qué pide SonarQube:** Que las rutas de API no estén hardcodeadas como constantes sino que vengan de un parámetro configurable (ej: `application.properties`).

**Recomendación:** Para ambiente local esto es aceptable. En producción se podría usar `@Value("${api.recipes.path}")` desde `application.properties`. Esta es una issue de tipo **MINOR** que no afecta funcionalidad.

---

### Issue 3 — String literal duplicado "Error de conexión: " (S1192)

| Campo | Detalle |
|-------|---------|
| **Regla** | `java:S1192` — *Define a constant instead of duplicating this literal 4 times* |
| **Severidad** | CRITICAL |
| **Tipo** | Code Smell |
| **Línea** | 517 |
| **Esfuerzo** | 10 min |

**Qué pide SonarQube:** El literal `"Error de conexión: "` (sin "con el backend") se repite 4 veces en los métodos de favoritos. Se diferencia del ya extraído `MSG_ERROR_CONEXION` ("Error de conexión con el backend: ").

---

### Issue 4 — HttpStatus no reflectivo (S6863)

| Campo | Detalle |
|-------|---------|
| **Regla** | `java:S6863` — *Set a HttpStatus code reflective of the operation* |
| **Severidad** | MAJOR |
| **Tipo** | Bug |
| **Línea** | 607 (método `obtenerFavoritos()`) |
| **Esfuerzo** | 5 min |

**Qué pide SonarQube:** En el `catch (Exception e)` genérico, se retorna `ResponseEntity.ok()` (HTTP 200) cuando en realidad hubo un error. Debería retornar un código de error.

---

## 4. Duplicación de código (7.7% Overall)

SonarQube detecta **4 bloques duplicados** en 74 líneas (1 archivo). Los bloques duplicados corresponden al **patrón repetitivo de los métodos proxy** (agregarFavorito, quitarFavorito, postComentario, postValoracion) que tienen estructura similar:

```
verificar token → construir HttpRequest → enviar → procesar respuesta → catch
```

Esto es un patrón común en controladores BFF/proxy y para reducirlo significativamente habría que abstraer la lógica en un método genérico tipo `proxyRequest()`. Sin embargo, esa refactorización **no es crítica** dado que cada método maneja rutas, métodos HTTP y respuestas ligeramente diferentes.

---

## 5. Coverage (0.0%)

La cobertura de tests es 0% porque el proyecto solo tiene el test de contexto por defecto de Spring Boot (`SeguridadcalidadApplicationTests`), que verifica que el contexto de la aplicación carga correctamente pero no ejecuta lógica del controlador.

Para mejorar la cobertura se requiere:
- Configurar JaCoCo como plugin de Maven para generar reportes de cobertura
- Crear tests unitarios con `MockMvc` para los endpoints del `WebController`
- Crear tests de integración para los flujos de autenticación

> **Nota:** El Quality Gate por defecto de SonarQube requiere ≥ 80% de cobertura en new code. Para un proyecto académico en ambiente local, esto es informativo y no bloqueante a nivel funcional.

---

## 6. Resumen Visual

```
ANTES:  50 issues  ████████████████████████████████████████████████████
DESPUÉS: 4 issues  ████

Reducción: 92% ✅
```

| Categoría | Antes → Después |
|-----------|----------------|
| 🔴 BLOCKER | 10 → **0** ✅ |
| 🟠 CRITICAL | 21 → **1** |
| 🟡 MAJOR | 16 → **1** |
| 🟢 MINOR | 3 → **2** |
| 🐛 Bugs | 23 → **1** |
| 🧹 Code Smells | 27 → **3** |
| 🔒 Vulnerabilidades | 0 → **0** |

---

*Documento generado como parte del proceso de aseguramiento de calidad — Seguridad y Calidad de Software, Duoc UC.*
