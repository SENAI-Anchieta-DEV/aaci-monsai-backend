package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Idoso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdosoRepository extends JpaRepository<Idoso, Long> {
    boolean existsByCpf(String attr0);
    List<Idoso> findByAsiloId(Long asiloId);
}
