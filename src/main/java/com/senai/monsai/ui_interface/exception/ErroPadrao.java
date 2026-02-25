package com.senai.monsai.ui_interface.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErroPadrao {
    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String caminho;
}
