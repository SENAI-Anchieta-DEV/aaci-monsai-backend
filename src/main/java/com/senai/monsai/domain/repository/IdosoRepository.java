package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Idoso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdosoRepository extends JpaRepository<Idoso, Long> {
    boolean existsByCpf(String cpf);
}
