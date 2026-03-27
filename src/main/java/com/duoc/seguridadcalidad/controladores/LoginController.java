package com.duoc.seguridadcalidad.controladores;

import com.duoc.seguridadcalidad.modelos.Usuario;
import com.duoc.seguridadcalidad.JWTAuthenticationConfig;
import com.duoc.seguridadcalidad.MyUserDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    JWTAuthenticationConfig jwtAuthenticationConfig;

    @Autowired
    private MyUserDetailsService userDetailsService;

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("login")
    public String login(@RequestBody LoginRequest loginRequest) {

        logger.info("Recibida solicitud de login para usuario: {}", loginRequest.getUsername());

        /**
         * En el ejemplo no se realiza la correcta validación del usuario
         * En producción deberías usar BCrypt para comparar passwords
         */

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        logger.info("Usuario encontrado en la base de datos: {}", userDetails.getUsername());

        if (!userDetails.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Invalid login");
        }

        String token = jwtAuthenticationConfig.getJWTToken(loginRequest.getUsername());

        return token;

    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getPassword() {
            return password;
        }
        public void setPassword(String password) {
            this.password = password;
        }
    }

}