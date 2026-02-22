package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Pulseira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PulseiraRepository extends JpaRepository<Pulseira, Long> {
}
