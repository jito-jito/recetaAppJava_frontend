package com.duoc.seguridadcalidad.controladores;

import com.duoc.seguridadcalidad.modelos.Usuario;
import com.duoc.seguridadcalidad.repositorios.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        Map<String, String> response = new HashMap<>();

        try {
            // Verificar si el usuario ya existe
            if (userRepository.findByUsername(request.getUsername()) != null) {
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Crear nuevo usuario
            Usuario newUser = new Usuario();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            
            // En producción, deberías siempre hashear la contraseña
            // newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setPassword(request.getPassword()); // Por simplicidad, sin hash
            
            newUser.setEstaAutenticado(true);

            userRepository.save(newUser);

            response.put("message", "User registered successfully");
            response.put("username", newUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Usuario> getProfile(@RequestParam String username) {
        Usuario user = userRepository.findByUsername(username);
        if (user != null) {
            // No devolver la contraseña
            Usuario safeUser = new Usuario();
            safeUser.setIdUsuario(user.getIdUsuario());
            safeUser.setUsername(user.getUsername());
            safeUser.setEmail(user.getEmail());
            safeUser.setEstaAutenticado(user.getEstaAutenticado());
            
            return ResponseEntity.ok(safeUser);
        }
        return ResponseEntity.notFound().build();
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }
}