package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.application.dto.LoginResponseDTO;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import com.senai.monsai.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException; // Import adicionado
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public LoginResponseDTO autenticar(LoginRequestDTO dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        if (!usuario.isAtivo()) {
            throw new BadCredentialsException("Usuário inativo.");
        }

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getTipo().name());
        // Exemplo de como a equipe de backend deve retornar os dados no AuthService:
        return new LoginResponseDTO(
                token,
                usuario.getTipo().name(),
                usuario.getId(),
                usuario.getNome(),
                usuario.getCpf(),
                usuario.getAsilo() != null ? usuario.getAsilo().getId() : null
        );
    }
}