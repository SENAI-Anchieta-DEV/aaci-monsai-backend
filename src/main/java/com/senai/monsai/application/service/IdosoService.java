package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.IdosoNaoEncontradoException;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.PulseiraRepository; // Troque para DispositivoRepository se você renomeou o arquivo
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
    private final AsiloRepository asiloRepository; // <-- Injetando o AsiloRepository

    public Idoso criarIdoso(IdosoCreateDTO dto) {
        String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado no sistema."));

        Asilo asiloDestino;

        if (dto.asiloId() != null) {
            asiloDestino = asiloRepository.findById(dto.asiloId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("O Asilo informado no asiloId não existe."));

            if (usuarioLogado.getAsilo() != null && !usuarioLogado.getAsilo().getId().equals(asiloDestino.getId())) {
                throw new RegraNegocioException("Violação de segurança: Você não pode cadastrar um idoso em outro asilo.");
            }
        } else {
            asiloDestino = usuarioLogado.getAsilo();
        }
        if (asiloDestino == null) {
            throw new RegraNegocioException("É obrigatório informar o asiloId no corpo da requisição para este usuário (Super Admin).");
        }

        // REGRA DE NEGÓCIO: Sem duplicidade de Idosos
        if (idosoRepository.existsByCpf(dto.cpf())) {
            throw new RecursoDuplicadoException("Já existe um idoso cadastrado com este CPF.");
        }

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setSerial(dto.serialDispositivo());
        dispositivo = pulseiraRepository.save(dispositivo);

        Idoso idoso = new Idoso();
        idoso.setNome(dto.nome());
        idoso.setCpf(dto.cpf());
        idoso.setEmail(dto.email());
        idoso.setDispositivo(dispositivo);
        idoso.setAsilo(asiloDestino);
        idoso.setAtivo(true);

        dispositivo.setIdoso(idoso);

        return idosoRepository.save(idoso);
    }
    public List<Idoso> listarTodos() {
        return idosoRepository.findAll();
    }

    public void inativarIdoso(Long idIdoso) {
        // Descobre quem está tentando inativar
        String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado."));

        Idoso idoso = idosoRepository.findById(idIdoso)
                .orElseThrow(IdosoNaoEncontradoException::new);

        // 3. REGRA DE NEGÓCIO: Evitando Zumbis
        if (!idoso.isAtivo()) {
            throw new RegraNegocioException("Este idoso já encontra-se inativo no sistema.");
        }

        // 4. REGRA DE NEGÓCIO: Fronteira do Asilo (Cross-Tenant Leak)
        // O usuário logado NÃO PODE inativar um idoso de outro asilo!
        if (!usuarioLogado.getAsilo().getId().equals(idoso.getAsilo().getId())) {
            throw new RegraNegocioException("Violação de segurança: Você não tem permissão para alterar dados de um idoso de outro asilo.");
        }

        idoso.setAtivo(false);

        if (idoso.getDispositivo() != null) {
            idoso.setDispositivo(null);
        }

        List<Usuario> usuariosQueCuidavam = idoso.getUsuarios();
        for (Usuario usuario : usuariosQueCuidavam) {
            usuario.getIdosos().remove(idoso);
            usuarioRepository.save(usuario);
        }
        idosoRepository.save(idoso);
    }
}