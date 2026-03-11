package com.senai.monsai.application.service;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsiloRepository asiloRepository;

    public Usuario criarUsuario(UsuarioCreateDTO dto) {
        Asilo asilo = asiloRepository.findById(dto.asiloId())
                .orElseThrow(() -> new RuntimeException("Asilo não encontrado"));

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.nome());
        novoUsuario.setEmail(dto.email());
        novoUsuario.setSenha(passwordEncoder.encode(dto.senha())); // Sempre encripte!
        novoUsuario.setTipo(dto.tipoUsuario()); // Assumindo que você tem um Enum
        novoUsuario.setAsilo(asilo);

        return usuarioRepository.save(novoUsuario);
    }

}
