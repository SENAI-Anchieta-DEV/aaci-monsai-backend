package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.application.dto.LoginResponseDTO;
import com.senai.monsai.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso e Token gerado"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos (ex: e-mail em branco)"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (E-mail ou senha incorretos)")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) { //valid adicionado
        LoginResponseDTO response = authService.autenticar(dto);
        return ResponseEntity.ok(response);
    }
}