package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.AtualizarSenhaDTO;
import com.senai.monsai.application.dto.UsuarioCreateDTO;
import com.senai.monsai.application.service.UsuarioService;
import com.senai.monsai.domain.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; // <-- Import do @Valid
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (Apenas Gestores/Admins podem criar)"),
            @ApiResponse(responseCode = "409", description = "Conflito: CPF ou E-mail já cadastrados")
    })
    public ResponseEntity<Usuario> criarUsuario(@Valid @RequestBody UsuarioCreateDTO dto) { // <-- @Valid adicionado
        Usuario novoUsuario = usuarioService.criarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    // ==========================================
    // 2. LISTAR USUÁRIOS
    // ==========================================
    @GetMapping
    @Operation(summary = "Listar todos os usuários", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ==========================================
    // 3. ATUALIZAR SENHA (PATCH)
    // ==========================================
    @PatchMapping("/{id}/senha")
    @Operation(summary = "Atualizar a senha de um usuário", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha atualizada com sucesso (Sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado pelo ID fornecido")
    })
    public ResponseEntity<Void> atualizarSenha(@PathVariable Long id, @Valid @RequestBody AtualizarSenhaDTO dto) { // <-- @Valid adicionado
        usuarioService.atualizarSenha(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. VINCULAR IDOSO AO USUÁRIO
    // ==========================================
    @PostMapping("/{idUsuario}/idosos/{idIdoso}")
    @Operation(summary = "Vincular um idoso a um usuário (Enfermeiro/Cuidador/Familiar)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Idoso vinculado com sucesso (Sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário ou Idoso não encontrado")
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Idoso desvinculado com sucesso (Sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário ou Idoso não encontrado")
    })
    public ResponseEntity<Void> desvincularIdoso(@PathVariable Long idUsuario, @PathVariable Long idIdoso) {
        usuarioService.desvincularIdoso(idUsuario, idIdoso);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 6. INATIVAR USUÁRIO
    // ==========================================
    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar um usuário (Demissão/Desligamento)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário inativado com sucesso (Sem conteúdo de retorno)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado pelo ID fornecido")
    })
    public ResponseEntity<Void> inativarUsuario(@PathVariable Long id) {
        usuarioService.inativarUsuario(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}