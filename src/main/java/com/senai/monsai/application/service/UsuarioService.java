package com.senai.monsai.application.service;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario criarUsuario(UsuarioCreateDTO dto) {
        if(usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("E-mail já está em uso!");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.nome());
        novoUsuario.setEmail(dto.email());
        novoUsuario.setTipo(dto.tipo());
        novoUsuario.setSenha(passwordEncoder.encode(dto.senha()));
        return usuarioRepository.save(novoUsuario);
    }
}
