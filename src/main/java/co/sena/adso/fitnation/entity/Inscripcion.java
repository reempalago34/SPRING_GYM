package co.sena.adso.fitnation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Réplica de app/models/inscripcion.py (Flask).
 */
@Entity
@Table(name = "inscripciones", schema = "fitnation")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscripcion")
    private Integer idInscripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "idCliente", nullable = false)
    private Integer idCliente;

    @Column(name = "idPlan", nullable = false)
    private Integer idPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCliente", referencedColumnName = "idCliente",
            insertable = false, updatable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPlan", referencedColumnName = "idPlan",
            insertable = false, updatable = false)
    private Plan plan;

    public Inscripcion() {
    }

    /** Equivale al default=get_colombia_time de Flask. */
    @PrePersist
    void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now(ZoneId.of("America/Bogota"));
        }
    }

    public Integer getIdInscripcion() { return idInscripcion; }
    public void setIdInscripcion(Integer idInscripcion) { this.idInscripcion = idInscripcion; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public Integer getIdPlan() { return idPlan; }
    public void setIdPlan(Integer idPlan) { this.idPlan = idPlan; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }
}
