package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.service.IdosoService;
import com.senai.monsai.domain.entity.Idoso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/idosos")
@RequiredArgsConstructor
public class IdosoController {

    private final IdosoService idosoService;

    // ==========================================
    // 1. CRIAR IDOSO
    // ==========================================
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Idoso> criarIdoso(@RequestBody IdosoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(idosoService.criarIdoso(dto));
    }
    // ==========================================
    // 2. LISTAR IDOSO
    // ==========================================
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR', 'ENFERMEIRO', 'CUIDADOR')")
    public ResponseEntity<List<Idoso>> listarIdosos() {
        return ResponseEntity.ok(idosoService.listarTodos());
    }
    // ==========================================
    // 3. INATIVAR IDOSO
    // ==========================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar um idoso (Saída do asilo)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Void> inativarIdoso(@PathVariable Long id) {
        idosoService.inativarIdoso(id);
        return ResponseEntity.noContent().build();
    }
}