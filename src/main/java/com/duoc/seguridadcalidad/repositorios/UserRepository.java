package com.duoc.seguridadcalidad.repositorios;

import org.springframework.data.repository.CrudRepository;

import com.duoc.seguridadcalidad.modelos.Usuario;


public interface UserRepository extends CrudRepository<Usuario, Integer> {

    Usuario findByUsername(String username);

}