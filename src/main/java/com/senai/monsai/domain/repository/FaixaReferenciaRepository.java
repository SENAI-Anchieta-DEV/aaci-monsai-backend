package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.FaixaReferencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FaixaReferenciaRepository extends JpaRepository<FaixaReferencia, Long> {
    Optional<FaixaReferencia> findByIdosoId(Long idosoId);
}
