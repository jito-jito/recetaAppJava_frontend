// JavaScript para la página de detalle

async function handleLogout() {
    if (confirm('¿Estás seguro de que quieres cerrar sesión?')) {
        try {
            const response = await fetch('/api/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            const result = await response.json();
            
            if (result.success) {
                // Redireccionar al inicio en lugar de login
                window.location.href = '/';
            } else {
                alert('Error al cerrar sesión: ' + result.message);
            }
            
        } catch (error) {
            console.error('Error:', error);
            // Redireccionar de cualquier forma al inicio
            window.location.href = '/';
        }
    }
}

// Función para quitar de favoritos
async function removeFromFavorites(idReceta, titulo) {
    if (confirm(`¿Estás seguro de que quieres quitar "${titulo}" de tus favoritas?`)) {
        try {
            const response = await fetch(`/api/favoritos/${idReceta}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const result = await response.json();
                if (result.success) {
                    alert(`"${titulo}" ha sido quitada de tus favoritas.`);
                    // Recargar la página para mostrar la lista actualizada
                    window.location.reload();
                } else {
                    alert('Error: ' + result.message);
                }
            } else if (response.status === 401) {
                alert('Tu sesión ha expirado. Redirigiendo al login...');
                window.location.href = '/login';
            } else {
                throw new Error(`Error ${response.status}`);
            }
        } catch (error) {
            console.error('Error al quitar favorito:', error);
            alert('Error de conexión. Inténtalo de nuevo.');
        }
    }
}

// Función para cambiar el estado de publicación (Borrador / Publicada)
async function togglePublishStatus(idReceta, isPublishedStr) {
    const isCurrentlyPublished = isPublishedStr === 'true';
    const nextStatus = !isCurrentlyPublished;
    const accion = isCurrentlyPublished ? "ocultar (hacer borrador)" : "publicar";
    
    if (confirm(`¿Estás seguro de que deseas ${accion} esta receta?`)) {
        try {
            const response = await fetch(`/api/recipes/${idReceta}/estado?publicada=${nextStatus}`, {
                method: 'PUT'
            });

            if (response.ok) {
                alert(`!Receta ${nextStatus ? 'publicada' : 'ocultada'} con éxito!`);
                window.location.reload();
            } else if (response.status === 401 || response.status === 403) {
                alert('Tu sesión ha expirado o no tienes permisos.');
                window.location.href = '/login';
            } else {
                const text = await response.text();
                alert('Error al cambiar el estado de la receta: ' + text);
            }
        } catch (error) {
            console.error('Error al cambiar estado:', error);
            alert('Error de red al comunicarse con el proxy frontend.');
        }
    }
}

// Event listeners - ejecutar cuando se carga el DOM
document.addEventListener('DOMContentLoaded', function() {
    // Botón de logout
    const logoutBtn = document.getElementById('logoutBtnDetail');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }

    // Event delegation para botones dinámicos (remover favoritos)
    document.addEventListener('click', function(e) {
        if (e.target.closest('.remove-from-list-btn')) {
            const button = e.target.closest('.remove-from-list-btn');
            const recipeId = button.dataset.recipeId;
            const recipeTitle = button.dataset.recipeTitle;
            removeFromFavorites(recipeId, recipeTitle);
        }
        
        // Delegación para botón de cambiar estado borrador/público
        if (e.target.closest('.toggle-status-btn')) {
            const button = e.target.closest('.toggle-status-btn');
            const recipeId = button.dataset.recipeId;
            const currentStatus = button.dataset.currentStatus;
            togglePublishStatus(recipeId, currentStatus);
        }
    });
});
// Manejador global para ocultar multimedia rota
document.addEventListener('error', function(e) {
    if (e.target && e.target.tagName && (e.target.tagName.toLowerCase() === 'img' || e.target.tagName.toLowerCase() === 'video')) {
        e.target.style.display = 'none';
    }
}, true);

// Función para revisar imagenes que fallaron antes de que el script cargara
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('img.recipe-media-element, img.recipe-media-item').forEach(img => {
        if (img.complete && img.naturalHeight === 0) {
            img.style.display = 'none';
        }
    });
});
