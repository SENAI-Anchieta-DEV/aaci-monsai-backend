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
        Asilo asilo = asiloRepository.findById(dto.getAsiloId())
                .orElseThrow(() -> new RuntimeException("Asilo não encontrado"));

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.getNome());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setTipo(dto.getTipoUsuario());
        novoUsuario.setAsilo(asilo);

        return usuarioRepository.save(novoUsuario);
    }

}
