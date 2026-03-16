package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Pulseira;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.IdosoNaoEncontradoException;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.PulseiraRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdosoService {

    private final IdosoRepository idosoRepository;
    private final PulseiraRepository pulseiraRepository;
    private final UsuarioRepository usuarioRepository;

    public Idoso criarIdoso(IdosoCreateDTO dto) {
        String emailGestorLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario gestor = usuarioRepository.findByEmail(emailGestorLogado).orElseThrow();
        Asilo asiloDoGestor = gestor.getAsilo();

        if (asiloDoGestor == null) {
            throw new RegraNegocioException("Este usuário não está vinculado a nenhum asilo.");
        }
        if (idosoRepository.existsByCpf(dto.getCpf())) {
            throw new RecursoDuplicadoException("Já existe um idoso cadastrado com este CPF.");
        }

        Pulseira pulseira = new Pulseira();
        pulseira.setSerial(dto.getSerialPulseira());
        pulseira = pulseiraRepository.save(pulseira);

        Idoso idoso = new Idoso();
        idoso.setNome(dto.getNome());
        idoso.setCpf(dto.getCpf());
        idoso.setEmail(dto.getEmail());
        idoso.setPulseira(pulseira);
        idoso.setAsilo(asiloDoGestor);

        return idosoRepository.save(idoso);
    }
    public List<Idoso> listarTodos() {
        return idosoRepository.findAll();
    }
    public void inativarIdoso(Long idIdoso) {
        Idoso idoso = idosoRepository.findById(idIdoso)
                .orElseThrow(IdosoNaoEncontradoException::new);
        idoso.setAtivo(false);
        if (idoso.getPulseira() != null) {
            idoso.setPulseira(null);
        }
        List<Usuario> usuariosQueCuidavam = idoso.getUsuarios();
        for (Usuario usuario : usuariosQueCuidavam) {
            usuario.getIdosos().remove(idoso);
            usuarioRepository.save(usuario);
        }
        idosoRepository.save(idoso);
    }

}
