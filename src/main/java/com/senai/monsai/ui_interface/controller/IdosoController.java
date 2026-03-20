package com.senai.monsai.ui_interface.controller;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.service.IdosoService;
import com.senai.monsai.domain.entity.Idoso;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/idosos")
@RequiredArgsConstructor
public class IdosoController {

    private final IdosoService idosoService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
   @PreAuthorize("hasRole('GESTOR')") // APENAS GESTOR PODE ACESSAR!
    public ResponseEntity<Idoso> criarIdoso(@RequestBody IdosoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(idosoService.criarIdoso(dto));
    }
}