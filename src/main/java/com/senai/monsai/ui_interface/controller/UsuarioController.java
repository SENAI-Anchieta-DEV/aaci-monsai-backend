package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AtualizarSenhaDTO;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.application.service.UsuarioService;
import com.senai.monsai.domain.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gestão de Gestores, Funcionários, Familiares e vínculos com Idosos")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ==========================================
    // 1. CRIAR USUÁRIO
    // ==========================================
    @PostMapping
    @Operation(summary = "Criar um novo Usuário", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Usuario> criarUsuario(@RequestBody UsuarioCreateDTO dto) {
        Usuario novoUsuario = usuarioService.criarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    // ==========================================
    // 2. LISTAR USUÁRIOS
    // ==========================================
    @GetMapping
    @Operation(summary = "Listar todos os usuários", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ==========================================
    // 3. ATUALIZAR SENHA (PATCH)
    // ==========================================
    @PatchMapping("/{id}/senha")
    @Operation(summary = "Atualizar a senha de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Void> atualizarSenha(@PathVariable Long id, @RequestBody AtualizarSenhaDTO dto) {
        usuarioService.atualizarSenha(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. VINCULAR IDOSO AO USUÁRIO
    // ==========================================
    @PostMapping("/{idUsuario}/idosos/{idIdoso}")
    @Operation(summary = "Vincular um idoso a um usuário (Enfermeiro/Cuidador/Familiar)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Void> vincularIdoso(@PathVariable Long idUsuario, @PathVariable Long idIdoso) {
        usuarioService.vincularIdoso(idUsuario, idIdoso);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 5. DESVINCULAR IDOSO DO USUÁRIO
    // ==========================================
    @DeleteMapping("/{idUsuario}/idosos/{idIdoso}")
    @Operation(summary = "Desvincular um idoso de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Void> desvincularIdoso(@PathVariable Long idUsuario, @PathVariable Long idIdoso) {
        usuarioService.desvincularIdoso(idUsuario, idIdoso);
        return ResponseEntity.noContent().build();
    }
}