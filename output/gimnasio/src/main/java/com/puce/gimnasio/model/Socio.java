package com.puce.gimnasio.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Socio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String plan;
    private double mensualidad;
    private boolean activo;
}
