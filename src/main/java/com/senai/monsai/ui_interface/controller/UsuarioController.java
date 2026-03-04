package com.senai.monsai.ui_interface.controller;

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

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gestão de Gestores, Funcionários e Familiares")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Criar um novo Usuário", security = @SecurityRequirement(name = "bearerAuth"))
    // A anotação abaixo garante que apenas o SUPER_ADMIN ou um GESTOR possam criar contas
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'GESTOR')")
    public ResponseEntity<Usuario> criarUsuario(@RequestBody UsuarioCreateDTO dto) {
        Usuario novoUsuario = usuarioService.criarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }
}
