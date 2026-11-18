package com.puce.gimnasio.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clase_grupal")
public class ClaseGrupal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nombre;
    private String instructor;
    private String horario;
    private int cupos;
}
