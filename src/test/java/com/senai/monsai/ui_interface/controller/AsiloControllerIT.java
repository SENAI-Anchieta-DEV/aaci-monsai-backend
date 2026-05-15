package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.AsiloCreateDTO;
import com.senai.monsai.application.dto.LoginRequestDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import com.senai.monsai.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AsiloControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private AsiloRepository asiloRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tokenAdmin;
    private String tokenGestorComum;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        asiloRepository.deleteAll();

        // 1. Criar e Salvar Super Admin
        Usuario admin = Usuario.builder()
                .nome("Admin Sistema")
                .email("admin@monsai.com")
                .tipo(TipoUsuario.SUPER_ADMIN)
                .ativo(true)
                .build();
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.generateToken(admin.getEmail(), "ROLE_SUPER_ADMIN");

        // 2. Criar e Salvar Gestor (Para que o filtro de segurança o encontre)
        Usuario gestor = Usuario.builder()
                .nome("Gestor Comum")
                .email("gestor@teste.com")
                .tipo(TipoUsuario.GESTOR)
                .ativo(true)
                .build();
        usuarioRepository.save(gestor);
        tokenGestorComum = jwtService.generateToken(gestor.getEmail(), "ROLE_GESTOR");
    }

    @Test
    @DisplayName("1. Deve criar um asilo com sucesso (Super Admin)")
    void deveCriarAsiloSucesso() throws Exception {
        AsiloCreateDTO dto = new AsiloCreateDTO("Asilo Vida Feliz", "12345678000199", "Rua das Flores, 10");

        mockMvc.perform(post("/asilos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Asilo Vida Feliz"))
                .andExpect(jsonPath("$.cnpj").value("12345678000199"));
    }

    @Test
    @DisplayName("2. Deve retornar 403 ao tentar criar asilo sem ser Super Admin")
    void deveRetornar403CriarAsilo() throws Exception {
        AsiloCreateDTO dto = new AsiloCreateDTO("Asilo Ilegal", "000", "Endereço");

        mockMvc.perform(post("/asilos")
                        .header("Authorization", "Bearer " + tokenGestorComum)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Deve retornar 409 ao cadastrar CNPJ duplicado")
    void deveRetornar409CnpjDuplicado() throws Exception {
        // Salva um asilo primeiro
        Asilo existente = new Asilo();
        existente.setNome("Asilo 1");
        existente.setCnpj("99999999000188");
        asiloRepository.save(existente);

        AsiloCreateDTO dto = new AsiloCreateDTO("Asilo 2", "99999999000188", "Endereço");

        mockMvc.perform(post("/asilos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict()); // RecursoDuplicadoException
    }

    @Test
    @DisplayName("4. Deve listar todos os asilos")
    void deveListarAsilos() throws Exception {
        mockMvc.perform(get("/asilos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("5. Deve retornar 404 ao atualizar asilo inexistente")
    void deveRetornar404Atualizar() throws Exception {
        AsiloCreateDTO dto = new AsiloCreateDTO("Novo Nome", "123", "End");

        mockMvc.perform(put("/asilos/9999") // ID que não existe
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("6. Deve inativar um asilo com sucesso")
    void deveInativarAsilo() throws Exception {
        Asilo asilo = new Asilo();
        asilo.setNome("Para Deletar");
        asilo.setCnpj("00011122233344");
        asilo.setAtivo(true);
        asilo = asiloRepository.save(asilo);

        mockMvc.perform(delete("/asilos/" + asilo.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        // Opcional: Verificar no banco se está ativo = false
    }

    @Test
    @DisplayName("7. Deve retornar 400 ao inativar asilo que já está inativo")
    void deveRetornar400InativarJaInativo() throws Exception {
        Asilo asilo = new Asilo();
        asilo.setCnpj("21.321.321/3213-12");
        asilo.setNome("Inativo");
        asilo.setAtivo(false); // Já inativo
        asilo = asiloRepository.save(asilo);

        mockMvc.perform(delete("/asilos/" + asilo.getId())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest()); // RegraNegocioException
    }

    @Test
    @DisplayName("Deve negar login se o asilo do usuário estiver inativo")
    void deveNegarLoginAsiloInativo() throws Exception {
        Asilo asilo = new Asilo();
        asilo.setCnpj("21.321.321/3213-12");
        asilo.setNome("NoLogin");
        asilo.setAtivo(false);
        asiloRepository.save(asilo);

        LoginRequestDTO dto = new LoginRequestDTO("gestor@monsai.com", "senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized()); // Ou Forbidden, dependendo da sua regra
    }
}