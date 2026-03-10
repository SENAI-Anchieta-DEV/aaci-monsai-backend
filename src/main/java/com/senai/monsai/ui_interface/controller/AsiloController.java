package com.senai.monsai.ui_interface.controller;
import com.senai.monsai.application.dto.AsiloCreateDTO;
import com.senai.monsai.application.service.AsiloService;
import com.senai.monsai.domain.entity.Asilo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asilos")
@RequiredArgsConstructor
@Tag(name = "Asilos", description = "Endpoints para gestão de Asilos")
public class AsiloController {

    private final AsiloService asiloService;

    @PostMapping
    @Operation(summary = "Criar um novo Asilo", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Asilo adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Apenas Admins podem criar)"),
            @ApiResponse(responseCode = "409", description = "Conflito: Cnpj já cadastrado")
    })
    public ResponseEntity<Asilo> criarAsilo(@RequestBody AsiloCreateDTO dto) {
        Asilo novoAsilo = asiloService.criarAsilo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAsilo);
    }
}