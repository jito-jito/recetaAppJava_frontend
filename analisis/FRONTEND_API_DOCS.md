# Documentación de API - Endpoints de Recetas

Esta documentación detalla los endpoints necesarios para que un frontend pueda interactuar con el catálogo de recetas.

---

## 🌎 Endpoints Públicos (No requieren Autenticación)

### 1. Obtener todas las recetas
Retorna una lista completa de las recetas registradas en la plataforma.

**Petición:**
```http
GET /recipes
Host: localhost:8080
```

**Respuesta Exitosa (200 OK):**
```json
[
  {
    "idReceta": 1,
    "titulo": "Tacos al Pastor",
    "tipoCocina": "Urbana",
    "paisOrigen": "México",
    "dificultad": "MEDIA",
    "tiempoCoccion": 50,
    "instrucciones": "Marinar la carne y asar...",
    "popularidad": 4.5,
    "fechaPublicacion": "2025-12-17",
    "imagenes": [
      "/media/imagenes/uuid-de-ejemplo.jpg"
    ],
    "videos": [],
    "puntajePromedio": 4.5,
    "cantidadComentarios": 0,
    "ingredientes": [
      {
        "nombre": "Cerdo",
        "cantidad": 1.0,
        "unidadMedida": "kg"
      }
    ]
  }
]
```

### 2. Obtener una receta por ID
Permite recuperar toda la información detallada de una receta específica basándose en su ID.

**Petición:**
```http
GET /recipes/{id}
Host: localhost:8080
```

**Respuesta Exitosa (200 OK):** *(Retorna un solo objeto JSON con la estructura mostrada arriba).*

---

## 🔒 Endpoints Protegidos (Requieren Token JWT)

Para cualquier petición protegida, debes enviar un token activo de un usuario logueado en las cabeceras HTTP:
**Header necesario:** `Authorization: Bearer <TU_TOKEN_JWT>`

### 3. Crear Nueva Receta (con Archivos Multimedia)

Este endpoint ya no recibe un JSON en crudo, sino que requiere una petición **`multipart/form-data`** para poder subir y adjuntar los archivos de foto y video físicamente al sistema.

**Petición:**
```http
POST /recipes
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
Content-Type: multipart/form-data; boundary=---boundary
```

#### Parámetros del Form-Data:

| Nombre (Key) | Tipo | Opcional | Descripción |
| :--- | :--- | :--- | :--- |
| `receta` | Text/JSON | **No** | Toda la estructura de datos de la receta enviada como un gran string en formato JSON. (*Ver ejemplo abajo*). |
| `imagenes` | File | Sí | Uno o más archivos de imagen a adjuntar a la receta (.jpg, .png). Puedes mandar esta llave (key) múltiples veces para subir varias fotos a la vez. |
| `videos` | File | Sí | Uno o más archivos de video a adjuntar a la receta (.mp4, .mov). Peso máximo 500MB. |

**Ejemplo de cómo construir el valor string para la llave `receta`:**
```json
{
  "titulo": "Nueva Receta Exquisita",
  "tipoCocina": "Italiana",
  "paisOrigen": "Italia",
  "dificultad": "ALTA",
  "tiempoCoccion": 45,
  "instrucciones": "Paso 1: Hervir el agua. Paso 2...",
  "popularidad": 5.0,
  "fechaPublicacion": "2026-04-12",
  "ingredientes": [
    {
      "nombre": "Pasta Fresca",
      "cantidad": 300,
      "unidadMedida": "gramos"
    }
  ]
}
```

> **Atención Desarrolladores UI/Front**: 
> Si usas **Axios** en React o Vue, envía tu petición utilizando un objeto `FormData`:
> ```javascript
> const formData = new FormData();
> 
> // Convertir el objeto receta a un String JSON
> formData.append('receta', JSON.stringify({ titulo: "Mi Receta", dificultad: "MEDIA" /* etc */ }));
> 
> // Añadir archivos físicos obtenidos desde un <input type="file" multiple />
> let fileInputImages = document.querySelector('#input-images').files;
> for (let i = 0; i < fileInputImages.length; i++) {
>     formData.append('imagenes', fileInputImages[i]); 
> }
> 
> // Realizar la petición: Axios manejará el boundary del Form-Data automáticamente por ti.
> axios.post('http://localhost:8080/recipes', formData, {
>     headers: { 'Authorization': `Bearer ${token}` }
> });
> ```

**Respuesta Exitosa (201 Created):**
Devolverá de inmediato el objeto estructurado JSON de la receta creada (con el `idReceta` generado por el backend y los arrays de `imagenes` / `videos` llenos con las URLs donde quedaron alojados en el servidor). Nota de privacidad: Todas las recetas creadas inician como **borradores ocultos (`publicada: false`)** y no saldrán en los endpoints públicos hasta que se cambie su estado a publicado.

---

### 4. Obtener "Mis Recetas" (Borradores y Publicadas)

Devuelve una lista con TODAS las recetas que el usuario actualmente logueado ha creado. Es el único endpoint que retorna recetas en modo borrador (`publicada: false`). Ideal para diseñar un Dashboard de creador de contenido.

**Petición:**
```http
GET /recipes/mis-recetas
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
```
*No requiere mandar el ID o nombre del usuario, el backend lo deduce seguromente desde el Token JWT.*

---

### 5. Publicar / Despublicar una Receta

Permite cambiar el estado de privacidad de una de tus recetas para que aparezca (o desaparezca) del catálogo público y resultados de búsqueda. Solo el creador original de la receta está autorizado a hacer esto.

**Petición:**
```http
PUT /recipes/{id_receta}/estado?publicada=true
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
```

**Parámetros URL:**
- `publicada=true` -> Publica la receta para que todos la vean.
- `publicada=false` -> Oculta la receta convirtiéndola en un borrador privado.

**Respuestas:**
- `200 OK`: Cambio exitoso. Retorna la receta modificada.
- `403 Forbidden`: Intento de cambiar de estado a una receta ajena.
- `404 Not Found`: La receta no existe.

---

### 6. Sistema de Comentarios

Permite a los usuarios dejar y leer opiniones (texto) acerca de una receta específica.

**A. Obtener Comentarios (GET)**
Devuelve una lista con todos los comentarios de una receta, ordenados del más reciente al más antiguo.
```http
GET /recipes/{id_receta}/comentarios
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
```
**Respuesta Exitosa (200 OK):**
```json
[
  {
    "idComentario": 1,
    "texto": "¡Me encantó esta receta, muy fácil de preparar!",
    "autor": "usuario_fan",
    "fecha": "2026-04-12T14:30:00"
  }
]
```

**B. Agregar un Comentario (POST)**
```http
POST /recipes/{id_receta}/comentarios
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
Content-Type: application/json
```
**Body JSON:**
```json
{
    "texto": "Tu comentario aquí..."
}
```
*Retorna (200 OK) el JSON de la receta actualizada reflejando su nuevo `cantidadComentarios`.*

---

### 7. Sistema de Valoraciones (Puntajes)

Permite a los usuarios calificar una receta de forma totalmente disociada a los comentarios. 
**Regla de Negocio:** El creador de la receta tiene prohibido auto-valorar su plato. Si un autor lo intenta, el backend devolverá `403 Forbidden`.

**A. Obtener Valoraciones (GET)**
```http
GET /recipes/{id_receta}/valoraciones
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
```
**Respuesta Exitosa (200 OK):**
```json
[
  {
    "idValoracion": 1,
    "puntaje": 5,
    "autor": "usuario_fan"
  }
]
```

**B. Agregar o Actualizar Valoración (POST)**
Se admite un `puntaje` entero entre `0` y `5`. Si el mismo usuario envía un puntaje hacia la misma receta por segunda vez, se actualizará su valoración anterior.
```http
POST /recipes/{id_receta}/valoraciones
Host: localhost:8080
Authorization: Bearer <TU_TOKEN_JWT>
Content-Type: application/json
```
**Body JSON:**
```json
{
    "puntaje": 4
}
```
*Retorna (200 OK) el JSON de la receta actualizada reflejando su nuevo `puntajePromedio`.*

---

## 🖼️ Acceso a Medios (Archivos Estáticos)

Si en el JSON de una receta recibes que una imagen es `"/media/imagenes/4b56ec39.jpg"`, entonces como frontend debes formar la ruta completa apuntando al backend local para poder renderizarla en una etiqueta HTML:

```html
<img src="http://localhost:8080/media/imagenes/4b56ec39.jpg" />
```
