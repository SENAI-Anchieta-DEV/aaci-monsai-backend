package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.service.IdosoService;
import com.senai.monsai.domain.entity.Idoso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // <-- Import do @Valid adicionado
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/idosos")
@RequiredArgsConstructor
@Tag(name = "Idosos", description = "Gestão dos idosos residentes no asilo")
public class IdosoController {

    private final IdosoService idosoService;

    // ==========================================
    // 1. CRIAR IDOSO
    // ==========================================
    @PostMapping
    @Operation(summary = "Cadastrar um novo Idoso", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Idoso adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Apenas Gestores podem criar)"),
            @ApiResponse(responseCode = "409", description = "Conflito: CPF já cadastrado")
    })
    public ResponseEntity<Idoso> criarIdoso(@Valid @RequestBody IdosoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(idosoService.criarIdoso(dto));
    }

    // ==========================================
    // 2. LISTAR IDOSOS
    // ==========================================
    @GetMapping
    @Operation(summary = "Listar todos os idosos", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ENFERMEIRO', 'CUIDADOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Idoso>> listarIdosos() {
        return ResponseEntity.ok(idosoService.listarTodos());
    }

    // ==========================================
    // 3. INATIVAR IDOSO
    // ==========================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar um idoso (Saída do asilo)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Idoso inativado com sucesso (Sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Idoso não encontrado pelo ID fornecido")
    })
    public ResponseEntity<Void> inativarIdoso(@PathVariable Long id) {
        idosoService.inativarIdoso(id);
        return ResponseEntity.noContent().build();
    }
}