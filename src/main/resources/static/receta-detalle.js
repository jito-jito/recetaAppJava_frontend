// receta-detalle.js
document.addEventListener('DOMContentLoaded', () => {

    // Extraer Data de Meta Tags
    const apiTokenMeta = document.querySelector('meta[name="api-token"]');
    const apiToken = apiTokenMeta ? apiTokenMeta.getAttribute('content') : '';
    
    const recipeIdMeta = document.querySelector('meta[name="receta-id"]');
    const recipeId = recipeIdMeta ? recipeIdMeta.getAttribute('content') : null;

    const authMeta = document.querySelector('meta[name="is-authenticated"]');
    const isAuthenticated = authMeta ? authMeta.getAttribute('content') === 'true' : false;

    // ----- Manejo de media rota (Fallback Seguro CSP) -----
    // Como el CSP bloquea onerrror="..." inline en el HTML, nos suscribimos al error desde JS.
    const handleBrokenImage = (img) => {
        img.style.display = 'none';
        if (img.classList.contains('main-recipe-image')) {
            const container = img.closest('.main-image-container');
            if (container) container.style.display = 'none';
        }
    };

    const images = document.querySelectorAll('img.main-recipe-image, img.thumbnail-image');
    images.forEach(img => {
        // ¿Ya falló de antemano antes de inicializar JS? (Ej. Error 404 instantáneo del local network)
        if (img.complete && img.naturalHeight === 0) {
            handleBrokenImage(img);
        } else {
            // Aún iterando carga...
            img.addEventListener('error', function() {
                handleBrokenImage(this);
            });
        }
    });

    const videos = document.querySelectorAll('video.main-recipe-video, source');
    const handleBrokenVideo = (element) => {
        const videoElement = element.tagName.toLowerCase() === 'video' ? element : element.closest('video');
        if (videoElement) {
            videoElement.style.display = 'none';
            const container = videoElement.closest('.main-image-container');
            if (container) container.style.display = 'none';
        }
    };

    // Para videos source, la captura (true) asegura que el error llegue al manejador
    videos.forEach(media => {
        media.addEventListener('error', function() {
            handleBrokenVideo(this);
        }, true); 
        
        // Timeout para video: si networkState dice NETWORK_NO_SOURCE
        if (media.tagName.toLowerCase() === 'video' && media.networkState === HTMLMediaElement.NETWORK_NO_SOURCE) {
            handleBrokenVideo(media);
        }
    });

    if (!recipeId) return;

    // ----- Parse JWT -----
    function parseJwt(token) {
        if (!token) return null;
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    }

    const rawToken = apiToken.replace('Bearer ', '');
    const decodedToken = parseJwt(rawToken);
    const currentUser = decodedToken ? decodedToken.sub : null;
    const roles = decodedToken && decodedToken.authorities ? decodedToken.authorities : [];
    const isAdmin = roles.includes('ROLE_ADMIN');

    // ----- Cargar Comentarios -----
    const loadComments = async () => {
        const commentsList = document.getElementById('commentsList');
        try {
            const response = await fetch(`/api/recipes/${recipeId}/comentarios`);
            if (response.ok) {
                const comments = await response.json();
                renderComments(comments);
            } else {
                commentsList.innerHTML = `<div class="empty-state-comments">Error al cargar comentarios: ${response.status}</div>`;
            }
        } catch (error) {
            commentsList.innerHTML = `<div class="empty-state-comments">Error de red al cargar comentarios.</div>`;
            console.error(error);
        }
    };

    window.toggleCommentState = async (comentarioId, bloqueado) => {
        if (!confirm(`¿Estás seguro de que deseas ${bloqueado ? 'bloquear/ocultar' : 'reactivar'} este comentario?`)) return;
        
        try {
            const response = await fetch(`/api/recipes/${recipeId}/comentarios/${comentarioId}/estado?bloqueado=${bloqueado}`, {
                method: 'PUT'
            });
            if (response.ok) {
                await loadComments();
            } else {
                alert('Error al cambiar el estado del comentario');
            }
        } catch (error) {
            console.error(error);
            alert('Error de red');
        }
    };

    const renderComments = (comments) => {
        const commentsList = document.getElementById('commentsList');
        // Actualizar contadores del DOM
        document.getElementById('headerCommentsCount').textContent = comments.length;
        document.getElementById('commentsCountSpan').textContent = comments.length;

        if (comments.length === 0) {
            commentsList.innerHTML = `<div class="empty-state-comments">No hay comentarios todavía. ¡Sé el primero en opinar!</div>`;
            return;
        }

        commentsList.innerHTML = '';
        comments.forEach(c => {
            // Frontend validation safety
            if (c.bloqueado && !isAdmin && c.autor !== currentUser) {
                return; // Do not render if not admin and not author
            }

            const dateObj = new Date(c.fecha);
            const dateString = dateObj.toLocaleDateString() + ' ' + dateObj.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
            
            const card = document.createElement('div');
            card.className = 'comment-card' + (c.bloqueado ? ' blocked-comment' : '');
            
            const cId = c.id || c.idComentario || c.comentarioId;

            // Usar API del DOM para evitar bloqueos por CSP (inline handlers/styles)
            const header = document.createElement('div');
            header.className = 'comment-header';
            header.innerHTML = `<span class="comment-author">👤 ${c.autor}</span><span class="comment-date">${dateString}</span>`;
            card.appendChild(header);

            if (c.bloqueado) {
                const warning = document.createElement('div');
                warning.className = 'blocked-warning';
                warning.textContent = '⚠️ Este comentario fue bloqueado por contenido inapropiado.';
                warning.style.color = '#d63031';
                warning.style.fontSize = '0.9em';
                warning.style.marginBottom = '5px';
                warning.style.padding = '4px';
                warning.style.background = '#ffeaa7';
                warning.style.borderRadius = '4px';
                card.appendChild(warning);
            }

            const textDiv = document.createElement('div');
            textDiv.className = 'comment-text';
            // Usar textContent previene inyección XSS también
            textDiv.textContent = c.texto;
            if (c.bloqueado) {
                textDiv.style.opacity = '0.6';
                textDiv.style.fontStyle = 'italic';
            }
            card.appendChild(textDiv);

            if (isAdmin) {
                const actionBtn = document.createElement('button');
                actionBtn.style.color = 'white';
                actionBtn.style.border = 'none';
                actionBtn.style.padding = '5px 10px';
                actionBtn.style.borderRadius = '4px';
                actionBtn.style.cursor = 'pointer';
                actionBtn.style.fontSize = '0.8em';
                actionBtn.style.marginTop = '10px';

                if (c.bloqueado) {
                    actionBtn.className = 'btn-reactivate';
                    actionBtn.textContent = 'Reactivar Comentario';
                    actionBtn.style.background = '#27ae60';
                    actionBtn.addEventListener('click', () => {
                        if (!cId) { alert('Error: ID de comentario no encontrado'); return; }
                        window.toggleCommentState(cId, false);
                    });
                } else {
                    actionBtn.className = 'btn-block';
                    actionBtn.textContent = 'Ocultar/Bloquear';
                    actionBtn.style.background = '#e74c3c';
                    actionBtn.addEventListener('click', () => {
                        if (!cId) { alert('Error: ID de comentario no encontrado'); return; }
                        window.toggleCommentState(cId, true);
                    });
                }
                card.appendChild(actionBtn);
            }

            commentsList.appendChild(card);
        });
    };

    // ----- Publicar Comentario -----
    const commentForm = document.getElementById('commentForm');
    if (commentForm) {
        const submitBtn = document.getElementById('submitCommentBtn');
        const commentText = document.getElementById('commentText');

        commentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const text = commentText.value.trim();
            if (!text) return;

            submitBtn.disabled = true;
            submitBtn.textContent = 'Enviando...';

            try {
                // Post al proxy, que inyectará el token de la sesión Java
                const response = await fetch(`/api/recipes/${recipeId}/comentarios`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ texto: text })
                });

                if (response.ok) {
                    commentText.value = '';
                    // Recargar comentarios tras postear exitoso (o actualizar usando la respuesta)
                    await loadComments();
                } else if(response.status === 401 || response.status === 403) {
                    alert('Sesión expirada o no autorizada. Por favor, inicia sesión de nuevo.');
                } else {
                    alert('Error al publicar comentario');
                }
            } catch (error) {
                console.error(error);
                alert('Error de conectividad');
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Publicar Comentario';
            }
        });
    }

    // ----- Valoraciones (Estrellas Interactivas) -----
    if (isAuthenticated) {
        const starsContainer = document.getElementById('interactiveStarsContainer');
        const stars = document.querySelectorAll('.rate-star');
        const rateMessage = document.getElementById('rateMessage');

        if (starsContainer) {
            let currentRating = 0;

            // Hover effects
            stars.forEach(star => {
                star.addEventListener('mouseover', function() {
                    const val = parseInt(this.getAttribute('data-value'));
                    stars.forEach(s => {
                        if (parseInt(s.getAttribute('data-value')) <= val) { s.classList.add('hovered'); }
                        else { s.classList.remove('hovered'); }
                    });
                });

                star.addEventListener('mouseout', function() {
                    stars.forEach(s => s.classList.remove('hovered'));
                });

                // Click to rate
                star.addEventListener('click', async function() {
                    const val = parseInt(this.getAttribute('data-value'));
                    currentRating = val;
                    
                    // Visual set active
                    stars.forEach(s => {
                        if (parseInt(s.getAttribute('data-value')) <= val) { s.classList.add('active'); }
                        else { s.classList.remove('active'); }
                    });

                    // Api call
                    rateMessage.textContent = 'Enviando valoración...';
                    rateMessage.className = 'rate-message';

                    try {
                        const response = await fetch(`/api/recipes/${recipeId}/valoraciones`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ puntaje: val })
                        });

                        if (response.ok) {
                            rateMessage.textContent = '¡Valoración enviada! Gracias.';
                            rateMessage.className = 'rate-message success';
                        } else if (response.status === 403) {
                            rateMessage.textContent = 'No puedes valorar tu propia receta.';
                            rateMessage.className = 'rate-message error';
                        } else {
                            rateMessage.textContent = 'Ocurrió un error al enviar valoración.';
                            rateMessage.className = 'rate-message error';
                        }
                    } catch (error) {
                        console.error("Error valorando", error);
                        rateMessage.textContent = 'Error de red.';
                        rateMessage.className = 'rate-message error';
                    }
                });
            });
        }
    }

    // Load comentarios on Init
    loadComments();

    // Logout Helper
    const logoutBtn = document.getElementById('logoutBtn');
    if(logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            if (confirm('¿Cerrar sesión?')) {
                const response = await fetch('/api/logout', { method: 'POST' });
                if(response.ok) window.location.href = '/login';
            }
        });
    }
});
