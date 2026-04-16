package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.dto.IdosoUpdateDTO;
import com.senai.monsai.application.service.IdosoService;
import com.senai.monsai.domain.entity.Idoso;
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
@RequestMapping("/idosos")
@RequiredArgsConstructor
@Tag(name = "Idosos", description = "Gestão de Idosos, dados pessoais e vinculação com Dispositivos IoT")
public class IdosoController {

    private final IdosoService idosoService;

    // ==========================================
    // 1. CRIAR IDOSO
    // ==========================================
    @PostMapping
    @Operation(summary = "Criar um novo Idoso e vincular a um dispositivo", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Idoso criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio (Ex: asiloId obrigatório)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Conflito: CPF já cadastrado")
    })
    public ResponseEntity<Idoso> criarIdoso(@Valid @RequestBody IdosoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(idosoService.criarIdoso(dto));
    }

    // ==========================================
    // 2. LISTAR IDOSOS
    // ==========================================
    @GetMapping
    @Operation(summary = "Listar Idosos (Gestor/Cuidador vê apenas do seu asilo, SuperAdmin vê todos)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GESTOR', 'CUIDADOR')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Idoso>> listarIdosos() {
        return ResponseEntity.ok(idosoService.listarTodos());
    }

    // ==========================================
    // 3. BUSCAR IDOSO POR SERIAL DA PULSEIRA
    // ==========================================
    @GetMapping("/buscarPorSerial")
    @Operation(summary = "Busca idoso pelo serial da pulseira")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GESTOR', 'CUIDADOR')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Idoso encontrado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Idoso> buscarPorSerial(@RequestParam String serial) {
        return ResponseEntity.ok(idosoService.buscarPorSerial(serial));
    }

    // ==========================================
    // 4. ATUALIZAR IDOSO
    // ==========================================
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados pessoais de um Idoso", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GESTOR')")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Idoso atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Tentativa de editar idoso inativo ou de outro asilo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Idoso não encontrado")
    })
    public ResponseEntity<Idoso> atualizarIdoso(@PathVariable Long id, @Valid @RequestBody IdosoUpdateDTO dto) {
        return ResponseEntity.ok(idosoService.atualizarIdoso(id, dto));
    }

    // ==========================================
    // 5. INATIVAR IDOSO
    // ==========================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar um Idoso (Óbito, Transferência ou Fim de Contrato)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','GESTOR')")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Idoso inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Idoso já inativo ou pertence a outro asilo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Idoso não encontrado")
    })
    public ResponseEntity<Void> inativarIdoso(@PathVariable Long id) {
        idosoService.inativarIdoso(id);
        return ResponseEntity.noContent().build();
    }
}