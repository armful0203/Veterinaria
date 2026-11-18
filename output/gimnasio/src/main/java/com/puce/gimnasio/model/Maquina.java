package com.puce.gimnasio.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Maquina {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String marca;
    private int anioCompra;
    private boolean enMantenimiento;
}
