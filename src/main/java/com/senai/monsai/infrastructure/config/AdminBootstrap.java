package com.senai.monsai.infrastructure.config;

import com.senai.monsai.domain.entity.Usuario;
import com.senai.monsai.domain.enums.TipoUsuario;
import com.senai.monsai.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@monsai.com";
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario superAdmin = new Usuario();
            superAdmin.setNome("Super Admin Monsai");
            superAdmin.setEmail(adminEmail);

            superAdmin.setSenha(passwordEncoder.encode("admin123"));

            superAdmin.setTipo(TipoUsuario.SUPER_ADMIN);

            usuarioRepository.save(superAdmin);
            System.out.println("✅ Super Admin criado com sucesso! Email: " + adminEmail);
        } else {
            System.out.println("⚡ O Super Admin já está cadastrado no sistema.");
        }
    }
}