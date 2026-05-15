package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    List<Usuario> findByTipo(TipoUsuario tipo);
    List<Usuario> findByAsiloId(Long asiloId);
}
