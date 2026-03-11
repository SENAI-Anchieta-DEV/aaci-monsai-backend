package com.senai.monsai.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senai.monsai.domain.enums.StatusDispositivo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/*
    DTO Mestre que representa o contrato JSON da AACI-126.
    Agrupa todas as informações da telemetria em um único arquivo.
 */
@Schema(description = "Contrato de Telemetria da AACI-126")
public record TelemetriaDTO(
        @Schema(example = "1", description = "ID único do idoso associado à telemetria")
        @NotNull(message = "idoso_id é obrigatório")
        @JsonProperty("idoso_id") Long idosoId,

        @Schema(example = "550e8400-e29b-41d4-a716-446655440000", description = "UUID identificador do dispositivo IoT")
        @NotBlank(message = "pulseira_id é obrigatório")
        @JsonProperty("pulseira_id") String pulseiraId,

        @Schema(example = "2026-03-11T10:00:00", description = "Data e hora do registro da telemetria no formato ISO-8601")
        @NotBlank(message = "data_hora é obrigatória")
        @JsonProperty("data_hora") String dataHora,

        @Schema(description = "Objeto contendo os dados vitais coletados (batimentos, temperatura, etc)")
        @NotNull(message = "sinal_vital é obrigatório")
        @JsonProperty("sinal_vital") SinalVitalDTO sinalVital,

        @Schema(description = "Coordenadas geográficas atuais do dispositivo")
        @NotNull(message = "localizacao é obrigatória")
        @JsonProperty("localizacao") LocalizacaoDTO localizacao,

        @Schema(description = "Status atual do hardware do dispositivo (bateria, conectividade)")
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