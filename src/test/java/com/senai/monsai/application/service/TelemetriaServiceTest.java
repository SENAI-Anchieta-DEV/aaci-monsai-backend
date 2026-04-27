package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.FaixaReferencia;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.enums.StatusDispositivo;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.repository.DispositivoRepository;
import com.senai.monsai.domain.repository.FaixaReferenciaRepository;
import com.senai.monsai.domain.repository.MensagemMqttRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelemetriaServiceTest {

    @Mock
    private DispositivoRepository dispositivoRepository;

    @Mock
    private MensagemMqttRepository mensagemMqttRepository;

    @Mock
    private FaixaReferenciaRepository faixaReferenciaRepository;

    @InjectMocks
    private TelemetriaService telemetriaService;

    private TelemetriaDTO telemetriaValida;
    private Dispositivo dispositivoMock;

    @BeforeEach
    void setUp() {
        Idoso idoso = new Idoso();
        idoso.setId(1L);

        dispositivoMock = new Dispositivo();
        dispositivoMock.setSerial("MON-313");
        dispositivoMock.setIdoso(idoso);

        var aceleracao = new TelemetriaDTO.AceleracaoDTO(
                0.0,
                0.0,
                9.8
        );

        var movimento = new TelemetriaDTO.MovimentoDTO(
                aceleracao,
                false
        );

        var sinalVital = new TelemetriaDTO.SinalVitalDTO(
                "SV-313",
                80,
                36.4,
                movimento
        );

        var localizacao = new TelemetriaDTO.LocalizacaoDTO(
                -23.55,
                -46.63,
                5.0
        );

        var statusDisp = new TelemetriaDTO.StatusDispositivoDTO(
                "ST-313",
                "2026-03-11T10:00:00",
                98,
                StatusDispositivo.ATIVO
        );

        telemetriaValida = new TelemetriaDTO(
                1L,
                "MON-313",
                "2026-03-11T10:00:00",
                sinalVital,
                localizacao,
                statusDisp
        );
    }

    // =========================================================
    // EDGE CASE 1: Processamento da telemetria sucesso
    // =========================================================

    @Test
    @DisplayName("AACI-214: Deve processar a telemetria com sucesso")
    void processarTelemetria() {
        // GIVEN
        when(dispositivoRepository.findBySerial(anyString()))
                .thenReturn(Optional.of(dispositivoMock));

        when(faixaReferenciaRepository.findByIdosoId(anyLong()))
                .thenReturn(Optional.empty());

        // WHEN
        assertDoesNotThrow(() -> telemetriaService.processarTelemetria(telemetriaValida));

        // THEN
        verify(mensagemMqttRepository, times(1)).save(any());
    }

    // =========================================================
    // EDGE CASE 2: Serial de dispositivo inexistente
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve lançar exceção quando a serial de um dispositivo não existir")
    void falhaPorSerialInexistente() {
        // GIVEN: O banco não encontra nenhum dispositivo com o serial recebido
        when(dispositivoRepository.findBySerial(anyString()))
            .thenReturn(Optional.empty());

        // WHEN & THEN: O service deve rejeitar o processamento
        assertThrows(RecursoNaoEncontradoException.class,
                () ->  telemetriaService.processarTelemetria(telemetriaValida));

        // Nenhuma mensagem deve ser salva se o dispositivo não existe
        verify(mensagemMqttRepository, never()).save(any());
    }

    // =========================================================
    // EDGE CASE 2: FaixaReferencia presente — caminho alternativo
    // =========================================================

    @Test
    @DisplayName("AACI-112: Deve processar a Telemetria e utilizar a FaixaReferencia quando presente")
    void processarTelemetriaComFaixaReferencia() {
        // GIVEN: Dispositivo existe normalmente
        when(dispositivoRepository.findBySerial("MON-313"))
                .thenReturn(Optional.of(dispositivoMock));

        // Cria a faixa de referencia com os limites da entidade
        FaixaReferencia faixaMock = new FaixaReferencia();
        faixaMock.setMinBpm(60);
        faixaMock.setMaxBpm(100);
        faixaMock.setMinTemp(36.0);
        faixaMock.setMaxTemp(37.5);

        when(faixaReferenciaRepository.findByIdosoId(1L))
                .thenReturn(Optional.of(faixaMock));

        // WHEN & THEN: Sinais vitais dentro da faixa - não lança exception
        assertDoesNotThrow(() -> telemetriaService.processarTelemetria(telemetriaValida));

        // Mensagem deve salvar normalmente
        verify(mensagemMqttRepository, times(1)).save(any());
    }
}