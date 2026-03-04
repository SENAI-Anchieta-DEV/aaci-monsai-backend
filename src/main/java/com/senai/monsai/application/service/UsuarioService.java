package com.senai.monsai.application.service;
import com.senai.monsai.application.dto.AtualizarSenhaDTO;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.AsiloNaoEncontradoException;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final IdosoRepository idosoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AsiloRepository asiloRepository;

    public Usuario criarUsuario(UsuarioCreateDTO dto) {
        Asilo asilo = asiloRepository.findById(dto.getAsiloId())
                .orElseThrow(() -> new AsiloNaoEncontradoException(dto.getAsiloId()));

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.getNome());
        novoUsuario.setEmail(dto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setTipo(dto.getTipoUsuario());
        novoUsuario.setAsilo(asilo);

        return usuarioRepository.save(novoUsuario);
    }
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
    public void atualizarSenha(Long idUsuario, AtualizarSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(RecursoNaoEncontradoException::new);
        usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
    }
    public void vincularIdoso(Long idUsuario, Long idIdoso) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(RecursoNaoEncontradoException::new);

        Idoso idoso = idosoRepository.findById(idIdoso)
                .orElseThrow(RecursoNaoEncontradoException::new);

        // Se o idoso ainda não estiver na lista desse usuário, a gente adiciona
        if (!usuario.getIdosos().contains(idoso)) {
            usuario.getIdosos().add(idoso);
            usuarioRepository.save(usuario);
        }
    }

    public void desvincularIdoso(Long idUsuario, Long idIdoso) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(RecursoNaoEncontradoException::new);

        Idoso idoso = idosoRepository.findById(idIdoso)
                .orElseThrow(RecursoNaoEncontradoException::new);
        if (usuario.getIdosos().contains(idoso)) {
            usuario.getIdosos().remove(idoso);
            usuarioRepository.save(usuario);
        }
    }
    public void inativarUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(RecursoNaoEncontradoException::new);
        usuario.setAtivo(false);
        usuario.getIdosos().clear();
        usuarioRepository.save(usuario);
    }

}
