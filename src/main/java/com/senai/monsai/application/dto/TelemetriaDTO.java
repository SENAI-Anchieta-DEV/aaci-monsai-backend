package com.senai.monsai.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senai.monsai.domain.enums.StatusDispositivo;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/*
    DTO Mestre que representa o contrato JSON da AACI-126.
    Agrupa todas as informações da telemetria em um único arquivo.
 */
public record TelemetriaDTO(
        @NotNull(message = "idoso_id é obrigatório")
        @JsonProperty("idoso_id") Long idosoId,

        @NotBlank(message = "pulseira_id é obrigatório")
        @JsonProperty("pulseira_id") String pulseiraId,

        @NotBlank(message = "data_hora é obrigatória")
        @JsonProperty("data_hora") String dataHora,

        @NotNull(message = "sinal_vital é obrigatório")
        @JsonProperty("sinal_vital") SinalVitalDTO sinalVital,

        @NotNull(message = "localizacao é obrigatória")
        @JsonProperty("localizacao") LocalizacaoDTO localizacao,

        @NotNull(message = "status_do_dispositivo é obrigatório")
        @JsonProperty("status_do_dispositivo") StatusDispositivoDTO statusDoDispositivo
) {

    // --- SUB-RECORDS ---

    public record SinalVitalDTO(
            @NotBlank(message = "sinal_vital_id é obrigatório")
            @JsonProperty("sinal_vital_id") String sinalVitalId,

            @Min(value = 0, message = "Frequência cardíaca deve ser positiva")
            @JsonProperty("frequencia_cardiaca_bpm") Integer frequenciaCardiacaBpm,

            @DecimalMin(value = "10.0", message = "Temperatura abaixo do limite crítico")
            @JsonProperty("temperatura_c") Double temperaturaC,

            @NotNull @JsonProperty("movimento") MovimentoDTO movimento
    ) {}

    public record MovimentoDTO(
            @NotBlank
            @JsonProperty("aceleracao") AceleracaoDTO aceleracao,

            @JsonProperty("queda_detectada") Boolean quedaDetectada
    ) {}

    public record AceleracaoDTO(
            @NotNull(message= "Necessário adicionar a coordenada x")
            @JsonProperty("x") Double x,

            @NotNull(message= "Necessário adicionar a coordenada y")
            @JsonProperty("y") Double y,

            @NotNull(message= "Necessário adicionar a coordenada z")
            @JsonProperty("z") Double z
    ) {}

    public record LocalizacaoDTO(
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("precisao_metro") Double precisaoMetro
    ) {}

    public record StatusDispositivoDTO(
            @NotBlank(message = "status_id é obrigatório")
            @JsonProperty("status_id") String statusId,


            @JsonProperty("ultimo_contato") String ultimoContato,

            @Min(0) @Max(100)
            @JsonProperty("nivel_bateria") Integer nivelBateria,

            @NotBlank
            @JsonProperty("status_pulseira") StatusDispositivo statusDispositivo
    ) {}
}