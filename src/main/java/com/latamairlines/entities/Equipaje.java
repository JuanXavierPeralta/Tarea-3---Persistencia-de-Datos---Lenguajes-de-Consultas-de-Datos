
package com.latamairlines.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_equipaje")
public class Equipaje {

    // IDENTIFICADOR AUTOMATICO
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "equipaje_generator"
    )
    @SequenceGenerator(
        name = "equipaje_generator",
        sequenceName = "tb_equipaje_seq",
        allocationSize = 1
    )
    @Column(name = "eq_id", nullable = false)
    private Integer id;

    // ATRIBUTOS
    @Column(name = "eq_tipo", nullable = false)
    private String tipo;

    @Column(name = "eq_pesoKg",
            nullable = false,
            precision = 6,
            scale = 2)
    private BigDecimal pesoKg;

    // MUCHOS EQUIPAJES PUEDEN PERTENECER A UNA RESERVA
    @ManyToOne(optional = false)
    @JoinColumn(name = "eq_reserva", nullable = false)
    private Reserva reserva;

    // CONSTRUCTORES
    public Equipaje() {
    }

    public Equipaje(String tipo, BigDecimal pesoKg,
                    Reserva reserva) {
        this.tipo = tipo;
        this.pesoKg = pesoKg;
        this.reserva = reserva;
    }

    // GETTERS
    public Integer getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public Reserva getReserva() {
        return reserva;
    }

    // SETTERS
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
}
