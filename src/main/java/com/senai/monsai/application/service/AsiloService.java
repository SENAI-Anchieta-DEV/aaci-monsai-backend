package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.AsiloCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.repository.AsiloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsiloService {

    private final AsiloRepository asiloRepository;

    public Asilo criarAsilo(AsiloCreateDTO dto) {
        Asilo asilo = new Asilo();
        asilo.setNome(dto.nome());
        asilo.setCnpj(dto.cnpj());
        asilo.setEndereco(dto.endereco());

        return asiloRepository.save(asilo);
    }
}
