package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.Asilo;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsiloRepository extends JpaRepository<Asilo, Long> {

    boolean existsByCnpj(@NotBlank(message = "O CNPJ é obrigatório.") String cnpj);

}
