package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.TelemetriaDTO;
import com.senai.monsai.application.service.TelemetriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telemetria")
public class TelemetriaController {
    @Autowired
    private TelemetriaService telemetriaService;

    @PostMapping
    @Operation(summary = "Registra nova telemetria", description = "Recebe os dados brutos da pulseira IoT e armazena no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Telemetria processada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação: payload malformado ou campos ausentes"),
            @ApiResponse(responseCode = "404", description = "Pulseira não cadastrada")
    })
    public ResponseEntity<String> receberTelemetria(@Valid @RequestBody TelemetriaDTO dto) {
        // Chamada do service que você acabou de criar
        telemetriaService.processarTelemetria(dto);

        return ResponseEntity.ok("Dados de telemetria recebidos com sucesso!");
    }
}
