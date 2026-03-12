package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.AtualizarSenhaDTO;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.exception.AsiloNaoEncontradoException;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
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
        // 1. Busca o asilo (Lança Erro 404 se não achar)
        Asilo asilo = asiloRepository.findById(dto.asiloId())
                .orElseThrow(() -> new AsiloNaoEncontradoException(dto.asiloId()));

        // 2. Regra de Negócio: Bloqueia E-mail duplicado (Lança Erro 409)
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RecursoDuplicadoException("Já existe um usuário cadastrado com este e-mail.");
        }

        // 3. Regra de Negócio: Bloqueia CPF duplicado (Lança Erro 409)
        if (usuarioRepository.existsByCpf(dto.cpf())) {
            throw new RecursoDuplicadoException("Já existe um usuário cadastrado com este CPF.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.nome());
        novoUsuario.setEmail(dto.email());
        novoUsuario.setSenha(passwordEncoder.encode(dto.senha()));
        novoUsuario.setCpf(dto.cpf());
        novoUsuario.setTipo(TipoUsuario.valueOf(dto.tipoUsuario()));
        novoUsuario.setAsilo(asilo);

        return usuarioRepository.save(novoUsuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void atualizarSenha(Long idUsuario, AtualizarSenhaDTO dto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com o ID fornecido."));

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    public void vincularIdoso(Long idUsuario, Long idIdoso) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        Idoso idoso = idosoRepository.findById(idIdoso)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Idoso não encontrado."));

        // Se o idoso ainda não estiver na lista desse usuário, a gente adiciona
        if (!usuario.getIdosos().contains(idoso)) {
            usuario.getIdosos().add(idoso);
            usuarioRepository.save(usuario);
        } else {
            // Opcional: Avisar que já está vinculado para não dar falso positivo pro Front
            throw new RegraNegocioException("Este idoso já está vinculado a este usuário.");
        }
    }

    public void desvincularIdoso(Long idUsuario, Long idIdoso) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        Idoso idoso = idosoRepository.findById(idIdoso)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Idoso não encontrado."));

        if (usuario.getIdosos().contains(idoso)) {
            usuario.getIdosos().remove(idoso);
            usuarioRepository.save(usuario);
        } else {
            throw new RegraNegocioException("Este idoso não está vinculado a este usuário.");
        }
    }

    public void inativarUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Este usuário já encontra-se inativo no sistema.");
        }

        usuario.setAtivo(false);
        usuario.getIdosos().clear();
        usuarioRepository.save(usuario);
    }
}