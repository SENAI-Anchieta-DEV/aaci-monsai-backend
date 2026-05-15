package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.dto.IdosoUpdateDTO;
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
    private final AsiloRepository asiloRepository;

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
        if (idosoRepository.existsByCpf(dto.cpf())) {
            throw new RecursoDuplicadoException("Já existe um idoso cadastrado com este CPF.");
        }

        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setSerial(dto.serialDispositivo());

        Idoso idoso = new Idoso();
        idoso.setNome(dto.nome());
        idoso.setCpf(dto.cpf());
        idoso.setEmail(dto.email());
        idoso.setAsilo(asiloDestino);
        idoso.setAtivo(true);
        idoso.setDispositivo(dispositivo);
        dispositivo.setIdoso(idoso);
        return idosoRepository.save(idoso);
    }
        public List<Idoso> listarTodos() {
            String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado."));

            if (usuarioLogado.getAsilo() == null) {
                return idosoRepository.findAll();
            }

            return idosoRepository.findByAsiloId(usuarioLogado.getAsilo().getId());
        }

    public Idoso buscarPorSerial(String serial) {
        return idosoRepository.findByDispositivoSerial(serial)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Idoso com a pulseira " + serial + " não encontrado."));
    }

        public Idoso atualizarIdoso(Long idIdoso, IdosoUpdateDTO dto) {
            String emailUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
            Usuario usuarioLogado = usuarioRepository.findByEmail(emailUsuarioLogado)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário logado não encontrado."));

            Idoso idoso = idosoRepository.findById(idIdoso)
                    .orElseThrow(IdosoNaoEncontradoException::new);

            if (!idoso.isAtivo()) {
                throw new RegraNegocioException("Não é possível editar os dados de um idoso inativo.");
            }

            // Validação de Segurança: O usuário logado NÃO PODE editar um idoso de outro asilo
            if (usuarioLogado.getAsilo() != null && !usuarioLogado.getAsilo().getId().equals(idoso.getAsilo().getId())) {
                throw new RegraNegocioException("Violação de segurança: Você não tem permissão para editar um idoso de outro asilo.");
            }

            // Se o CPF mudou na edição, verifica se o novo CPF já existe no banco
            if (!idoso.getCpf().equals(dto.cpf()) && idosoRepository.existsByCpf(dto.cpf())) {
                throw new RecursoDuplicadoException("Já existe outro idoso cadastrado com este CPF.");
            }


            idoso.setNome(dto.nome());
            idoso.setCpf(dto.cpf());
            idoso.setEmail(dto.email());

            return idosoRepository.save(idoso);
        }

    public void inativarIdoso(Long idIdoso) {
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
        if (usuarioLogado.getAsilo() != null && !usuarioLogado.getAsilo().getId().equals(idoso.getAsilo().getId())) {
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