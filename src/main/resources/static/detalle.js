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
    });
});