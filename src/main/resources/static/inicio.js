// JavaScript para la página de inicio
// Funciones de búsqueda y filtrado

// Flag: solo mostrar "no resultados" si el usuario ya interactuó con el buscador/filtros
let userHasFiltered = false;

function searchRecipes() {
    userHasFiltered = true;
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    filterRecipes(searchTerm);
}

function filterRecipes(searchTerm = '') {
    const cards = document.querySelectorAll('.recipe-card');
    const difficultyFilter = document.getElementById('difficultyFilter').value;
    const timeFilter = document.getElementById('timeFilter').value;
    const typeFilter = document.getElementById('typeFilter').value;
    const currentSearchTerm = searchTerm || document.getElementById('searchInput').value.toLowerCase();
    
    let visibleCount = 0;

    cards.forEach(card => {
        const difficulty = card.dataset.difficulty;
        const time = parseInt(card.dataset.time);
        const type = card.dataset.type;
        const keywords = (card.dataset.keywords || '').toLowerCase();
        const titleEl = card.querySelector('h3');
        const title = titleEl ? titleEl.textContent.toLowerCase() : '';
        
        let showCard = true;

        // Filtro de búsqueda de texto
        if (currentSearchTerm && !keywords.includes(currentSearchTerm) && !title.includes(currentSearchTerm)) {
            showCard = false;
        }

        // Filtro de dificultad
        if (difficultyFilter && difficulty !== difficultyFilter) {
            showCard = false;
        }

        // Filtro de tiempo
        if (timeFilter) {
            if (timeFilter === 'quick' && time >= 20) showCard = false;
            if (timeFilter === 'medium' && (time < 20 || time > 40)) showCard = false;
            if (timeFilter === 'long' && time <= 40) showCard = false;
        }

        // Filtro de tipo
        if (typeFilter && type !== typeFilter) {
            showCard = false;
        }

        if (showCard) {
            card.style.display = 'block';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });

    // Solo mostrar "no resultados" si el usuario ya buscó/filtró activamente
    const noResults = document.getElementById('noResults');
    if (noResults) {
        if (visibleCount === 0 && userHasFiltered) {
            noResults.classList.add('visible');
        } else {
            noResults.classList.remove('visible');
        }
    }
}

function searchByKeyword(keyword) {
    userHasFiltered = true;
    document.getElementById('searchInput').value = keyword;
    searchRecipes();
}

function clearFilters() {
    userHasFiltered = false; // reset: volvemos a "carga inicial"
    document.getElementById('searchInput').value = '';
    document.getElementById('difficultyFilter').value = '';
    document.getElementById('timeFilter').value = '';
    document.getElementById('typeFilter').value = '';
    // Mostrar todas las cards sin activar el mensaje de "no resultados"
    document.querySelectorAll('.recipe-card').forEach(card => {
        card.style.display = 'block';
    });
    const noResults = document.getElementById('noResults');
    if (noResults) noResults.classList.remove('visible');
}

// Funciones para usuarios autenticados
async function addToMyList(idReceta, titulo, buttonElement) {
    try {
        const response = await fetch(`/api/favoritos/${idReceta}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                alert(`¡"${titulo}" se añadió a tu lista de favoritas!`);
                
                // Cambiar el botón visual para indicar que ya está en favoritos
                if (buttonElement) {
                    buttonElement.innerHTML = '<span class="btn-icon">✅</span> En favoritas';
                    buttonElement.classList.add('added-to-favorites');
                    buttonElement.disabled = true;
                }
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
        console.error('Error al agregar favorito:', error);
        alert('Error de conexión. Inténtalo de nuevo.');
    }
}

async function handleLogoutFromHome() {
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
                // Recargar la página para actualizar el estado de autenticación
                window.location.reload();
            } else {
                alert('Error al cerrar sesión: ' + result.message);
            }
            
        } catch (error) {
            console.error('Error:', error);
            // Recargar de cualquier forma
            window.location.reload();
        }
    }
}

// Event listeners - ejecutar cuando se carga el DOM
document.addEventListener('DOMContentLoaded', function() {
    // Búsqueda en tiempo real
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            userHasFiltered = true;
            setTimeout(filterRecipes, 300); // Debounce de 300ms
        });

        // Enter para buscar
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchRecipes();
            }
        });
    }

    // Botón de búsqueda
    const searchBtn = document.getElementById('searchBtn');
    if (searchBtn) {
        searchBtn.addEventListener('click', searchRecipes);
    }

    // Filtros
    const difficultyFilter = document.getElementById('difficultyFilter');
    if (difficultyFilter) {
        difficultyFilter.addEventListener('change', function() { userHasFiltered = true; filterRecipes(); });
    }

    const timeFilter = document.getElementById('timeFilter');
    if (timeFilter) {
        timeFilter.addEventListener('change', function() { userHasFiltered = true; filterRecipes(); });
    }

    const typeFilter = document.getElementById('typeFilter');
    if (typeFilter) {
        typeFilter.addEventListener('change', function() { userHasFiltered = true; filterRecipes(); });
    }

    // Botón limpiar filtros
    const clearFiltersBtn = document.getElementById('clearFiltersBtn');
    if (clearFiltersBtn) {
        clearFiltersBtn.addEventListener('click', clearFilters);
    }

    // Botón de logout
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogoutFromHome);
    }

    // Event delegation para botones dinámicos (favoritos)
    document.addEventListener('click', function(e) {
        // Botones de agregar a favoritos
        if (e.target.closest('.add-to-list-btn') && !e.target.disabled) {
            const button = e.target.closest('.add-to-list-btn');
            const recipeId = button.dataset.recipeId;
            const recipeTitle = button.dataset.recipeTitle;
            addToMyList(recipeId, recipeTitle, button);
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
