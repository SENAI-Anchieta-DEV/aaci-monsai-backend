package com.senai.monsai.domain.repository;

import com.senai.monsai.domain.entity.MensagemMqtt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensagemMqttRepository extends JpaRepository<MensagemMqtt, Long> {
}
