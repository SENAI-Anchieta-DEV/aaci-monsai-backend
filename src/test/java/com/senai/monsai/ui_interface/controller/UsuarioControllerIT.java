package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.AtualizarSenhaDTO;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerIT {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AsiloRepository asiloRepository;

    @Autowired private JwtService jwtService;

    private Asilo asiloSalvo;
    private String tokenGestor;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        asiloRepository.deleteAll();

        // 1. Prepara um Asilo que será usado em todos os testes
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo Teste");
        asilo.setCnpj("12345678000199");
        asiloSalvo = asiloRepository.save(asilo);

        // 2. Cria um Gestor REAL no banco para gerar um token válido
        Usuario gestor = new Usuario();
        gestor.setNome("Gestor Master");
        gestor.setEmail("gestor@teste.com");
        gestor.setCpf("99999999999");
        gestor.setSenha("senha123");
        gestor.setTipo(TipoUsuario.GESTOR);
        gestor.setAsilo(asiloSalvo);
        gestor.setAtivo(true);
        usuarioRepository.save(gestor);

        // 3. Gera o token real para o Gestor (Idêntico ao que o Login faz)
        tokenGestor = jwtService.generateToken(gestor.getEmail(), "ROLE_GESTOR");
    }

    @Test
    @DisplayName("1. Deve criar um usuário quando os dados forem válidos")
    void deveCriarUsuario() throws Exception {
        String json = """
                {
                  "nome": "Carlos Silva",
                  "email": "carlos@gmail.com",
                  "cpf": "12345678901",
                  "senha": "Senha123!",
                  "tipoUsuario": "ENFERMEIRO",
                  "asiloId": %d
                }
                """.formatted(asiloSalvo.getId());

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + tokenGestor) // <-- Passando o Token Real!
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("2. Deve listar todos os usuários cadastrados")
    void deveListarUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .header("Authorization", "Bearer " + tokenGestor)) // <-- Passando o Token Real!
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("3. Deve atualizar a senha de um usuário existente")
    void deveAtualizarSenha() throws Exception {
        // Primeiro, salvamos um usuário no banco para ter quem atualizar
        Usuario user = new Usuario();
        user.setNome("Ana");
        user.setEmail("ana@teste.com");
        user.setCpf("99988877766");
        user.setSenha("velha123");
        user.setTipo(TipoUsuario.ENFERMEIRO);
        user.setAsilo(asiloSalvo);
        user.setAtivo(true);
        user = usuarioRepository.save(user);

        // DTO de atualização
        AtualizarSenhaDTO dto = new AtualizarSenhaDTO("NovaSenha123!");

        mockMvc.perform(patch("/usuarios/" + user.getId() + "/senha")
                        .header("Authorization", "Bearer " + tokenGestor) // <-- Passando o Token Real!
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("4. Deve inativar um usuário (Soft Delete)")
    void deveInativarUsuario() throws Exception {
        Usuario user = new Usuario();
        user.setNome("Remover");
        user.setEmail("remover@teste.com");
        user.setCpf("11122233344");
        user.setSenha("senha");
        user.setTipo(TipoUsuario.ENFERMEIRO);
        user.setAsilo(asiloSalvo);
        user.setAtivo(true);
        user = usuarioRepository.save(user);

        mockMvc.perform(delete("/usuarios/" + user.getId())
                        .header("Authorization", "Bearer " + tokenGestor)) // <-- Passando o Token Real!
                .andExpect(status().isNoContent());

        // Verificação extra: se o campo ativo no banco agora é false
        assertFalse(usuarioRepository.findById(user.getId()).get().isAtivo());
    }

    @Test
    @DisplayName("5. Segurança: Enfermeiro não pode criar outros usuários")
    void funcionarioNaoPodeCriarUsuario() throws Exception {
        // Cria um enfermeiro no banco para testar a falha de segurança
        Usuario enfermeiro = new Usuario();
        enfermeiro.setNome("Enfermeiro Teste");
        enfermeiro.setEmail("enfermeiro@teste.com");
        enfermeiro.setCpf("55566677788");
        enfermeiro.setSenha("senha123");
        enfermeiro.setTipo(TipoUsuario.ENFERMEIRO);
        enfermeiro.setAsilo(asiloSalvo);
        enfermeiro.setAtivo(true);
        usuarioRepository.save(enfermeiro);

        // Gera o token de enfermeiro
        String tokenEnfermeiro = jwtService.generateToken(enfermeiro.getEmail(), "ROLE_ENFERMEIRO");

        mockMvc.perform(post("/usuarios")
                        .header("Authorization", "Bearer " + tokenEnfermeiro) // <-- Usa o token do enfermeiro
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden()); // Espera um 403 Proibido com sucesso!
    }

    
}