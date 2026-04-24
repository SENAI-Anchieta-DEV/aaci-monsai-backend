    package com.senai.monsai.application.service;

    import com.senai.monsai.application.dto.TelemetriaDTO;
    import com.senai.monsai.domain.entity.Dispositivo;
    import com.senai.monsai.domain.entity.Idoso;
    import com.senai.monsai.domain.enums.StatusDispositivo;
    import com.senai.monsai.domain.repository.AsiloRepository;
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

    import static org.junit.jupiter.api.Assertions.*;
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
        void setUp(){
            Idoso idoso = new Idoso();
            idoso.setId(1L);

            dispositivoMock = new Dispositivo();
            dispositivoMock.setSerial("MON-313");
            dispositivoMock.setIdoso(idoso);

            var aceleracao = new TelemetriaDTO.AceleracaoDTO(0.0, 0.0, 9.8);
            var movimento = new TelemetriaDTO.MovimentoDTO(aceleracao, false);
            var sinalVital = new TelemetriaDTO.SinalVitalDTO("SV-313", 80, 36.4, movimento);
            var localizacao = new TelemetriaDTO.LocalizacaoDTO(-23.55, -46.63, 5.0);
            var statusDisp = new TelemetriaDTO.StatusDispositivoDTO("ST-313", "2026-03-11T10:00:00", 98, StatusDispositivo.ATIVO);

            telemetriaValida = new TelemetriaDTO(1L, "MON-313", "2026-03-11T10:00:00", sinalVital, localizacao, statusDisp);
        }

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
    }