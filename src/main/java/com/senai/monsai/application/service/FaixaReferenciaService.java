package com.senai.monsai.application.service;

import com.senai.monsai.domain.entity.AuditoriaFaixa;
import com.senai.monsai.domain.entity.FaixaReferencia;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.AuditoriaFaixaRepository;
import com.senai.monsai.domain.repository.FaixaReferenciaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class FaixaReferenciaService {

    @Autowired
    private FaixaReferenciaRepository repository;

    @Autowired
    private AuditoriaFaixaRepository auditoriaRepository;

    @Transactional
    public void atualizarFaixa(FaixaReferencia novaFaixa, Usuario gestor) {
        FaixaReferencia antiga = repository.findById(novaFaixa.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Faixa de referência ID " + novaFaixa.getId() + " não existe no banco."));

        Long idosoId = novaFaixa.getIdoso().getId();
        String nomeGestor = gestor.getNome();

        // --- AUDITORIA DE BATIMENTOS (BPM) ---

        // Mínimo BPM
        if (!Objects.equals(antiga.getMinBpm(), novaFaixa.getMinBpm())) {
            salvarLog(idosoId, nomeGestor, "BPM Mínimo",
                    String.valueOf(antiga.getMinBpm()), String.valueOf(novaFaixa.getMinBpm()));
        }

        // Máximo BPM
        if (!Objects.equals(antiga.getMaxBpm(), novaFaixa.getMaxBpm())) {
            salvarLog(idosoId, nomeGestor, "BPM Máximo",
                    String.valueOf(antiga.getMaxBpm()), String.valueOf(novaFaixa.getMaxBpm()));
        }

        // --- AUDITORIA DE TEMPERATURA ---

        // Mínima Temperatura
        if (!Objects.equals(antiga.getMinTemp(), novaFaixa.getMinTemp())) {
            salvarLog(idosoId, nomeGestor, "Temperatura Mínima",
                    String.valueOf(antiga.getMinTemp()), String.valueOf(novaFaixa.getMinTemp()));
        }

        // Máxima Temperatura
        if (!Objects.equals(antiga.getMaxTemp(), novaFaixa.getMaxTemp())) {
            salvarLog(idosoId, nomeGestor, "Temperatura Máxima",
                    String.valueOf(antiga.getMaxTemp()), String.valueOf(novaFaixa.getMaxTemp()));
        }

        // 2. Após registrar todas as mudanças no log, salvamos a nova versão no banco
        repository.save(novaFaixa);
    }

    private void salvarLog(Long idosoId, String nomeUser, String campo, String antes, String depois) {
        AuditoriaFaixa log = AuditoriaFaixa.builder()
                .idosoId(idosoId)
                .usuarioNome(nomeUser)
                .dataAlteracao(LocalDateTime.now())
                .campoAlterado(campo)
                .valorAntigo(antes)
                .valorNovo(depois)
                .build();
        auditoriaRepository.save(log);
    }
}
