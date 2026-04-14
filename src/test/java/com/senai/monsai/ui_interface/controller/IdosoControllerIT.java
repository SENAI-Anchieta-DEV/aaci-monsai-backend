package com.senai.monsai.ui_interface.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.application.dto.IdosoUpdateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.AsiloRepository;
import com.senai.monsai.domain.repository.IdosoRepository;
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

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdosoControllerIT {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private IdosoRepository idosoRepository;
    @Autowired private AsiloRepository asiloRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JwtService jwtService;

    private Asilo asiloSalvo;
    private String tokenGestor;

    @BeforeEach
    void setup() {
        // Limpeza respeitando as constraints de integridade
        usuarioRepository.deleteAll();
        idosoRepository.deleteAll();
        asiloRepository.deleteAll();

        // 1. Criar Asilo
        Asilo asilo = new Asilo();
        asilo.setNome("Asilo Monsai");
        asilo.setCnpj("12345678000199");
        asilo.setEndereco("Rua Tech, 123");
        asilo.setAtivo(true);
        asiloSalvo = asiloRepository.save(asilo);

        // 2. Criar GESTOR (O Service buscará esse usuário pelo e-mail do Token)
        Usuario gestor = Usuario.builder()
                .nome("Gestor de Teste")
                .email("gestor@monsai.com")
                .cpf("11122233344")
                .senha("senha123")
                .tipo(TipoUsuario.GESTOR)
                .asilo(asiloSalvo)
                .ativo(true)
                .idosos(new ArrayList<>())
                .build();
        usuarioRepository.save(gestor);

        // 3. Gerar Token vinculado ao e-mail do gestor
        tokenGestor = jwtService.generateToken(gestor.getEmail(), "ROLE_GESTOR");
    }

    @Test
    @DisplayName("1. Deve criar idoso e vincular dispositivo")
    void deveCriarIdoso() throws Exception {
        // PEGANDO O ID REAL GERADO NO SETUP
        String idRealDoBanco = asiloSalvo.getId().toString();

        IdosoCreateDTO dto = new IdosoCreateDTO(
                "Benedito Silva",
                "70275850021",
                "benedito@email.com",
                idRealDoBanco, // <--- ID DINÂMICO AQUI
                3L
        );

        mockMvc.perform(post("/idosos")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Benedito Silva"));
    }

    @Test
    @DisplayName("2. Deve falhar ao criar idoso com CPF duplicado")
    void naoDeveDuplicarCpf() throws Exception {
        String idRealDoBanco = asiloSalvo.getId().toString();

        // Salva um idoso prévio para causar o conflito
        Idoso idosoExistente = new Idoso();
        idosoExistente.setNome("Já Existo");
        idosoExistente.setCpf("12345678901");
        idosoExistente.setAsilo(asiloSalvo);
        idosoExistente.setAtivo(true);
        idosoRepository.save(idosoExistente);

        // Tenta criar outro com o mesmo CPF
        IdosoCreateDTO dto = new IdosoCreateDTO(
                "Novo",
                "12345678901",
                "email@email.com",
                idRealDoBanco, // <--- ID DINÂMICO AQUI TAMBÉM
                4L
        );

        mockMvc.perform(post("/idosos")
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isConflict()); // Agora deve retornar 409 em vez de 404
    }

    @Test
    @DisplayName("3. Deve atualizar dados do idoso")
    void deveAtualizarIdoso() throws Exception {
        Idoso idoso = new Idoso();
        idoso.setNome("Original");
        idoso.setCpf("11122233344");
        idoso.setAsilo(asiloSalvo);
        idoso.setAtivo(true);
        idoso = idosoRepository.save(idoso);

        IdosoUpdateDTO updateDto = new IdosoUpdateDTO("Nome Atualizado", "11122233344", "novo@email.com");

        mockMvc.perform(put("/idosos/" + idoso.getId())
                        .header("Authorization", "Bearer " + tokenGestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"));
    }

    @Test
    @DisplayName("4. Deve inativar idoso e remover vínculo com dispositivo")
    void deveInativarIdoso() throws Exception {
        Idoso idoso = new Idoso();
        idoso.setNome("Para Inativar");
        idoso.setCpf("99988877766");
        idoso.setAsilo(asiloSalvo);
        idoso.setAtivo(true);
        idoso = idosoRepository.save(idoso);

        mockMvc.perform(delete("/idosos/" + idoso.getId())
                        .header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isNoContent());

        Idoso inativado = idosoRepository.findById(idoso.getId()).get();
        assertFalse(inativado.isAtivo());
    }

    @Test
    @DisplayName("5. Segurança: Não deve permitir acesso de outro asilo")
    void naoDeveAcessarOutroAsilo() throws Exception {
        // Criar um segundo asilo
        Asilo outroAsilo = new Asilo();
        outroAsilo.setNome("Outro Asilo");
        outroAsilo.setCnpj("00000000000000");
        outroAsilo = asiloRepository.save(outroAsilo);

        // Criar idoso vinculado ao segundo asilo
        Idoso idosoAlheio = new Idoso();
        idosoAlheio.setNome("Idoso Alheio");
        idosoAlheio.setCpf("00000000000");
        idosoAlheio.setAsilo(outroAsilo);
        idosoAlheio.setAtivo(true);
        idosoAlheio = idosoRepository.save(idosoAlheio);

        // Gestor do Asilo 1 tenta deletar idoso do Asilo 2
        mockMvc.perform(delete("/idosos/" + idosoAlheio.getId())
                        .header("Authorization", "Bearer " + tokenGestor))
                .andExpect(status().isBadRequest()); // RegraNegocioException mapeada para 400
    }
}