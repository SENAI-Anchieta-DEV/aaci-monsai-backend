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
        if (!antiga.getMinBpm().equals(novaFaixa.getMinBpm())) {
            salvarLog(idosoId, nomeGestor, "BPM Mínimo",
                    antiga.getMinBpm().toString(), novaFaixa.getMinBpm().toString());
        }

        // Máximo BPM
        if (!antiga.getMaxBpm().equals(novaFaixa.getMaxBpm())) {
            salvarLog(idosoId, nomeGestor, "BPM Máximo",
                    antiga.getMaxBpm().toString(), novaFaixa.getMaxBpm().toString());
        }

        // --- AUDITORIA DE TEMPERATURA ---

        // Mínima Temperatura
        if (!antiga.getMinTemp().equals(novaFaixa.getMinTemp())) {
            salvarLog(idosoId, nomeGestor, "Temperatura Mínima",
                    antiga.getMinTemp().toString(), novaFaixa.getMinTemp().toString());
        }

        // Máxima Temperatura
        if (!antiga.getMaxTemp().equals(novaFaixa.getMaxTemp())) {
            salvarLog(idosoId, nomeGestor, "Temperatura Máxima",
                    antiga.getMaxTemp().toString(), novaFaixa.getMaxTemp().toString());
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
