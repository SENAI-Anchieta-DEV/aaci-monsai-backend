package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.IdosoCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.entity.Idoso;
import com.senai.monsai.domain.entity.Dispositivo;
import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.repository.IdosoRepository;
import com.senai.monsai.domain.repository.PulseiraRepository;
import com.senai.monsai.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdosoService {

    private final IdosoRepository idosoRepository;
    private final PulseiraRepository pulseiraRepository;
    private final UsuarioRepository usuarioRepository;

    public Idoso criarIdoso(IdosoCreateDTO dto) {
        // 1. Descobre quem é o Gestor que está logado fazendo a requisição
        String emailGestorLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario gestor = usuarioRepository.findByEmail(emailGestorLogado).orElseThrow();
        Asilo asiloDoGestor = gestor.getAsilo();

        if (asiloDoGestor == null) {
            throw new RuntimeException("Este usuário não está vinculado a nenhum asilo.");
        }

        // 2. Cria a Pulseira IoT no banco
        Dispositivo dispositivo = new Dispositivo();
        dispositivo.setSerial(dto.serialDispositivo());
        dispositivo = pulseiraRepository.save(dispositivo);

        // 3. Cria o Idoso e amarra tudo
        Idoso idoso = new Idoso();
        idoso.setNome(dto.nome());
        idoso.setCpf(dto.cpf());
        idoso.setEmail(dto.email());
        idoso.setDispositivo(dispositivo);
        idoso.setAsilo(asiloDoGestor); // O idoso vai direto pro asilo do gestor!
        dispositivo.setIdoso(idoso);
        return idosoRepository.save(idoso);
    }
}
