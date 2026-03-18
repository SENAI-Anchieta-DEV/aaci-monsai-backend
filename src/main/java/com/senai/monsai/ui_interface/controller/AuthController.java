package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.application.dto.LoginResponseDTO;
import com.senai.monsai.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login e geração de token JWT")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Recebe e-mail e senha e retorna o token JWT.")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = authService.autenticar(dto);
        return ResponseEntity.ok(response);
    }
}