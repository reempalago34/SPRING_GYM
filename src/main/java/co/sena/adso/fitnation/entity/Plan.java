package co.sena.adso.fitnation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Réplica de app/models/plan.py (Flask).
 *
 * Flask usa Float para precio -> Double aquí (misma representación JSON).
 */
@Entity
@Table(name = "planes", schema = "fitnation")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPlan")
    private Integer idPlan;

    @Column(nullable = false, length = 100)
    private String nombrePlan;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer duracionMeses;

    public Plan() {
    }

    public Integer getIdPlan() { return idPlan; }
    public void setIdPlan(Integer idPlan) { this.idPlan = idPlan; }

    public String getNombrePlan() { return nombrePlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public Integer getDuracionMeses() { return duracionMeses; }
    public void setDuracionMeses(Integer duracionMeses) { this.duracionMeses = duracionMeses; }
}
