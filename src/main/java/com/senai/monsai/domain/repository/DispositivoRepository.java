package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Dispositivo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, String> {
    Optional<Dispositivo> findBySerial(String serial);
}
