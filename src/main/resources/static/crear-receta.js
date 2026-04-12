// crear-receta.js
document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Manejo dinámico de ingredientes
    const ingredientsContainer = document.getElementById('ingredientsContainer');
    const addIngredientBtn = document.getElementById('addIngredientBtn');

    addIngredientBtn.addEventListener('click', () => {
        const newRow = document.createElement('div');
        newRow.className = 'ingredient-row';
        newRow.innerHTML = `
            <input type="text" class="form-control i-nombre" placeholder="Nombre (Ej: Tomate)" required>
            <input type="number" class="form-control i-cantidad" placeholder="Cant." step="0.01" min="0" required>
            <input type="text" class="form-control i-unidad" placeholder="Unidad" required>
            <button type="button" class="btn-remove-ingredient" title="Eliminar ingrediente">✕</button>
        `;
        ingredientsContainer.appendChild(newRow);

        // Activamos evento al botoncito recién creado
        const removeBtn = newRow.querySelector('.btn-remove-ingredient');
        removeBtn.addEventListener('click', () => {
            ingredientsContainer.removeChild(newRow);
        });
    });

    // 2. Manejo Logout
    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
                try {
                    const response = await fetch('/api/logout', { method: 'POST' });
                    if(response.ok) {
                        window.location.href = '/login';
                    }
                } catch (error) {
                    console.error('Error al cerrar sesión:', error);
                }
            }
        });
    }

    // 3. Envío del Formulario
    const recipeForm = document.getElementById('recipeForm');
    const submitBtn = document.getElementById('submitBtn');
    
    // Obtener Token Inyectado en el HTML
    const metaToken = document.querySelector('meta[name="api-token"]');
    const jwtToken = metaToken ? metaToken.getAttribute('content') : '';

    recipeForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // 3.1 Armar Objeto 'receta'
        const ingredientesNodes = document.querySelectorAll('.ingredient-row');
        const ingredientesList = [];
        
        ingredientesNodes.forEach(node => {
            ingredientesList.push({
                nombre: node.querySelector('.i-nombre').value,
                cantidad: parseFloat(node.querySelector('.i-cantidad').value),
                unidadMedida: node.querySelector('.i-unidad').value
            });
        });

        const recetaData = {
            titulo: document.getElementById('titulo').value,
            tipoCocina: document.getElementById('tipoCocina').value,
            paisOrigen: document.getElementById('paisOrigen').value,
            dificultad: document.getElementById('dificultad').value,
            tiempoCoccion: parseInt(document.getElementById('tiempoCoccion').value),
            instrucciones: document.getElementById('instrucciones').value,
            popularidad: 5.0, // base rating para una receta nueva
            fechaPublicacion: new Date().toISOString().split('T')[0],
            ingredientes: ingredientesList
        };

        // 3.2 Construir FormData
        const formData = new FormData();
        formData.append('receta', JSON.stringify(recetaData));

        const imageFiles = document.getElementById('inputImages').files;
        for(let i=0; i<imageFiles.length; i++) {
            formData.append('imagenes', imageFiles[i]);
        }

        const videoFiles = document.getElementById('inputVideos').files;
        for(let i=0; i<videoFiles.length; i++) {
            formData.append('videos', videoFiles[i]);
        }

        // 3.3 Enviar a la API externa
        submitBtn.disabled = true;
        submitBtn.textContent = 'Subiendo y Publicando...';

        try {
            const response = await fetch('/api/recipes', {
                method: 'POST',
                headers: {
                    // El Token ya no es estrictamente necesario inyectarlo manual aquí porque 
                    // nuestro proxy Spring Boot lee nuestra HttpSession automáticamente.
                    // Pero lo enviamos de todos modos por compatibilidad.
                    'Authorization': jwtToken 
                },
                body: formData
            });

            if(response.ok) {
                alert('¡Receta creada con éxito!');
                window.location.href = '/'; 
            } else if (response.status === 401 || response.status === 403) {
                alert('Error de autenticación. Tu sesión pudo haber expirado o token inválido.');
                window.location.href = '/login';
            } else {
                const errorData = await response.text();
                alert('Ocurrió un error al crear la receta: ' + errorData);
            }
        } catch (error) {
            console.error('Error enviando la receta:', error);
            alert('Error de red al intentar comunicarse con el backend puerto 8080.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Publicar Receta';
        }
    });
});
