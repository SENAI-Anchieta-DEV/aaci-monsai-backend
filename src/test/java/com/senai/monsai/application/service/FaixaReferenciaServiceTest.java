package com.senai.monsai.application.service;

import com.senai.monsai.domain.entity.AuditoriaFaixa;
import com.senai.monsai.domain.entity.FaixaReferencia;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.AuditoriaFaixaRepository;
import com.senai.monsai.domain.repository.FaixaReferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaixaReferenciaServiceTest {

    @Mock
    private FaixaReferenciaRepository repository;

    @Mock
    private AuditoriaFaixaRepository auditoriaRepository;

    @InjectMocks
    private FaixaReferenciaService faixaReferenciaService;

    private FaixaReferencia faixaAntiga;
    private FaixaReferencia novaFaixa;
    private Usuario gestor;

    @BeforeEach
    void setUp() {
        Idoso idoso = new Idoso();
        idoso.setId(10L);

        gestor = new Usuario();
        gestor.setNome("Gestor Teste");

        faixaAntiga = new FaixaReferencia();
        faixaAntiga.setId(1L);
        faixaAntiga.setIdoso(idoso);
        faixaAntiga.setMinBpm(60);
        faixaAntiga.setMaxBpm(100);
        faixaAntiga.setMinTemp(36.0);
        faixaAntiga.setMaxTemp(37.5);

        novaFaixa = new FaixaReferencia();
        novaFaixa.setId(1L);
        novaFaixa.setIdoso(idoso);
        novaFaixa.setMinBpm(65); // Alterado
        novaFaixa.setMaxBpm(100); // Igual
        novaFaixa.setMinTemp(36.0); // Igual
        novaFaixa.setMaxTemp(38.0); // Alterado
    }

    // =========================================================
    // EDGE CASE 1: Deve atualizar a faixa e gerar logs de auditoria
    // =========================================================
    @Test
    @DisplayName("AACI-114: Atualização de faixa gerando logs de auditoria")
    void deveAtualizarFaixaEGerarLogs() {
        when(repository.findById(1L)).thenReturn(Optional.of(faixaAntiga));

        assertDoesNotThrow(() -> faixaReferenciaService.atualizarFaixa(novaFaixa, gestor));

        // BPM Min e Temp Max mudam, o auditoriaRepository é chamado 2 vezes
        verify(auditoriaRepository, times(2)).save(any(AuditoriaFaixa.class));
        verify(repository, times(1)).save(novaFaixa);
    }

    // =========================================================
    // EDGE CASE 2: Não deve gerar logs se nenhum valor for alterado
    // =========================================================
    @Test
    @DisplayName("AACI-114: Atualização de faixa sem alterações não gera logs")
    void naoDeveGerarLogSeNaoHouverAlteracao() {
        // Nova faixa idêntica à antiga
        FaixaReferencia faixaSemMudanca = new FaixaReferencia();
        faixaSemMudanca.setId(1L);
        faixaSemMudanca.setIdoso(faixaAntiga.getIdoso());
        faixaSemMudanca.setMinBpm(60);
        faixaSemMudanca.setMaxBpm(100);
        faixaSemMudanca.setMinTemp(36.0);
        faixaSemMudanca.setMaxTemp(37.5);

        when(repository.findById(1L)).thenReturn(Optional.of(faixaAntiga));

        assertDoesNotThrow(() -> faixaReferenciaService.atualizarFaixa(faixaSemMudanca, gestor));

        // Nenhuma alteração = Nenhuma chamada no repositório de auditoria
        verify(auditoriaRepository, never()).save(any());
        verify(repository, times(1)).save(faixaSemMudanca);
    }

    // =========================================================
    // EDGE CASE 3: Deve lançar Exceção se a Faixa não existir no banco
    // =========================================================
    @Test
    @DisplayName("AACI-114: Falha ao tentar atualizar faixa inexistente")
    void deveLancarExcecaoQuandoFaixaNaoExistir() {
        String mensagemEsperada = "Faixa de referência ID 1 não existe no banco.";

        when(repository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(
                RecursoNaoEncontradoException.class,
                () -> faixaReferenciaService.atualizarFaixa(novaFaixa, gestor)
        );

        assertEquals(mensagemEsperada, exception.getMessage());
        verify(auditoriaRepository, never()).save(any());
        verify(repository, never()).save(any());
    }
}