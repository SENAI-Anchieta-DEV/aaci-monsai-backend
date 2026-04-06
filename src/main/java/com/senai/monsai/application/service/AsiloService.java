package com.senai.monsai.application.service;

import com.senai.monsai.application.dto.AsiloCreateDTO;
import com.senai.monsai.domain.entity.Asilo;
import com.senai.monsai.domain.exception.RecursoDuplicadoException;
import com.senai.monsai.domain.exception.RecursoNaoEncontradoException;
import com.senai.monsai.domain.exception.RegraNegocioException;
import com.senai.monsai.domain.repository.AsiloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AsiloService {

    private final AsiloRepository asiloRepository;

    public Asilo criarAsilo(AsiloCreateDTO dto) {
        if (asiloRepository.existsByCnpj(dto.cnpj())) {
            throw new RecursoDuplicadoException("Já existe um asilo cadastrado com este CNPJ.");
        }

        Asilo asilo = new Asilo();
        asilo.setNome(dto.nome());
        asilo.setCnpj(dto.cnpj());
        asilo.setEndereco(dto.endereco());
        asilo.setAtivo(true);
        return asiloRepository.save(asilo);
    }

    public List<Asilo> listarTodos() {
        return asiloRepository.findAll();
    }

    public Asilo atualizarAsilo(Long id, AsiloCreateDTO dto) {
        Asilo asilo = asiloRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Asilo não encontrado."));

        if (!asilo.isAtivo()) {
            throw new RegraNegocioException("Não é possível editar um asilo que está inativo.");
        }

        // Valida se o novo CNPJ já pertence a outro asilo
        if (!asilo.getCnpj().equals(dto.cnpj()) && asiloRepository.existsByCnpj(dto.cnpj())) {
            throw new RecursoDuplicadoException("Já existe outro asilo cadastrado com este CNPJ.");
        }

        asilo.setNome(dto.nome());
        asilo.setCnpj(dto.cnpj());
        asilo.setEndereco(dto.endereco());

        return asiloRepository.save(asilo);
    }

    public void inativarAsilo(Long id) {
        Asilo asilo = asiloRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Asilo não encontrado."));

        if (!asilo.isAtivo()) {
            throw new RegraNegocioException("Este asilo já está inativo.");
        }

        asilo.setAtivo(false);
        asiloRepository.save(asilo);
    }
}