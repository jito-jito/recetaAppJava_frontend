// JavaScript para la página de login

async function handleLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    
    // Validar campos
    if (!username || !password) {
        alert('Por favor ingresa usuario y contraseña');
        return;
    }

    // Deshabilitar botón durante la petición
    const loginBtn = document.querySelector('.login-btn');
    const originalText = loginBtn.textContent;
    loginBtn.disabled = true;
    loginBtn.textContent = 'Iniciando sesión...';
    
    try {
        // Realizar petición al endpoint proxy del frontend
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        
        const result = await response.json();
        
        if (result.success) {
            alert('¡Login exitoso!');
            // Redireccionar usando la URL proporcionada por el backend
            window.location.href = result.redirectUrl || '/detalle';
        } else {
            alert('Error en login: ' + result.message);
        }
        
    } catch (error) {
        console.error('Error de conexión:', error);
        alert('Error de conexión: ' + error.message);
    } finally {
        // Restaurar botón
        loginBtn.disabled = false;
        loginBtn.textContent = originalText;
    }
}

// Event listeners - ejecutar cuando se carga el DOM
document.addEventListener('DOMContentLoaded', function() {
    // Botón de login
    const loginBtn = document.getElementById('loginBtn');
    if (loginBtn) {
        loginBtn.addEventListener('click', handleLogin);
    }

    // Permitir login con Enter
    const inputs = document.querySelectorAll('input');
    inputs.forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                handleLogin();
            }
        });
    });
});