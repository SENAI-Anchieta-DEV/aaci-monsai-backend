package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        // Criando um usuário de teste com senha criptografada
        Usuario usuario = Usuario.builder()
                .nome("Usuario Teste")
                .email("teste@monsai.com")
                .senha(passwordEncoder.encode("senha123")) // Senha real: senha123
                .tipo(TipoUsuario.GESTOR)
                .ativo(true)
                .build();
        usuarioRepository.save(usuario);
    }

    @Test
    @DisplayName("1. Deve realizar login com sucesso e retornar token")
    void deveFazerLoginSucesso() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("teste@monsai.com", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipoPerfil").value("GESTOR")); // Ajustado para tipoPerfil
    }

    @Test
    @DisplayName("2. Deve retornar 401 para senha incorreta")
    void deveRetornar401SenhaIncorreta() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("teste@monsai.com", "senha_errada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3. Deve retornar 401 para e-mail inexistente")
    void deveRetornar401EmailInexistente() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("nao_existo@monsai.com", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. Deve retornar 400 quando o e-mail for inválido/vazio")
    void deveRetornar400EmailInvalido() throws Exception {
        // Testando validação do @Valid
        LoginRequestDTO dto = new LoginRequestDTO("", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("5. Deve retornar 401 para usuário inativo")
    void deveRetornar401UsuarioInativo() throws Exception {
        // Tornando o usuário inativo
        Usuario inativo = usuarioRepository.findByEmail("teste@monsai.com").get();
        inativo.setAtivo(false);
        usuarioRepository.save(inativo);

        LoginRequestDTO dto = new LoginRequestDTO("teste@monsai.com", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }


}