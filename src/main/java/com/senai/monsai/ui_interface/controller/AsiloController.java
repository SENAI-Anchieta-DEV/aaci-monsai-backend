package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AsiloCreateDTO;
import com.senai.monsai.application.service.AsiloService;
import com.senai.monsai.domain.entity.Asilo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asilos")
@RequiredArgsConstructor
@Tag(name = "Asilos", description = "Endpoints de uso exclusivo do Super Admin para gerenciar as instituições clientes")
public class AsiloController {

    private final AsiloService asiloService;

    // ==========================================
    // 1. CRIAR ASILO
    // ==========================================
    @PostMapping
    @Operation(summary = "Cadastrar uma nova instituição (Asilo) no sistema", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Asilo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Apenas Super Admin)"),
            @ApiResponse(responseCode = "409", description = "Conflito: CNPJ já cadastrado no sistema")
    })
    public ResponseEntity<Asilo> criarAsilo(@Valid @RequestBody AsiloCreateDTO dto) {
        Asilo novoAsilo = asiloService.criarAsilo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAsilo);
    }

    // ==========================================
    // 2. LISTAR ASILOS
    // ==========================================
    @GetMapping
    @Operation(summary = "Listar todas as instituições clientes cadastradas", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Asilo>> listarAsilos() {
        return ResponseEntity.ok(asiloService.listarTodos());
    }

    // ==========================================
    // 3. ATUALIZAR ASILO
    // ==========================================
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados cadastrais de um Asilo", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asilo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou tentativa de editar asilo inativo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Asilo não encontrado")
    })
    public ResponseEntity<Asilo> atualizarAsilo(@PathVariable Long id, @Valid @RequestBody AsiloCreateDTO dto) {
        return ResponseEntity.ok(asiloService.atualizarAsilo(id, dto));
    }

    // ==========================================
    // 4. INATIVAR ASILO (BLOQUEIO)
    // ==========================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Bloquear/Inativar um Asilo cliente", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Asilo inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Asilo já encontra-se inativo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Asilo não encontrado")
    })
    public ResponseEntity<Void> inativarAsilo(@PathVariable Long id) {
        asiloService.inativarAsilo(id);
        return ResponseEntity.noContent().build();
    }
    // ==========================================
    // 5. BUSCAR ENDEREÇO (PÚBLICO)
    // ==========================================
    @GetMapping("/{id}/endereco")
    @Operation(summary = "Buscar o endereço de um asilo (Acesso Público)")
    @PreAuthorize("permitAll()")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Endereço retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Asilo não encontrado")
    })
    public ResponseEntity<String> buscarEndereco(@PathVariable Long id) {
        return ResponseEntity.ok(asiloService.buscarEndereco(id));
    }
}